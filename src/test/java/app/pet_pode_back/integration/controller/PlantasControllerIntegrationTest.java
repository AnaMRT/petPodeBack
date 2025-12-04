package app.pet_pode_back.integracao;

import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource("classpath:.env.test")
@AutoConfigureMockMvc
class PlantasControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlantaRepository plantaRepository;

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
        plantaRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNome("Rafa");
        usuario.setEmail("rafa@test.com");
        usuario.setSenha("123456");

        Pet pet = new Pet();
        pet.setNome("Luna");
        pet.setEspecie("Canino");
        pet.setUsuario(usuario);
        usuario.setPets(List.of(pet));

        usuario = usuarioRepository.save(usuario);

        token = "Bearer " + jwtUtil.gerarToken(usuario.getId());
    }


    @Test
    void deveListarPlantas() throws Exception {
        Plantas p = new Plantas(
                null,
                "Costela de Adão",
                "Monstera deliciosa",
                "Planta de interior muito popular",
                false,
                false,
                null
        );
        plantaRepository.save(p);

        mockMvc.perform(get("/plantas")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomePopular").value("Costela de Adão"));
    }



    @Test
    void deveBuscarPlantasComTokenValido() throws Exception {
        Plantas p = new Plantas(
                null,
                "Azaleia",
                "Rhododendron",
                "Descrição",
                true,
                false,
                null
        );
        plantaRepository.save(p);

        mockMvc.perform(get("/plantas/search")
                        .param("termo", "azal")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomePopular").value("Azaleia"));
    }


    @Test
    void deveRetornarBadRequestQuandoHeaderAuthorizationAusente() throws Exception {
        mockMvc.perform(get("/plantas/search")
                        .param("termo", "qualquer"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O header Authorization é obrigatório."));
    }



    @Test
    void deveBuscarMesmoSeUsuarioNaoTemPet() throws Exception {
        // salva uma planta que não bate com nada
        plantaRepository.save(new Plantas(
                null,
                "Rosa",
                "Rosa spp.",
                "Descrição",
                false,
                false,
                null
        ));

        usuario.setPets(List.of());
        usuarioRepository.save(usuario);

        mockMvc.perform(get("/plantas/search")
                        .param("termo", "rosa")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deveListarPlantasVazia() throws Exception {
        plantaRepository.deleteAll();

        mockMvc.perform(get("/plantas")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExistirAoBuscarPlantas() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        String tokenFake = "Bearer " + jwtUtil.gerarToken(idInexistente);

        mockMvc.perform(get("/plantas/search")
                        .param("termo", "rosa")
                        .header("Authorization", tokenFake)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }
    @Test
    void deveBuscarPlantasComTermoParcial() throws Exception {
        Plantas p = new Plantas(
                null,
                "Azaleia",
                "Rhododendron",
                "Descrição",
                true,
                false,
                null
        );
        plantaRepository.save(p);

        // Busca usando parte do nome: "azal"
        mockMvc.perform(get("/plantas/search")
                        .param("termo", "azal")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomePopular").value("Azaleia"));
    }

    @Test
    void deveBuscarIgnorandoAcentuacao() throws Exception {
        // Salva planta com acento
        Plantas p = new Plantas(
                null,
                "Costela-de-Adão",
                "Monstera deliciosa",
                "Descrição",
                false,
                false,
                null
        );
        plantaRepository.save(p);

        // Busca sem acento: "adao"
        mockMvc.perform(get("/plantas/search")
                        .param("termo", "adao")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomePopular").value("Costela-de-Adão"));
    }

}
