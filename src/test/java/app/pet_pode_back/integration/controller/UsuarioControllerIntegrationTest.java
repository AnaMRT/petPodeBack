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

    // JwtUtil real — apenas injetamos
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

        // token REAL gerado pelo JwtUtil
        token = "Bearer " + jwtUtil.gerarToken(usuario.getId());
    }



    @Test
    void deveListarUsuarios() throws Exception {
        mockMvc.perform(get("/usuario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(usuario.getId().toString()));
    }

    // ----------------------------------------------------
    // PUT /usuario (happy path)
    // ----------------------------------------------------
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

    // ----------------------------------------------------
    // PUT /usuario — DTO inválido
    // ----------------------------------------------------
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

    // ----------------------------------------------------
    // DELETE /usuario
    // ----------------------------------------------------
    @Test
    void deveRemoverUsuario() throws Exception {
        mockMvc.perform(delete("/usuario")
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        assert usuarioRepository.count() == 0;
    }

    // ----------------------------------------------------
    // GET /usuario/logado
    // ----------------------------------------------------
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
        String tokenInexistente = "Bearer " + jwtUtil.gerarToken(idInexistente);

        mockMvc.perform(put("/usuario")
                        .header("Authorization", tokenInexistente)
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
    void deveRetornar409AoEditarUsuarioComEmailDuplicado() throws Exception {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setNome("Outro");
        outroUsuario.setEmail("outro@email.com");
        outroUsuario.setSenha("123456");
        usuarioRepository.save(outroUsuario);

        mockMvc.perform(put("/usuario")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "Rafaela",
                                    "email": "outro@email.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem")
                        .value("Operação inválida: já existe um registro com esses dados."));
    }

    @Test
    void deveRetornar404AoBuscarUsuarioLogadoInexistente() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        String tokenInexistente = "Bearer " + jwtUtil.gerarToken(idInexistente);

        mockMvc.perform(get("/usuario/logado")
                        .header("Authorization", tokenInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }


}
