package app.pet_pode_back.integration.controller;

import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.security.JwtUtil;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource("classpath:.env.test")
@AutoConfigureMockMvc
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;


    private Usuario usuario;
    private String token;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNome("Rafa");
        usuario.setEmail("teste@teste.com");
        usuario.setSenha("123456");
        usuario = usuarioRepository.save(usuario);

        token = "Bearer " + jwtUtil.gerarToken(usuario.getId());
    }




    @Test
    void deveEditarUsuario() throws Exception {
        mockMvc.perform(put("/usuario")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Novo Nome",
                                  "email": "novoemail@teste.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuario.getId().toString()));

        Usuario atualizado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assert atualizado.getNome().equals("Novo Nome");
        assert atualizado.getEmail().equals("novoemail@teste.com");
    }


    @Test
    void deveRetornar400DTOInvalido() throws Exception {
        mockMvc.perform(put("/usuario")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "",
                                  "email": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400))
                .andExpect(jsonPath("$.path").value("/usuario"));
    }


    @Test
    void deveRemoverUsuario() throws Exception {
        mockMvc.perform(delete("/usuario")
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        assert usuarioRepository.count() == 0;
    }


    @Test
    void deveRetornarUsuarioLogado() throws Exception {
        mockMvc.perform(get("/usuario/logado")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuario.getId().toString()));
    }

    @Test
    void deveRetornar404QuandoEditarUsuarioInexistente() throws Exception {
        UUID idInexistente = UUID.randomUUID();

        mockMvc.perform(put("/usuario")
                        .header("Authorization", "Bearer " + jwtUtil.gerarToken(idInexistente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nome": "Novo Nome",
                              "email": "novo@email.com"
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }







    @Test
    void deveRetornar404AoEditarUsuarioInexistente() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(jwtUtil.extrairUsuarioId("tokenInexistente")).thenReturn(idInexistente);

        mockMvc.perform(put("/usuario")
                        .header("Authorization", "Bearer tokenInexistente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Novo Nome",
                                  "email": "novo@email.com"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }

    @Test
    void deveRetornar404AoBuscarUsuarioLogadoInexistente() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(jwtUtil.extrairUsuarioId("tokenInexistente")).thenReturn(idInexistente);

        mockMvc.perform(get("/usuario/logado")
                        .header("Authorization", "Bearer tokenInexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }




}
