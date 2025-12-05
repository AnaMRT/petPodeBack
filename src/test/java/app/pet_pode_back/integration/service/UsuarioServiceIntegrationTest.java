package app.pet_pode_back.integration.service;

import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.exception.ParametroInvalidoException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.service.UsuarioService;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

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
    void deveAtualizarImagemDoUsuario() throws Exception {

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
    void deveLancarErroQuandoCloudinaryFalhaNoUpload() throws Exception {
        MultipartFile file = new MockMultipartFile("img", "img.png", "image/png", "teste".getBytes());

        when(uploaderMock.upload(any(), any())).thenThrow(new RuntimeException("Falha Cloudinary"));

        assertThatThrownBy(() -> usuarioService.atualizarImagemUsuario(usuarioPadrao.getId(), file))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deveAtualizarImagemUsuarioRemovendoImagemAntiga() throws Exception {
        usuarioPadrao.setImagemPublicId("old-img");
        usuarioRepository.save(usuarioPadrao);

        MultipartFile file = new MockMultipartFile("img", "x.png", "image/png", "xxx".getBytes());

        Map<String, Object> result = Map.of(
                "secure_url", "https://nova.com/img.png",
                "public_id", "new-img"
        );

        when(uploaderMock.upload(any(), any())).thenReturn(result);

        String url = usuarioService.atualizarImagemUsuario(usuarioPadrao.getId(), file);

        verify(uploaderMock).destroy(eq("old-img"), any());
        verify(uploaderMock).upload(any(), any());

        assertThat(url).isEqualTo("https://nova.com/img.png");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        UUID idInexistente = UUID.randomUUID();
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNome("X");

        assertThatThrownBy(() -> usuarioService.editarUsuario(idInexistente, dto))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    void deveAtualizarSenhaQuandoInformadaCorretamente() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenhaAtual("123456");
        dto.setSenha("novaSenha");
        dto.setConfirmarSenha("novaSenha");

        Usuario atualizado = usuarioService.editarUsuario(usuarioPadrao.getId(), dto);

        assertThat(passwordEncoder.matches("novaSenha", atualizado.getSenha())).isTrue();
    }

    @Test
    void deveEditarUsuarioSemAlterarSenha() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNome("Novo Nome");
        dto.setEmail("novo@email");

        Usuario atualizado = usuarioService.editarUsuario(usuarioPadrao.getId(), dto);

        assertThat(atualizado.getNome()).isEqualTo("Novo Nome");
        assertThat(atualizado.getSenha()).isEqualTo(usuarioPadrao.getSenha());
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
    void deveLancarExcecaoQuandoSenhaAtualInformadaMasNovaSenhaFaltando() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenhaAtual("123456");
        dto.setSenha(null);
        dto.setConfirmarSenha(null);

        assertThatThrownBy(() -> usuarioService.editarUsuario(usuarioPadrao.getId(), dto))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Para alterar a senha, informe nova senha e confirmação.");
    }


    @Test
    void deveLancarExcecaoQuandoNovaSenhaInformadaSemSenhaAtual() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenha("novaSenha");
        dto.setConfirmarSenha("novaSenha");

        assertThatThrownBy(() -> usuarioService.editarUsuario(usuarioPadrao.getId(), dto))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Para alterar a senha, informe também a senha atual.");
    }


    @Test
    void deveRemoverUsuario() {
        usuarioService.remover(usuarioPadrao.getId());
        assertThat(usuarioRepository.findById(usuarioPadrao.getId())).isEmpty();
    }

    @Test
    void deveLancarExcecaoAoRemoverUsuarioInexistente() {
        UUID idInexistente = UUID.randomUUID();
        assertThatThrownBy(() -> usuarioService.remover(idInexistente))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado");
    }


    @Test
    void deveRetornarUsuarioQuandoEncontrado() {
        Usuario encontrado = usuarioService.buscarUsuarioPorId(usuarioPadrao.getId());

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getId()).isEqualTo(usuarioPadrao.getId());
        assertThat(encontrado.getEmail()).isEqualTo("rafa@email.com");
    }


}
