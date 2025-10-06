package app.pet_pode_back.controller;

import app.pet_pode_back.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {

        usuarioRepository.deleteAll();
    }

    @Test
    public void criarUsuarioEVerificarNoBanco() throws Exception {

        Map<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("nome", "Teste Banco");
        usuarioMap.put("email", "teste.banco@email.com");
        usuarioMap.put("senha", "senha123");


        mockMvc.perform(post("/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioMap)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Teste Banco"))
                .andExpect(jsonPath("$.email").value("teste.banco@email.com"));

        mockMvc.perform(get("/usuario")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Teste Banco"))
                .andExpect(jsonPath("$[0].email").value("teste.banco@email.com"));
    }
}
