package app.pet_pode_back.unit.controller;

import app.pet_pode_back.controller.AuthController;
import app.pet_pode_back.dto.LoginRequest;
import app.pet_pode_back.dto.ResetPasswordDTO;
import app.pet_pode_back.exception.ParametroInvalidoException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class AuthControllerTest {

    @InjectMocks
    private AuthController controller;

    @Mock
    private AuthService authService;

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail("teste@teste.com");
        usuario.setSenha("senha123");
    }

    @Test
    void deveLogarComSucesso() {
        LoginRequest dto = new LoginRequest();
        dto.setEmail("teste@teste.com");
        dto.setSenha("123456");

        when(authService.login(dto)).thenReturn("TOKEN123");

        ResponseEntity<?> response = controller.login(dto);

        assertEquals(200, response.getStatusCodeValue());
        Map body = (Map) response.getBody();
        assertNotNull(body);
        assertEquals("TOKEN123", body.get("token"));

        verify(authService).login(dto);
    }

    @Test
    void deveFalharAoLogarComCamposInvalidos() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void deveFalharAoLogarComCredenciaisInvalidas() {
        LoginRequest dto = new LoginRequest();
        dto.setEmail("teste@teste.com");
        dto.setSenha("errada");

        when(authService.login(dto)).thenThrow(new ParametroInvalidoException("Credenciais inválidas"));

        ParametroInvalidoException ex = assertThrows(
                ParametroInvalidoException.class,
                () -> controller.login(dto)
        );
        assertEquals("Credenciais inválidas", ex.getMessage());
        verify(authService).login(dto);
    }


    @Test
    void deveRegistrarUsuarioComSucesso() {
        Usuario novo = new Usuario();
        novo.setEmail("novo@teste.com");
        novo.setSenha("123");

        when(authService.registrar(novo)).thenReturn("TOKEN_REG");

        ResponseEntity<?> response = controller.register(novo);

        assertEquals(200, response.getStatusCodeValue());
        Map body = (Map) response.getBody();
        assertNotNull(body);
        assertEquals("TOKEN_REG", body.get("token"));

        verify(authService).registrar(novo);
    }

    @Test
    void deveFalharAoRegistrarComCamposInvalidos() throws Exception {
        Usuario request = new Usuario();
        request.setNome("");
        request.setEmail("");
        request.setSenha("");

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveFalharAoRegistrarUsuarioComEmailJaExistente() {
        Usuario novo = new Usuario();
        novo.setEmail("teste@teste.com");

        when(authService.registrar(novo)).thenThrow(new ParametroInvalidoException("Email já cadastrado"));

        ParametroInvalidoException ex = assertThrows(
                ParametroInvalidoException.class,
                () -> controller.register(novo)
        );
        assertEquals("Email já cadastrado", ex.getMessage());
        verify(authService).registrar(novo);
    }


    @Test
    void deveEnviarCodigoDeRedefinicaoQuandoEmailValido() {
        String email = "teste@teste.com";

        doNothing().when(authService).solicitarRedefinicaoSenha(email);

        ResponseEntity<String> response = controller.forgotPassword(email);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Código de redefinição de senha enviado por e-mail.", response.getBody());

        verify(authService).solicitarRedefinicaoSenha(email);
    }

    @Test
    void deveFalharAoSolicitarRedefinicaoQuandoEmailNaoExiste() {
        String email = "naoexiste@teste.com";

        doThrow(new RegistroNaoEncontradoException("Usuário não encontrado"))
                .when(authService).solicitarRedefinicaoSenha(email);

        RegistroNaoEncontradoException ex = assertThrows(
                RegistroNaoEncontradoException.class,
                () -> controller.forgotPassword(email)
        );

        assertEquals("Usuário não encontrado", ex.getMessage());
        verify(authService).solicitarRedefinicaoSenha(email);
    }


    @Test
    void deveRedefinirSenhaComSucesso() {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setCodigo("123456");
        dto.setNovaSenha("novaSenha123");

        doNothing().when(authService).redefinirSenha("123456", "novaSenha123");

        ResponseEntity<String> response = controller.resetPassword(dto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Senha redefinida com sucesso.", response.getBody());

        verify(authService).redefinirSenha("123456", "novaSenha123");
    }

    @Test
    void deveFalharAoRedefinirSenhaComCodigoInvalido() {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setCodigo("000000");
        dto.setNovaSenha("nova");

        doThrow(new ParametroInvalidoException("Código inválido."))
                .when(authService).redefinirSenha("000000", "nova");

        ParametroInvalidoException ex = assertThrows(
                ParametroInvalidoException.class,
                () -> controller.resetPassword(dto)
        );

        assertEquals("Código inválido.", ex.getMessage());
        verify(authService).redefinirSenha("000000", "nova");
    }

    @Test
    void deveFalharAoRedefinirSenhaComSenhaInvalida() {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setCodigo("123456");
        dto.setNovaSenha("");

        doThrow(new ParametroInvalidoException("Senha inválida"))
                .when(authService).redefinirSenha("123456", "");

        ParametroInvalidoException ex = assertThrows(
                ParametroInvalidoException.class,
                () -> controller.resetPassword(dto)
        );

        assertEquals("Senha inválida", ex.getMessage());
        verify(authService).redefinirSenha("123456", "");
    }


    @Test
    void deveFalharAoResetarSenhaComCamposInvalidos() throws Exception {
        ResetPasswordDTO request = new ResetPasswordDTO();
        request.setCodigo("");
        request.setNovaSenha("");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}