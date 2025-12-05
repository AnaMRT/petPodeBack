package app.pet_pode_back.integration.service;

import app.pet_pode_back.dto.LoginRequest;
import app.pet_pode_back.exception.ParametroInvalidoException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PasswordResetTokenRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.model.PasswordResetToken;
import app.pet_pode_back.service.AuthService;
import app.pet_pode_back.service.EmailService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(locations = "classpath:.env.test")
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder encoder;

    @MockBean
    private EmailService emailService;

    private Usuario usuarioPadrao;

    @BeforeEach
    void setup() {
        tokenRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuarioPadrao = new Usuario();
        usuarioPadrao.setNome("Rafa");
        usuarioPadrao.setEmail("rafa@email.com");
        usuarioPadrao.setSenha(encoder.encode("123456"));

        usuarioPadrao = usuarioRepository.save(usuarioPadrao);
        doNothing().when(emailService).enviarEmail(anyString(), anyString(), anyString());

    }


    @Test
    void deveLogarComSucesso() {
        LoginRequest req = new LoginRequest();
        req.setEmail(usuarioPadrao.getEmail());
        req.setSenha("123456");

        String token = authService.login(req);

        assertThat(token).isNotBlank();
    }

    @Test
    void deveFalharAoTentarLogarEmailInexistente() {
        LoginRequest req = new LoginRequest();
        req.setEmail("naoexiste@email.com");
        req.setSenha("123456");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    void deveFalharQuandoSenhaIncorreta() {
        LoginRequest req = new LoginRequest();
        req.setEmail(usuarioPadrao.getEmail());
        req.setSenha("senhaErrada");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Credenciais inválidas");
    }


    @Test
    void deveRegistrarComSucesso() {
        Usuario novo = new Usuario();
        novo.setNome("Novo");
        novo.setEmail("novo@email.com");
        novo.setSenha("abc123");

        String token = authService.registrar(novo);

        assertThat(token).isNotBlank();
        assertThat(usuarioRepository.findByEmail(novo.getEmail())).isPresent();
    }

    @Test
    void deveFalharQuandoEmailJaCadastrado() {
        Usuario novo = new Usuario();
        novo.setNome("X");
        novo.setEmail(usuarioPadrao.getEmail());
        novo.setSenha("aaaaaa");

        assertThatThrownBy(() -> authService.registrar(novo))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Email já cadastrado");
    }


    @Test
    void deveFalharSolicitarRedefinicaoParaEmailInexistente() {
        assertThatThrownBy(() -> authService.solicitarRedefinicaoSenha("aaaa@email.com"))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    void deveRedefinirSenhaComSucesso() {
        PasswordResetToken token = new PasswordResetToken();
        token.setCodigo("123456");
        token.setUsuario(usuarioPadrao);
        token.setUsed(false);
        token.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        tokenRepository.save(token);

        authService.redefinirSenha("123456", "novaSenha");

        Usuario atualizado = usuarioRepository.findByEmail(usuarioPadrao.getEmail()).get();
        assertThat(encoder.matches("novaSenha", atualizado.getSenha())).isTrue();
    }

    @Test
    void deveFalharAoRedefinirSenhaComCodigoInvalido() {
        assertThatThrownBy(() -> authService.redefinirSenha("999999", "senha"))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Código inválido.");
    }

    @Test
    void deveFalharAoRedefinirSenhaComTokenExpirado() {
        PasswordResetToken token = new PasswordResetToken();
        token.setCodigo("111222");
        token.setUsuario(usuarioPadrao);
        token.setUsed(false);
        token.setExpirationDate(LocalDateTime.now().minusMinutes(1));
        tokenRepository.save(token);

        assertThatThrownBy(() -> authService.redefinirSenha("111222", "nova"))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Código expirado.");
    }

    @Test
    void deveFalharAoRedefinirSenhaComTokenJaUsado() {
        PasswordResetToken token = new PasswordResetToken();
        token.setCodigo("222333");
        token.setUsuario(usuarioPadrao);
        token.setUsed(true);
        token.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        tokenRepository.save(token);

        assertThatThrownBy(() -> authService.redefinirSenha("222333", "nova"))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Código já foi utilizado.");
    }


}
