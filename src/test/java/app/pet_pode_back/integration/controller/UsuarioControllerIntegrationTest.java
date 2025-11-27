package app.pet_pode_back.integration.controller;

import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.exception.SemPermissaoException;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.service.UsuarioService;
import app.pet_pode_back.security.JwtUtil;
import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.TestInstance;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:.env.exemplo.test")
@AutoConfigureMockMvc
public class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private Cloudinary cloudinary;

    private UUID usuarioId;
    private String token;

    @BeforeEach
    void setup() {
        usuarioId = UUID.randomUUID();
        token = "Bearer exemploToken";
    }

    // ------------------------------------------------------------
    // GET /usuario
    // ------------------------------------------------------------
    @Test
    void deveListarUsuarios() throws Exception {
        Usuario u = new Usuario();
        u.setId(usuarioId);

        when(usuarioService.listarTodos()).thenReturn(List.of(u));

        mockMvc.perform(get("/usuario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(usuarioId.toString()));
    }

    // ------------------------------------------------------------
    // PUT /usuario (happy path)
    // ------------------------------------------------------------
    @Test
    void deveEditarUsuario() throws Exception {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("Rafa", "teste@teste.com", "123", "123", "1234");
        Usuario atualizado = new Usuario();
        atualizado.setId(usuarioId);

        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);
        when(usuarioService.editarUsuario(any(), any())).thenReturn(atualizado);

        mockMvc.perform(put("/usuario")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "Rafa",
                                    "email": "teste@teste.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuarioId.toString()));
    }

    // ------------------------------------------------------------
    // PUT /usuario — token inválido (cai no seu ControllerAdvice)
    // ------------------------------------------------------------
    @Test
    void deveRetornar403QuandoTokenInvalidoNoEditar() throws Exception {
        when(jwtUtil.extrairUsuarioId(anyString()))
                .thenThrow(new SemPermissaoException("Token inválido ou expirado"));

        mockMvc.perform(put("/usuario")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Rafa", "email": "teste@teste.com"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Token inválido ou expirado"))
                .andExpect(jsonPath("$.codigo").value(403))
                .andExpect(jsonPath("$.path").value("/usuario"));
    }

    // ------------------------------------------------------------
    // PUT /usuario — DTO inválido
    // ------------------------------------------------------------
    @Test
    void deveRetornar400QuandoDTOInvalido() throws Exception {
        // JWT válido
        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);

        mockMvc.perform(put("/usuario")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "email": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400))
                .andExpect(jsonPath("$.mensagem").exists())
                .andExpect(jsonPath("$.path").value("/usuario"));
    }

    // ------------------------------------------------------------
    // DELETE /usuario
    // ------------------------------------------------------------
    @Test
    void deveRemoverUsuario() throws Exception {
        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);

        mockMvc.perform(delete("/usuario")
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar403QuandoTokenInvalidoNoDelete() throws Exception {
        when(jwtUtil.extrairUsuarioId(anyString()))
                .thenThrow(new SemPermissaoException("Token inválido ou expirado"));

        mockMvc.perform(delete("/usuario")
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Token inválido ou expirado"))
                .andExpect(jsonPath("$.codigo").value(403))
                .andExpect(jsonPath("$.path").value("/usuario"));
    }


    @Test
    void deveRetornarUsuarioLogado() throws Exception {
        when(usuarioService.getUsuarioLogado(anyString()))
                .thenReturn(Map.of("id", usuarioId.toString()));

        mockMvc.perform(get("/usuario/logado")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuarioId.toString()));
    }
}
