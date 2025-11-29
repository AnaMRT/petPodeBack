package app.pet_pode_back.unit.controller;

import app.pet_pode_back.controller.UsuarioController;
import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.exception.handler.RestExceptionHandler;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.service.UsuarioService;
import app.pet_pode_back.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;


class UsuarioControllerTest {

    @InjectMocks
    private UsuarioController usuarioController;
    @Mock
    private UsuarioService usuarioService;

    @Mock
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }




    @Test
    void deveEditarUsuario_CenarioSucesso() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNome("Novo Nome");
        dto.setEmail("novo@email.com");

        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(usuarioId);
        usuarioMock.setNome("Novo Nome");
        usuarioMock.setEmail("novo@email.com");

        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);
        when(usuarioService.editarUsuario(eq(usuarioId), any(UsuarioUpdateDTO.class)))
                .thenReturn(usuarioMock);

        mockMvc.perform(put("/usuario")
                        .header("Authorization", "Bearer tokenQualquer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"))
                .andExpect(jsonPath("$.email").value("novo@email.com"));
    }

    @Test
    void deveRetornar404QuandoEditarUsuarioInexistente() throws Exception {
        UUID usuarioId = UUID.randomUUID();

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNome("Nome");
        dto.setEmail("email@email.com");
        dto.setSenha("123456");
        dto.setSenhaAtual("123456");
        dto.setConfirmarSenha("123456");

        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);

        doThrow(new RegistroNaoEncontradoException("Usuário não encontrado"))
                .when(usuarioService).editarUsuario(eq(usuarioId), any());

        mockMvc.perform(put("/usuario")
                        .header("Authorization", "Bearer tokenQualquer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404))
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }

    @Test
    void deveRemoverUsuario_CenarioSucesso() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);

        // não precisa stub do usuarioService.remover se for caminho feliz (não lança)
        mockMvc.perform(delete("/usuario")
                        .header("Authorization", "Bearer tokenQualquer"))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).remover(usuarioId);
    }

    @Test
    void deveRetornarUsuarioLogado() throws Exception {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nome", "Patapin");

        when(usuarioService.getUsuarioLogado("abc")).thenReturn(usuario);

        mockMvc.perform(get("/usuario/logado")
                        .header("Authorization", "Bearer abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Patapin"));
    }
    @Test
    void deveRetornar500QuandoErroInesperadoAoEditar() throws Exception {
        UUID usuarioId = UUID.randomUUID();

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNome("Teste");
        dto.setEmail("email@test.com");
        dto.setSenhaAtual("123456");
        dto.setSenha("1234566");
        dto.setConfirmarSenha("1234566");

        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);

        when(usuarioService.editarUsuario(eq(usuarioId), any()))
                .thenThrow(new RuntimeException("Erro inesperado"));

        mockMvc.perform(put("/usuario")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Erro inesperado. Tente novamente."));
    }

    @Test
    void deveRetornar403QuandoTokenInvalidoNoDelete() throws Exception {
        when(jwtUtil.extrairUsuarioId(anyString()))
                .thenThrow(new IllegalArgumentException("Token inválido"));

        mockMvc.perform(delete("/usuario")
                        .header("Authorization", "Bearer errado"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Token inválido ou expirado"));
    }

    @Test
    void deveRetornar404QuandoRemoverUsuarioInexistente() throws Exception {
        UUID usuarioId = UUID.randomUUID();

        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);
        Mockito.doThrow(new RegistroNaoEncontradoException("Usuário não encontrado"))
                .when(usuarioService).remover(usuarioId);

        mockMvc.perform(delete("/usuario")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }

    @Test
    void deveRetornar500QuandoErroInesperadoAoDeletar() throws Exception {
        UUID usuarioId = UUID.randomUUID();

        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);
        Mockito.doThrow(new RuntimeException("Falhou"))
                .when(usuarioService).remover(usuarioId);

        mockMvc.perform(delete("/usuario")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Erro inesperado. Tente novamente."));
    }

    @Test
    void deveRetornar404QuandoUsuarioLogadoNaoExiste() throws Exception {
        UUID usuarioId = UUID.randomUUID();

        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);
        when(usuarioService.getUsuarioLogado(anyString()))
                .thenThrow(new RegistroNaoEncontradoException("Usuário não encontrado"));

        mockMvc.perform(get("/usuario/logado")
                        .header("Authorization", "Bearer tokenValido"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }


    @Test
    void deveRetornar403QuandoTokenInvalidoNoDelete_2() throws Exception {
        when(jwtUtil.extrairUsuarioId(anyString()))
                .thenThrow(new IllegalArgumentException("Token inválido"));

        mockMvc.perform(delete("/usuario")
                        .header("Authorization", "Bearer invalido"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Token inválido ou expirado"));
    }

    @Test
    void deveRetornar409QuandoErroDeIntegridadeAoDeletar() throws Exception {
        UUID usuarioId = UUID.randomUUID();

        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);
        doThrow(new DataIntegrityViolationException("duplicado"))
                .when(usuarioService).remover(usuarioId);

        mockMvc.perform(delete("/usuario")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem")
                        .value("Operação inválida: já existe um registro com esses dados."));
    }

    @Test
    void deveRetornar500QuandoFalhaAoProcessarImagem() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);

        when(usuarioService.atualizarImagemUsuario(eq(usuarioId), any()))
                .thenThrow(new IOException("Falha IO"));

        MockMultipartFile arquivo = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", "123".getBytes()
        );

        mockMvc.perform(multipart("/usuario/imagem")
                        .file(arquivo)
                        .with(r -> { r.setMethod("PUT"); return r; })
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Erro inesperado. Tente novamente."));
    }

    @Test
    void deveAtualizarImagemDoUsuario() throws Exception {
        UUID usuarioId = UUID.randomUUID();

        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);
        when(usuarioService.atualizarImagemUsuario(eq(usuarioId), any()))
                .thenReturn("http://foto.com/img.jpg");

        MockMultipartFile arquivo = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", "conteudo".getBytes()
        );

        mockMvc.perform(multipart("/usuario/imagem")
                        .file(arquivo)
                        .with(request -> {
                            request.setMethod("PUT"); // 👈 NECESSÁRIO para PUT
                            return request;
                        })
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagemUrl").value("http://foto.com/img.jpg"));
    }


}

