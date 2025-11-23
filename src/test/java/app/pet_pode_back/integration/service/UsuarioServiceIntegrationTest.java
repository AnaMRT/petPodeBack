package app.pet_pode_back.integration.service;

import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.exception.ParametroInvalidoException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.service.UsuarioService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsuarioServiceIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario usuarioPadrao;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll(); // limpa usuários antes

        usuarioPadrao = new Usuario();
        usuarioPadrao.setNome("Rafa");
        usuarioPadrao.setEmail("rafa@email.com");
        usuarioPadrao.setSenha(passwordEncoder.encode("123456"));

        usuarioPadrao = usuarioRepository.save(usuarioPadrao);
    }

    @Test
    void deveListarUsuarios() {
        List<Usuario> usuarios = usuarioService.listarTodos();
        assertThat(usuarios).isNotEmpty();
        assertThat(usuarios.get(0).getEmail()).isEqualTo(usuarioPadrao.getEmail());
    }

    @Test
    void deveEditarNomeDoUsuario() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNome("NovoNome");

        Usuario atualizado = usuarioService.editarUsuario(usuarioPadrao.getId(), dto);

        assertThat(atualizado.getNome()).isEqualTo("NovoNome");
        assertThat(atualizado.getEmail()).isEqualTo(usuarioPadrao.getEmail());
    }

    @Test
    void deveEditarEmailDoUsuario() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setEmail("novo@email.com");

        Usuario atualizado = usuarioService.editarUsuario(usuarioPadrao.getId(), dto);

        assertThat(atualizado.getEmail()).isEqualTo("novo@email.com");
    }

    @Test
    void deveFalharSeUsuarioNaoExistir() {
        UUID idInexistente = UUID.randomUUID();
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNome("X");

        assertThatThrownBy(() -> usuarioService.editarUsuario(idInexistente, dto))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    void deveAtualizarSenhaCorretamente() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenhaAtual("123456");
        dto.setSenha("novaSenha");
        dto.setConfirmarSenha("novaSenha");

        Usuario atualizado = usuarioService.editarUsuario(usuarioPadrao.getId(), dto);

        assertThat(passwordEncoder.matches("novaSenha", atualizado.getSenha())).isTrue();
    }

    @Test
    void deveFalharAtualizacaoSenhaComSenhaAtualErrada() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenhaAtual("errada");
        dto.setSenha("novaSenha");
        dto.setConfirmarSenha("novaSenha");

        assertThatThrownBy(() -> usuarioService.editarUsuario(usuarioPadrao.getId(), dto))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Senha atual incorreta.");
    }

    @Test
    void deveFalharQuandoNovaSenhaEDiferenteDaConfirmacao() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenhaAtual("123456");
        dto.setSenha("novaSenha");
        dto.setConfirmarSenha("outraSenha");

        assertThatThrownBy(() -> usuarioService.editarUsuario(usuarioPadrao.getId(), dto))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Nova senha e confirmação não coincidem.");
    }

    @Test
    void deveRemoverUsuario() {
        usuarioService.remover(usuarioPadrao.getId());
        assertThat(usuarioRepository.findById(usuarioPadrao.getId())).isEmpty();
    }

    @Test
    void deveFalharAoRemoverUsuarioInexistente() {
        UUID idInexistente = UUID.randomUUID();
        assertThatThrownBy(() -> usuarioService.remover(idInexistente))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado");
    }
}
