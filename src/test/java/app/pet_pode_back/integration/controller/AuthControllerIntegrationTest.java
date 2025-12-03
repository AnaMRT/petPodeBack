package app.pet_pode_back.integration.controller;

import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.model.PasswordResetToken;
import app.pet_pode_back.repository.PasswordResetTokenRepository;

import app.pet_pode_back.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:.env.test")
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void limparBanco() {
        resetTokenRepository.deleteAll();
        usuarioRepository.deleteAll();

        doNothing().when(emailService).enviarEmail(anyString(), anyString(), anyString());

    }

    private PasswordResetToken criarToken(Usuario usuario, String codigo, boolean usado, LocalDateTime expira) {
        PasswordResetToken t = new PasswordResetToken();
        t.setUsuario(usuario);
        t.setCodigo(codigo);
        t.setUsed(usado);
        t.setExpirationDate(expira);
        return resetTokenRepository.save(t);
    }


    @Test
    void deveRegistrarUsuarioComSucesso() throws Exception {

        String json = """
                {
                  "nome": "Rafa",
                  "email": "teste@teste.com",
                  "senha": "123"
                }
                """;

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }


    @Test
    void deveFalharAoRegistrarUsuarioComEmailJaExistente() throws Exception {

        Usuario u = new Usuario();
        u.setNome("Rafa");
        u.setEmail("duplicado@test.com");
        u.setSenha(passwordEncoder.encode("123"));
        usuarioRepository.save(u);

        String json = """
                {
                  "nome": "Outro",
                  "email": "duplicado@test.com",
                  "senha": "abc"
                }
                """;

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Email já cadastrado"));
    }


    @Test
    void deveLogarComSucesso() throws Exception {

        Usuario u = new Usuario();
        u.setNome("Rafa");
        u.setEmail("login@test.com");
        u.setSenha(passwordEncoder.encode("123"));
        usuarioRepository.save(u);

        String json = """
                {
                  "email": "login@test.com",
                  "senha": "123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }


    @Test
    void deveFalharAoLogarComCredenciaisInvalidas() throws Exception {

        Usuario u = new Usuario();
        u.setNome("Rafa");
        u.setEmail("senhaerrada@test.com");
        u.setSenha(passwordEncoder.encode("123"));
        usuarioRepository.save(u);

        String json = """
                {
                  "email": "senhaerrada@test.com",
                  "senha": "999"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Credenciais inválidas"));
    }


    @Test
    void deveFalharLoginComEmailNaoEncontrado() throws Exception {

        String json = """
                {
                  "email": "naoexiste@test.com",
                  "senha": "123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }


    @Test
    void deveSolicitarRedefinicaoSenhaComSucesso() throws Exception {

        Usuario u = new Usuario();
        u.setNome("Rafa");
        u.setEmail("reset@test.com");
        u.setSenha(passwordEncoder.encode("123"));
        usuarioRepository.save(u);

        mockMvc.perform(post("/auth/forgot-password")
                        .param("email", "reset@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Código de redefinição de senha enviado por e-mail."));

        assertEquals(1, resetTokenRepository.count());
    }

    @Test
    void deveFalharAoSolicitarRedefinicaoQuandoEmailNaoExiste() throws Exception {

        mockMvc.perform(post("/auth/forgot-password")
                        .param("email", "naoexiste@test.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }


    @Test
    void deveRedefinirSenhaComSucesso() throws Exception {

        Usuario u = new Usuario();
        u.setNome("Rafa");
        u.setEmail("okreset@test.com");
        u.setSenha(passwordEncoder.encode("123"));
        usuarioRepository.save(u);

        criarToken(u, "999999", false, LocalDateTime.now().plusMinutes(10));

        String json = """
                {
                  "codigo": "999999",
                  "novaSenha": "nova123"
                }
                """;

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Senha redefinida com sucesso."));

        Usuario atualizado = usuarioRepository.findByEmail("okreset@test.com").get();
        assertTrue(passwordEncoder.matches("nova123", atualizado.getSenha()));
    }


    @Test
    void deveFalharAoRedefinirSenhaComCodigoInvalido() throws Exception {

        String json = """
                {
                  "codigo": "naoexiste",
                  "novaSenha": "abc"
                }
                """;

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Código inválido."));
    }


    @Test
    void deveFalharAoRedefinirSenhaTokenUsado() throws Exception {

        Usuario u = new Usuario();
        u.setNome("Rafa");
        u.setEmail("used@test.com");
        u.setSenha(passwordEncoder.encode("123"));
        usuarioRepository.save(u);

        criarToken(u, "888888", true, LocalDateTime.now().plusMinutes(10));

        String json = """
                {
                  "codigo": "888888",
                  "novaSenha": "nova123"
                }
                """;

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Código já foi utilizado."));
    }


    @Test
    void deveFalharAoRedefinirSenhaTokenExpirado() throws Exception {

        Usuario u = new Usuario();
        u.setNome("Rafa");
        u.setEmail("expirado@test.com");
        u.setSenha(passwordEncoder.encode("123"));
        usuarioRepository.save(u);

        criarToken(u, "777777", false, LocalDateTime.now().minusMinutes(1));

        String json = """
                {
                  "codigo": "777777",
                  "novaSenha": "nova123"
                }
                """;

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Código expirado."));
    }

    @Test
    void deveFalharAoRedefinirSenhaSemCamposObrigatorios() throws Exception {

        String json = """
                {
                  "codigo": "",
                  "novaSenha": ""
                }
                """;

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveFalharAoLogarComCamposInvalidos() throws Exception {
        String json = """
            {
              "email": "",
              "senha": ""
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }


    @Test
    void deveFalharAoRegistrarComCamposInvalidos() throws Exception {
        String json = """
            {
              "nome": "",
              "email": "",
              "senha": ""
            }
            """;

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }


    @Test
    void deveFalharResetSenhaSemCampos() throws Exception {
        String json = """
            {
              "codigo": "",
              "novaSenha": ""
            }
            """;

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

}
