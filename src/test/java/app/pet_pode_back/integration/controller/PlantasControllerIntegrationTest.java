package app.pet_pode_back.integration.controller;

import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.service.PlantaService;
import app.pet_pode_back.service.UsuarioService;
import app.pet_pode_back.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PlantasControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlantaService plantaService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Plantas planta;
    private Usuario usuario;
    private Pet pet;

    @BeforeEach
    public void setup() {
        planta = new Plantas();
        planta.setId(UUID.randomUUID());
        planta.setNomePopular("Rosa");
        planta.setNomeCientifico("Rosa spp.");
        planta.setDescricao("Planta bonita");
        planta.setToxicaParaCaninos(false);
        planta.setToxicaParaFelinos(false);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        pet = new Pet();
        pet.setId(UUID.randomUUID());

        usuario.setPets(List.of(pet));
    }

    @Test
    public void deveListarPlantas() throws Exception {
        Mockito.when(plantaService.listarTodos()).thenReturn(List.of(planta));

        mockMvc.perform(get("/plantas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomePopular").value("Rosa"));
    }


    @Test
    public void deveBuscarPlantasComToken() throws Exception {
        Mockito.when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuario.getId());
        Mockito.when(usuarioService.buscarUsuarioPorId(usuario.getId())).thenReturn(usuario);
        Mockito.when(plantaService.buscarPlantas(anyString(), any(Pet.class))).thenReturn(List.of(planta));

        mockMvc.perform(get("/plantas/search")
                        .header("Authorization", "Bearer tokenFake")
                        .param("termo", "Rosa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomePopular").value("Rosa"));
    }

}
