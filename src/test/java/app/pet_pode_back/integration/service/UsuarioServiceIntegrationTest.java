package app.pet_pode_back.integration.service;

import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.exception.ParametroInvalidoException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.service.UsuarioService;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource(locations = "classpath:.env.test")
class UsuarioServiceIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;


    private Usuario usuarioPadrao;


    @MockBean
    private Cloudinary cloudinary;

    private Uploader uploaderMock;
    private com.cloudinary.Api apiMock;


    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();

        usuarioPadrao = new Usuario();
        usuarioPadrao.setNome("Rafa");
        usuarioPadrao.setEmail("rafa@email.com");
        usuarioPadrao.setSenha(passwordEncoder.encode("123456"));

        usuarioPadrao = usuarioRepository.save(usuarioPadrao);

        uploaderMock = mock(Uploader.class);
        apiMock = mock(com.cloudinary.Api.class);

        when(cloudinary.uploader()).thenReturn(uploaderMock);
        when(cloudinary.api()).thenReturn(apiMock);
    }

    @Test
    void deveAtualizarImagemDoUsuarioComMockCloudinary() throws Exception {

        Map<String, Object> resultadoUpload = Map.of(
                "secure_url", "http://foto.com/img.jpg",
                "public_id", "usuarios/" + usuarioPadrao.getId() + "/img"
        );

        when(uploaderMock.upload(any(byte[].class), anyMap()))
                .thenReturn(resultadoUpload);

        MockMultipartFile arquivo = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", "conteudo".getBytes()
        );

        String url = usuarioService.atualizarImagemUsuario(usuarioPadrao.getId(), arquivo);

        assertThat(url).isEqualTo("http://foto.com/img.jpg");

        Usuario atualizado = usuarioRepository.findById(usuarioPadrao.getId()).orElseThrow();
        assertThat(atualizado.getImagemUrl()).isEqualTo("http://foto.com/img.jpg");
        assertThat(atualizado.getImagemPublicId())
                .isEqualTo("usuarios/" + usuarioPadrao.getId() + "/img");

        verify(uploaderMock, times(1)).upload(any(byte[].class), anyMap());
    }

    @Test
    void deveFalharAtualizarImagemSeIOException() throws Exception {

        MockMultipartFile arquivo = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", "conteudo".getBytes()
        );

        when(uploaderMock.upload(any(byte[].class), anyMap()))
                .thenThrow(new IOException("Falha simulada"));

        assertThatThrownBy(() ->
                usuarioService.atualizarImagemUsuario(usuarioPadrao.getId(), arquivo)
        )
                .isInstanceOf(IOException.class)
                .hasMessage("Falha simulada");
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
    @Test
    void deveFalharAtualizacaoSenhaComSenhaAtualFaltando() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenha("novaSenha");
        dto.setConfirmarSenha("novaSenha");

        assertThatThrownBy(() -> usuarioService.editarUsuario(usuarioPadrao.getId(), dto))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Para alterar a senha, informe também a senha atual.");
    }

    @Test
    void deveFalharAtualizacaoSenhaComNovaSenhaFaltando() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenhaAtual("123456");

        assertThatThrownBy(() -> usuarioService.editarUsuario(usuarioPadrao.getId(), dto))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Para alterar a senha, informe nova senha e confirmação.");
    }
    @Test
    void deveFalharBuscarUsuarioPorIdInexistente() {
        UUID idInexistente = UUID.randomUUID();

        assertThatThrownBy(() -> usuarioService.buscarUsuarioPorId(idInexistente))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado");
    }
    @Test
    void deveFalharSeSenhaAtualInformadaMasNovaSenhaFaltando() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenhaAtual("123456"); // atual informada
        dto.setSenha(null);           // nova senha ausente
        dto.setConfirmarSenha(null);

        assertThatThrownBy(() -> usuarioService.editarUsuario(usuarioPadrao.getId(), dto))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Para alterar a senha, informe nova senha e confirmação.");
    }
    @Test
    void deveFalharSeNovaSenhaDiferenteDaConfirmacao() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenhaAtual("123456");
        dto.setSenha("novaSenha");
        dto.setConfirmarSenha("outraSenha");

        assertThatThrownBy(() -> usuarioService.editarUsuario(usuarioPadrao.getId(), dto))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Nova senha e confirmação não coincidem.");
    }
    @Test
    void deveFalharSeNovaSenhaSemSenhaAtual() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenha("novaSenha");
        dto.setConfirmarSenha("novaSenha");
        dto.setSenhaAtual(null);

        assertThatThrownBy(() -> usuarioService.editarUsuario(usuarioPadrao.getId(), dto))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Para alterar a senha, informe também a senha atual.");
    }
    @Test
    void deveFalharSeSenhaAtualIncorreta() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenhaAtual("errada");
        dto.setSenha("novaSenha");
        dto.setConfirmarSenha("novaSenha");

        assertThatThrownBy(() -> usuarioService.editarUsuario(usuarioPadrao.getId(), dto))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Senha atual incorreta.");
    }


}
