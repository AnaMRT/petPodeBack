package app.pet_pode_back.integration.controller;

import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PetRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:.env.exemplo.test")
@AutoConfigureMockMvc
public class PetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    private Usuario usuario;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        petRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNome("Test User");
        usuario.setEmail("test@email.com");
        usuario.setSenha("123456");
        usuarioRepository.save(usuario);

        usuarioId = usuario.getId();

        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);
    }

    @Test
    void deveCadastrarPet() throws Exception {
        Pet pet = new Pet();
        pet.setNome("Rex");
        pet.setEspecie("Cachorro");

        mockMvc.perform(post("/pet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer qualquer-token")
                        .content(objectMapper.writeValueAsString(pet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Rex"))
                .andExpect(jsonPath("$.especie").value("Cachorro"));
    }

    @Test
    void deveListarPetsDoUsuario() throws Exception {
        Pet pet = new Pet();
        pet.setNome("Rex");
        pet.setEspecie("Cachorro");
        pet.setUsuario(usuario);
        petRepository.save(pet);

        mockMvc.perform(get("/pet")
                        .header("Authorization", "Bearer qualquer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Rex"));
    }

    @Test
    void deveEditarPet() throws Exception {
        Pet pet = new Pet();
        pet.setNome("Rex");
        pet.setEspecie("Cachorro");
        pet.setUsuario(usuario);
        petRepository.save(pet);

        Pet dto = new Pet();
        dto.setNome("Max");
        dto.setEspecie("Gato");

        mockMvc.perform(put("/pet/" + pet.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer qualquer-token")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Max"))
                .andExpect(jsonPath("$.especie").value("Gato"));
    }

    @Test
    void deveExcluirPet() throws Exception {
        Pet pet = new Pet();
        pet.setNome("Rex");
        pet.setEspecie("Cachorro");
        pet.setUsuario(usuario);
        petRepository.save(pet);

        mockMvc.perform(delete("/pet/" + pet.getId())
                        .header("Authorization", "Bearer qualquer-token"))
                .andExpect(status().isNoContent());
    }

    // Sad path: tentar editar pet que não existe
    @Test
    void deveFalharAoEditarPetInexistente() throws Exception {
        Pet dto = new Pet();
        dto.setNome("Max");

        mockMvc.perform(put("/pet/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer qualquer-token")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // Sad path: tentar excluir pet que não pertence ao usuário
    @Test
    void deveFalharExcluirPetDeOutroUsuario() throws Exception {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setNome("Outro");
        outroUsuario.setEmail("outro@email.com");
        outroUsuario.setSenha("123");
        usuarioRepository.save(outroUsuario);

        Pet pet = new Pet();
        pet.setNome("Rex");
        pet.setEspecie("Cachorro");
        pet.setUsuario(outroUsuario);
        petRepository.save(pet);

        mockMvc.perform(delete("/pet/" + pet.getId())
                        .header("Authorization", "Bearer qualquer-token"))
                .andExpect(status().isForbidden());
    }

    // Sad path: header Authorization ausente
    @Test
    void deveFalharSemHeader() throws Exception {
        mockMvc.perform(get("/pet"))
                .andExpect(status().isBadRequest());
    }
}
