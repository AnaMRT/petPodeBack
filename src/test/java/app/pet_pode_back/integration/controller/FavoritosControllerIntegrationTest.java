package app.pet_pode_back.integration.controller;

import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.security.JwtUtil;
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
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:.env.test")
public class FavoritosControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PlantaRepository plantasRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Usuario usuario;
    private Plantas planta;
    private String tokenValido;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNome("Teste");
        usuario.setEmail("teste@email.com");
        usuario.setSenha("123456");
        usuario = usuarioRepository.save(usuario);

        planta = new Plantas();
        planta.setNomePopular("Planta Teste");
        planta.setNomeCientifico("Planta Teste");
        planta.setToxicaParaCaninos(true);
        planta.setToxicaParaFelinos(true);
        planta = plantasRepository.save(planta);

        tokenValido = "Bearer " + jwtUtil.gerarToken(usuario.getId());
    }

    @Test
    void deveAdicionarFavoritoComSucesso() throws Exception {
        mockMvc.perform(put("/favoritos/" + planta.getId())
                        .header("Authorization", tokenValido))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornarErro400QuandoTokenNaoEnviadoNoAdicionar() throws Exception {
        mockMvc.perform(put("/favoritos/" + planta.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O header Authorization é obrigatório."));
    }

    @Test
    void deveRetornarErro400QuandoTokenForInvalidoNoAdicionar() throws Exception {
        mockMvc.perform(put("/favoritos/" + planta.getId())
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Token inválido."));
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExistirNoAdicionar() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        String token = "Bearer " + jwtUtil.gerarToken(idInexistente);

        mockMvc.perform(put("/favoritos/" + planta.getId())
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }


    @Test
    void deveRemoverFavoritoComSucesso() throws Exception {
        mockMvc.perform(put("/favoritos/" + planta.getId())
                .header("Authorization", tokenValido));

        mockMvc.perform(delete("/favoritos/" + planta.getId())
                        .header("Authorization", tokenValido))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornarErro400QuandoTokenNaoEnviadoNoRemover() throws Exception {
        mockMvc.perform(delete("/favoritos/" + planta.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O header Authorization é obrigatório."));
    }

    @Test
    void deveRetornarErro400QuandoTokenForInvalidoNoRemover() throws Exception {
        mockMvc.perform(delete("/favoritos/" + planta.getId())
                        .header("Authorization", "Bearer 123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Token inválido."));
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExistirNoRemover() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        String token = "Bearer " + jwtUtil.gerarToken(idInexistente);

        mockMvc.perform(delete("/favoritos/" + planta.getId())
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }


    @Test
    void deveListarFavoritosComSucesso() throws Exception {

        mockMvc.perform(put("/favoritos/" + planta.getId())
                .header("Authorization", tokenValido));

        mockMvc.perform(get("/favoritos")
                        .header("Authorization", tokenValido)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(planta.getId().toString()))
                .andExpect(jsonPath("$[0].nomePopular").value("Planta Teste"));
    }


    @Test
    void deveRetornarErro400QuandoTokenNaoEnviadoNoListar() throws Exception {
        mockMvc.perform(get("/favoritos"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O header Authorization é obrigatório."));
    }

    @Test
    void deveRetornarErro400QuandoTokenForInvalidoNoListar() throws Exception {
        mockMvc.perform(get("/favoritos")
                        .header("Authorization", "Bearer abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Token inválido."));
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExistirNoListar() throws Exception {
        UUID idQueNaoExiste = UUID.randomUUID();
        String token = "Bearer " + jwtUtil.gerarToken(idQueNaoExiste);

        mockMvc.perform(get("/favoritos")
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }
}