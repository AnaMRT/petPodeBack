package app.pet_pode_back.unit.service;

import app.pet_pode_back.dto.LoginRequest;
import app.pet_pode_back.model.PasswordResetToken;
import app.pet_pode_back.exception.ParametroInvalidoException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.repository.PasswordResetTokenRepository;
import app.pet_pode_back.service.AuthService;
import app.pet_pode_back.service.EmailService;
import app.pet_pode_back.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordResetTokenRepository resetTokenRepository;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail("teste@teste.com");
        usuario.setSenha("senhaCriptografada");
    }


    @Test
    void deveLogarComSucesso() {
        LoginRequest request = new LoginRequest();
        request.setEmail("teste@teste.com");
        request.setSenha("123456");

        when(usuarioRepository.findByEmail("teste@teste.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("123456", "senhaCriptografada"))
                .thenReturn(true);
        when(jwtUtil.gerarToken(usuario.getId()))
                .thenReturn("TOKEN123");

        String token = authService.login(request);

        assertEquals("TOKEN123", token);
    }

    @Test
    void deveFalharQuandoEmailNaoExiste() {
        LoginRequest request = new LoginRequest();
        request.setEmail("naoexiste@x.com");
        request.setSenha("123");

        when(usuarioRepository.findByEmail("naoexiste@x.com"))
                .thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class,
                () -> authService.login(request));
    }

    @Test
    void deveFalharQuandoSenhaIncorreta() {
        LoginRequest request = new LoginRequest();
        request.setEmail("teste@teste.com");
        request.setSenha("errada");

        when(usuarioRepository.findByEmail("teste@teste.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "senhaCriptografada"))
                .thenReturn(false);

        assertThrows(ParametroInvalidoException.class,
                () -> authService.login(request));
    }


    @Test
    void deveRegistrarComSucesso() {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setId(UUID.randomUUID());
        novoUsuario.setEmail("novo@teste.com");
        novoUsuario.setSenha("123");

        when(usuarioRepository.findByEmail("novo@teste.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("123")).thenReturn("CRYPTO");
        when(usuarioRepository.save(any())).thenReturn(novoUsuario);
        when(jwtUtil.gerarToken(novoUsuario.getId())).thenReturn("TOKEN_REG");

        String token = authService.registrar(novoUsuario);

        assertEquals("TOKEN_REG", token);
    }

    @Test
    void deveFalharQuandoEmailJaCadastrado() {
        Usuario novo = new Usuario();
        novo.setEmail("teste@teste.com");

        when(usuarioRepository.findByEmail("teste@teste.com"))
                .thenReturn(Optional.of(usuario));

        assertThrows(ParametroInvalidoException.class,
                () -> authService.registrar(novo));
    }


    @Test
    void deveRedefinirSenhaComSucesso() {
        PasswordResetToken token = new PasswordResetToken();
        token.setCodigo("123456");
        token.setUsuario(usuario);
        token.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        token.setUsed(false);

        when(resetTokenRepository.findByCodigo("123456"))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("novaSenha")).thenReturn("NOVA_CRIPTO");

        authService.redefinirSenha("123456", "novaSenha");

        verify(usuarioRepository).save(usuario);
        verify(resetTokenRepository).save(token);
        assertTrue(token.isUsed());
    }

    @Test
    void deveFalharAoRedefinirSenhaComCodigoInvalido() {
        when(resetTokenRepository.findByCodigo("000000"))
                .thenReturn(Optional.empty());

        ParametroInvalidoException ex = assertThrows(
                ParametroInvalidoException.class,
                () -> authService.redefinirSenha("000000", "senha")
        );
        assertEquals("Código inválido.", ex.getMessage());
    }

    @Test
    void deveFalharAoRedefinirSenhaComTokenJaUsado() {
        PasswordResetToken token = new PasswordResetToken();
        token.setUsed(true);

        when(resetTokenRepository.findByCodigo("123"))
                .thenReturn(Optional.of(token));

        ParametroInvalidoException ex = assertThrows(
                ParametroInvalidoException.class,
                () -> authService.redefinirSenha("123", "nova")
        );
        assertEquals("Código já foi utilizado.", ex.getMessage());
    }

    @Test
    void deveFalharAoRedefinirSenhaComTokenExpirado() {
        PasswordResetToken token = new PasswordResetToken();
        token.setUsed(false);
        token.setExpirationDate(LocalDateTime.now().minusMinutes(1));

        when(resetTokenRepository.findByCodigo("123"))
                .thenReturn(Optional.of(token));

        ParametroInvalidoException ex = assertThrows(
                ParametroInvalidoException.class,
                () -> authService.redefinirSenha("123", "nova")
        );
        assertEquals("Código expirado.", ex.getMessage());
    }

    @Test
    void deveEnviarCodigoDeRedefinicaoComSucesso() {
        String email = "teste@email.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setCodigo("123456");
        resetToken.setUsuario(usuario);
        resetToken.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        resetToken.setUsed(false);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(resetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(resetToken);
        doNothing().when(emailService).enviarEmail(anyString(), anyString(), anyString());

        authService.solicitarRedefinicaoSenha(email);

        verify(usuarioRepository, times(1)).findByEmail(email);
        verify(resetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1))
                .enviarEmail(eq(email), anyString(), contains("Seu código de verificação"));
    }


    @Test
    void deveLancarExcecaoQuandoEmailNaoEncontradoSolicitarRedefinicaoSenha() {
        String email = "naoexiste@email.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class, () -> {
            authService.solicitarRedefinicaoSenha(email);
        });

        verify(usuarioRepository, times(1)).findByEmail(email);
        verify(resetTokenRepository, never()).save(any());
        verify(emailService, never()).enviarEmail(anyString(), anyString(), anyString());
    }


    @Test
    void deveLancarExcecaoQuandoFalharAoEnviarEmail() {
        String email = "teste@email.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(resetTokenRepository.save(any())).thenReturn(new PasswordResetToken());
        doThrow(new RuntimeException("Erro ao enviar email"))
                .when(emailService)
                .enviarEmail(anyString(), anyString(), anyString());

        assertThrows(RuntimeException.class, () -> {
            authService.solicitarRedefinicaoSenha(email);
        });

        verify(resetTokenRepository, times(1)).save(any());
        verify(emailService, times(1))
                .enviarEmail(eq(email), anyString(), anyString());
    }

}