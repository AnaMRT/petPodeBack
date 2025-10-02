package app.pet_pode_back.service;

import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PasswordResetTokenRepository;
import app.pet_pode_back.repository.UsuarioRepository;

import app.pet_pode_back.model.PasswordResetToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import jakarta.validation.Valid;

import java.util.List;

@Service
public class UsuarioService {




    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;



    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrar(@Valid Usuario usuario) {
        String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(senhaCriptografada);
      //  verificarEmailExistente(usuario);
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario editarUsuario(UUID usuarioId, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (dto.getNome() != null) {
            usuario.setNome(dto.getNome());
        }

        if (dto.getEmail() != null) {
            usuario.setEmail(dto.getEmail());
        }

        if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        return usuarioRepository.save(usuario);
    }

    public void remover(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

         usuarioRepository.delete(usuario);
    }



    /*public Usuario verificarEmailExistente(Usuario usuario) {
        Optional<Usuario> emailExistente = usuarioRepository.findByEmail(usuario.getEmail());

        if(emailExistente.isPresent()) {
            throw new ParametroInvalidoException("Este email já está cadastrado. Tente novamente");
        }
        return usuario;
    }*/

    public void solicitarRedefinicaoSenha(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String codigo = String.format("%06d", new Random().nextInt(999999)); // Gera PIN de 6 dígitos

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setCodigo(codigo);
        resetToken.setUsuario(usuario);
        resetToken.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        resetToken.setUsed(false);

        resetTokenRepository.save(resetToken);

        String corpoEmail = "Seu código de verificação é: " + codigo + "\n" +
                "Este código expira em 10 minutos.";

        emailService.enviarEmail(
                usuario.getEmail(),
                "Código de verificação para redefinir senha",
                corpoEmail
        );
    }

    public void redefinirSenha(String codigo, String novaSenha) {
        PasswordResetToken resetToken = resetTokenRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Código inválido."));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Código já foi utilizado.");
        }

        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código expirado.");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);
    }

    public Usuario buscarUsuarioPorId(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }


}








