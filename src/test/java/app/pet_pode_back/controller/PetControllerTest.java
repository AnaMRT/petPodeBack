package app.pet_pode_back.controller;

import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.security.JwtUtil;
import app.pet_pode_back.service.Petservice;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private Petservice petService;

    @InjectMocks
    private PetController petController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Pet pet;
    private Usuario usuario;
    private UUID usuarioId;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        usuarioId = UUID.randomUUID();
        usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNome("João");

        pet = new Pet();
        pet.setNome("Rex");
        pet.setEspecie("Cachorro");
        pet.setUsuario(usuario);

        mockMvc = MockMvcBuilders.standaloneSetup(petController).build();
    }

    @Test
    void cadastrarPetComSucesso() throws Exception {
        String token = JwtUtil.gerarToken(usuarioId);

        when(petService.salvarPet(any(Pet.class), any(UUID.class))).thenReturn(pet);

        mockMvc.perform(post("/pet")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Rex"))
                .andExpect(jsonPath("$.especie").value("Cachorro"));

        verify(petService, times(1)).salvarPet(any(Pet.class), any(UUID.class));
    }



    @Test
    void listarPetsComSucesso() throws Exception {
        List<Pet> listaPets = new ArrayList<>();
        Pet pet1 = new Pet();
        pet1.setId(UUID.randomUUID());
        pet1.setNome("Teste");
        pet1.setEspecie("Canino");
        listaPets.add(pet1);

        when(petService.listarTodos()).thenReturn(listaPets);

        mockMvc.perform(get("/pet")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pet1.getId().toString()))
                .andExpect(jsonPath("$[0].nome").value("Teste"))
                .andExpect(jsonPath("$[0].especie").value("Canino"));

        verify(petService, times(1)).listarTodos();
    }
}
