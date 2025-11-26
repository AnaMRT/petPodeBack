package app.pet_pode_back.integration.service;

import app.pet_pode_back.dto.LoginRequest;
import app.pet_pode_back.exception.ParametroInvalidoException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PasswordResetTokenRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.model.PasswordResetToken;
import app.pet_pode_back.service.AuthService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder encoder;

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
    void deveFalharAoTentarLogarSenhaErrada() {
        LoginRequest req = new LoginRequest();
        req.setEmail(usuarioPadrao.getEmail());
        req.setSenha("senhaErrada");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Credenciais inválidas");
    }

    // ----------------------------------------------------------
    // REGISTRO
    // ----------------------------------------------------------

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
    void deveFalharAoRegistrarEmailDuplicado() {
        Usuario novo = new Usuario();
        novo.setNome("X");
        novo.setEmail(usuarioPadrao.getEmail());
        novo.setSenha("aaaaaa");

        assertThatThrownBy(() -> authService.registrar(novo))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Email já cadastrado");
    }

    @Test
    void deveGerarTokenDeRedefinicao() {
        authService.solicitarRedefinicaoSenha(usuarioPadrao.getEmail());

        assertThat(tokenRepository.count()).isEqualTo(1);
    }

    @Test
    void deveFalharSolicitarRedefinicaoParaEmailInexistente() {
        assertThatThrownBy(() -> authService.solicitarRedefinicaoSenha("aaaa@email.com"))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado");
    }


    @Test
    void deveRedefinirSenhaComSucesso() {
        // criar manualmente token
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
    void deveFalharComCodigoInvalido() {
        assertThatThrownBy(() -> authService.redefinirSenha("999999", "senha"))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Código inválido.");
    }

    @Test
    void deveFalharComTokenExpirado() {
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
    void deveFalharComTokenJaUsado() {
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

    // -------------------------------
// REDEFINIÇÃO DE SENHA – TOKEN JÁ USADO
// -------------------------------
    @Test
    void deveMarcarTokenComoUsadoAposRedefinirSenha() {
        PasswordResetToken token = new PasswordResetToken();
        token.setCodigo("333444");
        token.setUsuario(usuarioPadrao);
        token.setUsed(false);
        token.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        tokenRepository.save(token);

        authService.redefinirSenha("333444", "novaSenha123");

        PasswordResetToken atualizado = tokenRepository.findByCodigo("333444").get();
        assertThat(atualizado.isUsed()).isTrue(); // verifica se o token foi marcado como usado
    }

    // -------------------------------
// REDIFINICAÇÃO – CAMPOS OBRIGATÓRIOS FALTANDO
// -------------------------------
    @Test
    void deveFalharRedefinirSenhaSemCodigoOuSenha() {
        assertThatThrownBy(() -> authService.redefinirSenha("", ""))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Código inválido.");
    }

}
