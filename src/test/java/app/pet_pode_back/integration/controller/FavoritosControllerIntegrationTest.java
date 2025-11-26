package app.pet_pode_back.integration.controller;

import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.service.FavoritosService;
import app.pet_pode_back.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FavoritosControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FavoritosService favoritosService;

    private UUID usuarioId;
    private UUID plantaId;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        plantaId = UUID.randomUUID();
        // Mock para retornar o usuárioId quando o JWT for chamado
        when(jwtUtil.extrairUsuarioId("tokenValido")).thenReturn(usuarioId);
    }

    @Test
    void deveAdicionarFavorito() throws Exception {
        mockMvc.perform(put("/favoritos/" + plantaId)
                        .header("Authorization", "Bearer tokenValido"))
                .andExpect(status().isOk());
    }

    @Test
    void deveRemoverFavorito() throws Exception {
        mockMvc.perform(delete("/favoritos/" + plantaId)
                        .header("Authorization", "Bearer tokenValido"))
                .andExpect(status().isOk());
    }

    @Test
    void deveListarFavoritos() throws Exception {
        Plantas planta = new Plantas();
        planta.setId(plantaId);
        planta.setNomePopular("Planta Teste");

        when(favoritosService.listarFavoritos(usuarioId)).thenReturn(Set.of(planta));

        mockMvc.perform(get("/favoritos")
                        .header("Authorization", "Bearer tokenValido")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(plantaId.toString()))
                .andExpect(jsonPath("$[0].nomePopular").value("Planta Teste"));
    }

    @Test
    void deveRetornar401QuandoTokenForInvalido() throws Exception {
        mockMvc.perform(put("/favoritos/" + plantaId)
                        .header("Authorization", "Bearer token_invalido"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremFavoritos() throws Exception {
        when(favoritosService.listarFavoritos(usuarioId)).thenReturn(Set.of());

        mockMvc.perform(get("/favoritos")
                        .header("Authorization", "Bearer tokenValido")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void deveRetornar404QuandoUsuarioDoTokenNaoExistir() throws Exception {
        when(jwtUtil.extrairUsuarioId("tokenValido")).thenReturn(usuarioId);

        doThrow(new RegistroNaoEncontradoException("Usuário não encontrado"))
                .when(favoritosService)
                .adicionarFavorito(usuarioId, plantaId);

        mockMvc.perform(put("/favoritos/" + plantaId)
                        .header("Authorization", "Bearer tokenValido"))
                .andExpect(status().isNotFound());
    }


}
