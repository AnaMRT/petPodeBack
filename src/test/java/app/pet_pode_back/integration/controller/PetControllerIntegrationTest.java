package app.pet_pode_back.integration.controller;



import app.pet_pode_back.dto.PetUpdateDTO;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:.env.test")
@AutoConfigureMockMvc
public class PetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetRepository petRepository;

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
        petRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNome("Teste");
        usuario.setEmail("teste@teste.com");
        usuario.setSenha("123456");
        usuario = usuarioRepository.save(usuario);

        token = "Bearer " + jwtUtil.gerarToken(usuario.getId());
    }

    @Test
    void deveCadastrarPetComSucesso() throws Exception {
        Pet pet = new Pet();
        pet.setNome("Rex");
        pet.setEspecie("Cachorro");

        mockMvc.perform(post("/pet")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Rex"))
                .andExpect(jsonPath("$.especie").value("Cachorro"));
    }

    @Test
    void deveListarPetsDoUsuario() throws Exception {
        Pet pet1 = new Pet(null, "Rex", "Cachorro", usuario);
        Pet pet2 = new Pet(null, "Miau", "Gato", usuario);
        petRepository.saveAll(List.of(pet1, pet2));

        mockMvc.perform(get("/pet")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome", anyOf(is("Rex"), is("Miau"))));
    }

    @Test
    void deveEditarPetComSucesso() throws Exception {
        Pet pet = new Pet(null, "Rex", "Cachorro", usuario);
        pet = petRepository.save(pet);

        PetUpdateDTO dto = new PetUpdateDTO("Rexie", "Cachorro");

        mockMvc.perform(put("/pet/{id}", pet.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Rexie"));
    }

    @Test
    void deveExcluirPetComSucesso() throws Exception {
        Pet pet = new Pet(null, "Rex", "Cachorro", usuario);
        pet = petRepository.save(pet);

        mockMvc.perform(delete("/pet/{id}", pet.getId())
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }


    @Test
    void naoDeveCadastrarPetSemNomeValido() throws Exception {
        Pet pet = new Pet();
        pet.setNome("R"); // Menor que o mínimo 2
        pet.setEspecie("Cachorro");

        mockMvc.perform(post("/pet")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pet)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("O nome deve ter entre 2 e 100 caracteres")));
    }

    @Test
    void naoDeveEditarPetQueNaoExiste() throws Exception {
        PetUpdateDTO dto = new PetUpdateDTO("NovoNome", "Cachorro");

        mockMvc.perform(put("/pet/{id}", UUID.randomUUID())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem", containsString("Pet não encontrado")));
    }

    @Test
    void naoDeveExcluirPetQueNaoExiste() throws Exception {
        mockMvc.perform(delete("/pet/{id}", UUID.randomUUID())
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem", containsString("Pet não encontrado")));
    }

    @Test
    void naoDeveAcessarSemToken() throws Exception {
        mockMvc.perform(get("/pet"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("O header Authorization é obrigatório")));
    }

    @Test
    void naoDeveExcluirPetDeOutroUsuario() throws Exception {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setNome("Outro");
        outroUsuario.setEmail("outro@teste.com");
        outroUsuario.setSenha("123456");
        outroUsuario = usuarioRepository.save(outroUsuario);

        Pet pet = new Pet(null, "Rex", "Cachorro", outroUsuario);
        pet = petRepository.save(pet);

        mockMvc.perform(delete("/pet/{id}", pet.getId())
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem", containsString("Você não tem permissão")));
    }
    @Test
    void naoDeveEditarPetDeOutroUsuario() throws Exception {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setNome("Outro");
        outroUsuario.setEmail("outro@teste.com");
        outroUsuario.setSenha("123456");
        outroUsuario = usuarioRepository.save(outroUsuario);

        Pet pet = new Pet(null, "Rex", "Cachorro", outroUsuario);
        pet = petRepository.save(pet);

        PetUpdateDTO dto = new PetUpdateDTO("NovoNome", "Cachorro");

        mockMvc.perform(put("/pet/{id}", pet.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem", containsString("Você não tem permissão")));
    }

}
