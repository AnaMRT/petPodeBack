package app.pet_pode_back.service;

import app.pet_pode_back.dto.LoginRequest;
import app.pet_pode_back.model.PasswordResetToken;
import app.pet_pode_back.exception.ParametroInvalidoException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.repository.PasswordResetTokenRepository;
import app.pet_pode_back.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;
@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    public String login(LoginRequest loginRequest) {
        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado"));

        if (!passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenha())) {
            throw new ParametroInvalidoException("Credenciais inválidas");
        }

        return jwtUtil.gerarToken(usuario.getId());
    }

    public String registrar(Usuario novoUsuario) {

        if (usuarioRepository.findByEmail(novoUsuario.getEmail()).isPresent()) {
            throw new ParametroInvalidoException("Email já cadastrado");
        }

        novoUsuario.setSenha(passwordEncoder.encode(novoUsuario.getSenha()));
        Usuario salvo = usuarioRepository.save(novoUsuario);

        return jwtUtil.gerarToken(salvo.getId());
    }

    public void solicitarRedefinicaoSenha(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado"));

        String codigo = String.format("%06d", new Random().nextInt(999999));

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
                .orElseThrow(() -> new ParametroInvalidoException("Código inválido."));

        if (resetToken.isUsed()) {
            throw new ParametroInvalidoException("Código já foi utilizado.");
        }

        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new ParametroInvalidoException("Código expirado.");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);
    }
}
