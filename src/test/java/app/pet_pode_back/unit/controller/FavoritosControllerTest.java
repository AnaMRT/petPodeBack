package app.pet_pode_back.unit.controller;

import app.pet_pode_back.controller.FavoritosController;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.exception.handler.RestExceptionHandler;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.service.FavoritosService;
import app.pet_pode_back.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FavoritosControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FavoritosService favoritosService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private FavoritosController favoritosController;

    private UUID usuarioId;
    private UUID plantaId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(favoritosController)
                .setControllerAdvice(new RestExceptionHandler())
                .build();

        usuarioId = UUID.randomUUID();
        plantaId = UUID.randomUUID();
    }


    @Test
    void deveAdicionarFavoritoComSucesso() throws Exception {
        when(jwtUtil.extrairUsuarioId("token123")).thenReturn(usuarioId);

        mockMvc.perform(put("/favoritos/{id}", plantaId)
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk());

        verify(favoritosService).adicionarFavorito(usuarioId, plantaId);
    }

    @Test
    void deveRetornarErro400QuandoTokenNaoEnviadoNoAdicionar() throws Exception {
        mockMvc.perform(put("/favoritos/{id}", plantaId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExisteNoAdicionar() throws Exception {
        when(jwtUtil.extrairUsuarioId("tokenXYZ")).thenReturn(usuarioId);
        doThrow(new RegistroNaoEncontradoException("Usuário não encontrado"))
                .when(favoritosService).adicionarFavorito(usuarioId, plantaId);

        mockMvc.perform(put("/favoritos/{id}", plantaId)
                        .header("Authorization", "Bearer tokenXYZ"))
                .andExpect(status().isNotFound());
    }


    @Test
    void deveRemoverFavoritoComSucesso() throws Exception {
        when(jwtUtil.extrairUsuarioId("tokenABC")).thenReturn(usuarioId);

        mockMvc.perform(delete("/favoritos/{id}", plantaId)
                        .header("Authorization", "Bearer tokenABC"))
                .andExpect(status().isOk());

        verify(favoritosService).removerFavorito(usuarioId, plantaId);
    }

    @Test
    void deveRetornarErro400QuandoTokenNaoEnviadoNoRemover() throws Exception {
        mockMvc.perform(delete("/favoritos/{id}", plantaId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExisteNoRemover() throws Exception {
        when(jwtUtil.extrairUsuarioId("tok123")).thenReturn(usuarioId);
        doThrow(new RegistroNaoEncontradoException("Usuário não encontrado"))
                .when(favoritosService).removerFavorito(usuarioId, plantaId);

        mockMvc.perform(delete("/favoritos/{id}", plantaId)
                        .header("Authorization", "Bearer tok123"))
                .andExpect(status().isNotFound());
    }


    @Test
    void deveListarFavoritosComSucesso() throws Exception {
        Set<Plantas> favoritos = new HashSet<>();
        Plantas p = new Plantas();
        p.setId(plantaId);
        p.setNomePopular("Rosa");
        favoritos.add(p);

        when(jwtUtil.extrairUsuarioId("tok")).thenReturn(usuarioId);
        when(favoritosService.listarFavoritos(usuarioId)).thenReturn(favoritos);

        mockMvc.perform(get("/favoritos")
                        .header("Authorization", "Bearer tok")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomePopular").value("Rosa"));
    }

    @Test
    void deveRetornarErro400QuandoTokenNaoEnviadoNoListar() throws Exception {
        mockMvc.perform(get("/favoritos"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExisteNoListar() throws Exception {
        when(jwtUtil.extrairUsuarioId("tok3")).thenReturn(usuarioId);
        doThrow(new RegistroNaoEncontradoException("Usuário não encontrado"))
                .when(favoritosService).listarFavoritos(usuarioId);

        mockMvc.perform(get("/favoritos")
                        .header("Authorization", "Bearer tok3"))
                .andExpect(status().isNotFound());
    }
}
