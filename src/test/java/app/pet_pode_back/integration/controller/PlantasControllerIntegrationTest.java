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
        // limpa tabelas relevantes antes de cada teste
        plantaRepository.deleteAll();
        usuarioRepository.deleteAll();

        // cria usuário e pet (assumindo cascade de persistência em Usuario -> Pet)
        usuario = new Usuario();
        usuario.setNome("Rafa");
        usuario.setEmail("rafa@test.com");
        usuario.setSenha("123456");

        Pet pet = new Pet();
        pet.setNome("Luna");
        pet.setEspecie("Canino");
        pet.setUsuario(usuario);
        usuario.setPets(List.of(pet));

        // salva usuário (e pet se houver cascade)
        usuario = usuarioRepository.save(usuario);

        // token JWT real (JwtUtil.gerarToken deve existir)
        token = "Bearer " + jwtUtil.gerarToken(usuario.getId());
    }

    // ----------------------------
    // GET /plantas (listar)
    // ----------------------------
    @Test
    void deveListarPlantas() throws Exception {
        // usa construtor completo (7 args) da sua entidade Plantas
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

    // ----------------------------
    // POST /plantas (cadastrar)
    // ----------------------------

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

    // ----------------------------
    // GET /plantas/search - se header Authorization ausente -> MissingRequestHeaderException tratada no RestExceptionHandler
    // ----------------------------
    @Test
    void deveRetornarBadRequestQuandoHeaderAuthorizationAusente() throws Exception {
        mockMvc.perform(get("/plantas/search")
                        .param("termo", "qualquer"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O header Authorization é obrigatório."));
    }

    // ----------------------------
    // DELETE /plantas/{id} (remover existente)
    // ----------------------------

    @Test
    void deveRetornar404AoRemoverPlantaInexistente() throws Exception {
        UUID idInexistente = UUID.randomUUID();

        mockMvc.perform(delete("/plantas/" + idInexistente)
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Planta não encontrada"));
    }

    // ----------------------------
    // Buscar quando usuário não possui pets -> deve retornar lista vazia (ou conforme sua lógica)
    // ----------------------------
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

        // atualiza usuário para não ter pets
        usuario.setPets(List.of());
        usuarioRepository.save(usuario);

        mockMvc.perform(get("/plantas/search")
                        .param("termo", "rosa")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // se sua lógica retornar lista vazia nesse caso, espera vazio; caso contrário, ajuste
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaPlantas() throws Exception {
        plantaRepository.deleteAll(); // garante que banco está vazio

        mockMvc.perform(get("/plantas")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExistirAoBuscarPlantas() throws Exception {
        // token para um ID que não existe no banco
        UUID idInexistente = UUID.randomUUID();
        String tokenFake = "Bearer " + jwtUtil.gerarToken(idInexistente);

        mockMvc.perform(get("/plantas/search")
                        .param("termo", "rosa")
                        .header("Authorization", tokenFake)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }

}
