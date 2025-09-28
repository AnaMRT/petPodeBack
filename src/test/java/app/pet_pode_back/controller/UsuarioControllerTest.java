import app.pet_pode_back.controller.UsuarioController;
import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.security.JwtUtil;
import app.pet_pode_back.service.UsuarioService;
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

class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
    }

    @Test
    void CadastrarUsuarioComSucesso() throws Exception {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(UUID.randomUUID());
        usuarioMock.setNome("Teste");
        usuarioMock.setEmail("teste@email.com");
        usuarioMock.setSenha("senha123");

        when(usuarioService.cadastrar(any(Usuario.class))).thenReturn(usuarioMock);

        mockMvc.perform(post("/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teste\",\"email\":\"teste@email.com\",\"senha\":\"senha123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Teste"))
                .andExpect(jsonPath("$.email").value("teste@email.com"));
    }

    @Test
    void deveRemoverUsuarioComSucesso() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = JwtUtil.gerarToken(usuarioId);

        mockMvc.perform(delete("/usuario")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).remover(usuarioId);
    }

    @Test
    void deveEditarUsuarioComSucesso() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = JwtUtil.gerarToken(usuarioId);

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNome("Novo Nome");
        dto.setEmail("novo@email.com");
        dto.setSenha("novaSenha");

        Usuario usuarioEditado = new Usuario();
        usuarioEditado.setId(usuarioId);
        usuarioEditado.setNome("Novo Nome");
        usuarioEditado.setEmail("novo@email.com");
        usuarioEditado.setSenha("senhaCriptografada");

        when(usuarioService.editarUsuario(eq(usuarioId), any(UsuarioUpdateDTO.class))).thenReturn(usuarioEditado);

        mockMvc.perform(put("/usuario")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"))
                .andExpect(jsonPath("$.email").value("novo@email.com"));

        verify(usuarioService, times(1)).editarUsuario(eq(usuarioId), any(UsuarioUpdateDTO.class));
    }

    @Test
    void deveListarUsuariosComSucessoNoController() throws Exception {
        List<Usuario> listaUsuarios = new ArrayList<>();
        Usuario usuario1 = new Usuario();
        usuario1.setId(UUID.randomUUID());
        usuario1.setNome("Teste");
        usuario1.setEmail("teste@email.com");
        listaUsuarios.add(usuario1);

        when(usuarioService.listarTodos()).thenReturn(listaUsuarios);

        mockMvc.perform(get("/usuario")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(usuario1.getId().toString()))
                .andExpect(jsonPath("$[0].nome").value("Teste"))
                .andExpect(jsonPath("$[0].email").value("teste@email.com"));

        verify(usuarioService, times(1)).listarTodos();
    }

}