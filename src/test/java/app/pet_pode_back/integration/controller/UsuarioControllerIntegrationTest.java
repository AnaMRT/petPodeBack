package app.pet_pode_back.integration.controller;

import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

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

    @Autowired
    private ObjectMapper objectMapper;

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
    void deveEditarUsuarioComSucesso() throws Exception {
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
    void deveFalharEditarUsuarioComTokenInvalido() throws Exception {
        String json = """
                {
                  "nome": "NovoNome",
                  "email": "novo@teste.com"
                }
                """;

        mockMvc.perform(put("/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer tokeninvalido")
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Token inválido."));
    }

    @Test
    void deveRetornar409AoEditarUsuarioComEmailDuplicado() throws Exception {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setNome("Outro");
        outroUsuario.setEmail("outro@teste.com");
        outroUsuario.setSenha("123456");
        usuarioRepository.save(outroUsuario);

        String json = """
                {
                  "nome": "Rafa",
                  "email": "outro@teste.com"
                }
                """;

        mockMvc.perform(put("/usuario")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem")
                        .value("Operação inválida: já existe um registro com esses dados."));
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
    void deveRemoverUsuarioComSucesso() throws Exception {
        mockMvc.perform(delete("/usuario")
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        assert usuarioRepository.count() == 0;
    }

    @Test
    void deveFalharRemoverUsuarioComTokenInvalido() throws Exception {
        mockMvc.perform(delete("/usuario")
                        .header("Authorization", "Bearer tokeninvalido"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Token inválido."));
    }

    @Test
    void deveRetornar404AoRemoverUsuarioInexistente() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        String tokenInexistente = "Bearer " + jwtUtil.gerarToken(idInexistente);

        mockMvc.perform(delete("/usuario")
                        .header("Authorization", tokenInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }


    @Test
    void deveRetornarUsuarioLogado() throws Exception {
        mockMvc.perform(get("/usuario/logado")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuario.getId().toString()));
    }

    @Test
    void deveRetornar404QuandoUsuarioLogadoNaoExiste() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        String tokenInexistente = "Bearer " + jwtUtil.gerarToken(idInexistente);

        mockMvc.perform(get("/usuario/logado")
                        .header("Authorization", tokenInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }


}