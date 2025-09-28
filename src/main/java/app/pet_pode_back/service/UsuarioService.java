package app.pet_pode_back.service;

import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.exception.ParametroInvalidoException;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PasswordResetTokenRepository;
import app.pet_pode_back.repository.UsuarioRepository;

import app.pet_pode_back.model.PasswordResetToken;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PasswordResetTokenRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsuario(usuario);
        resetToken.setExpirationDate(LocalDateTime.now().plusMinutes(15));
        resetToken.setUsed(false);

        resetTokenRepository.save(resetToken);

        String link = "petpode://reset-password?token=" + token;

        // 🔹 Aqui você monta o HTML
        String corpoEmail = "<p>Clique no link para redefinir sua senha:</p>"
                + "<p><a href='" + link + "'>Redefinir senha</a></p>"
                + "<br/>"
                + "<p>Ou copie e cole no navegador: " + link + "</p>";

        // 🔹 E aqui você envia
        emailService.enviarEmail(
                usuario.getEmail(),
                "Redefinição de senha",
                corpoEmail
        );
    }


    public void redefinirSenha(String token, String novaSenha) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido."));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Token já foi utilizado.");
        }

        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado.");
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








