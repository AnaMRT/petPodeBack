package app.pet_pode_back.unit.service;

import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.exception.ParametroInvalidoException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.service.UsuarioService;
import app.pet_pode_back.security.JwtUtil;
import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock private Cloudinary cloudinary;
    @Mock private Uploader uploader;
    @Mock private Api api;
    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PlantaRepository plantaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(cloudinary.api()).thenReturn(api);
    }


    @Test
    void deveRetornarUsuarioQuandoEncontrado() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarUsuarioPorId(id);

        verify(usuarioRepository).findById(id);
        assertThat(resultado).isEqualTo(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        UUID id = UUID.randomUUID();

        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarUsuarioPorId(id))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado");

        verify(usuarioRepository).findById(id);
    }


    @Test
    void deveLancarErroQuandoArquivoFalhaAoLerBytes() throws Exception {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);

        MultipartFile file = mock(MultipartFile.class);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(file.getBytes()).thenThrow(new IOException("erro"));

        assertThrows(IOException.class, () -> usuarioService.atualizarImagemUsuario(id, file));

        verify(uploader, never()).upload(any(), any());
    }

    @Test
    void deveLancarErroQuandoCloudinaryFalhaNoUpload() throws Exception {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);

        MultipartFile file = mock(MultipartFile.class);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(file.getBytes()).thenReturn("dados".getBytes());

        when(uploader.upload(any(), any())).thenThrow(new RuntimeException("Falha Cloudinary"));

        assertThrows(RuntimeException.class,
                () -> usuarioService.atualizarImagemUsuario(id, file));

        verify(uploader).upload(any(), any());
        verify(usuarioRepository, never()).save(any());
    }
    @Test
    void deveAtualizarImagemUsuarioRemovendoImagemAntiga() throws Exception {
        UUID id = UUID.randomUUID();

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setImagemPublicId("old-public-id");

        MultipartFile file = mock(MultipartFile.class);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(file.getBytes()).thenReturn("conteudo".getBytes());

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://nova-imagem.com/img.png");
        uploadResult.put("public_id", "new-public-id");

        when(uploader.upload(any(), any())).thenReturn(uploadResult);

        String resultado = usuarioService.atualizarImagemUsuario(id, file);

        verify(uploader).destroy(eq("old-public-id"), any());
        verify(uploader).upload(any(), any());
        verify(usuarioRepository).save(usuario);

        assertThat(resultado).isEqualTo("https://nova-imagem.com/img.png");
        assertThat(usuario.getImagemPublicId()).isEqualTo("new-public-id");
    }
    @Test
    void deveRemoverUsuarioComImagensEPets() throws Exception {
        UUID usuarioId = UUID.randomUUID();

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setImagemPublicId("user-img");

        Pet pet = new Pet();
        pet.setId(UUID.randomUUID());
        pet.setImagemPublicId("pet-img");
        usuario.setPets(List.of(pet));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        usuarioService.remover(usuarioId);

        verify(uploader).destroy(eq("user-img"), any());
        verify(uploader).destroy(eq("pet-img"), any());

        verify(api).deleteFolder(eq("pets/" + pet.getId()), any());
        verify(api).deleteFolder(eq("usuarios/" + usuarioId), any());
        verify(api).deleteFolder(eq("pets/" + usuarioId), any());

        verify(usuarioRepository, times(1)).delete(usuario);
    }

    @Test
    void deveEditarUsuarioSemAlterarSenha() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Antigo Nome");
        usuario.setEmail("antigo@email.com");

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNome("Novo Nome");
        dto.setEmail("novo@email.com");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario resultado = usuarioService.editarUsuario(id, dto);

        verify(usuarioRepository).save(usuario);
        assertThat(resultado.getNome()).isEqualTo("Novo Nome");
        assertThat(resultado.getEmail()).isEqualTo("novo@email.com");
    }

    @Test
    void deveEditarUsuarioAlterandoSenha() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setSenha("senhaAtualCriptografada");

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenhaAtual("senhaAtual");
        dto.setSenha("novaSenha");
        dto.setConfirmarSenha("novaSenha");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaAtual", "senhaAtualCriptografada")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha")).thenReturn("senhaNovaCriptografada");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario resultado = usuarioService.editarUsuario(id, dto);

        verify(usuarioRepository).save(usuario);
        assertThat(resultado.getSenha()).isEqualTo("senhaNovaCriptografada");
    }

    @Test
    void deveLancarExcecaoQuandoSenhaAtualInformadaMasNovaSenhaFaltando() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setSenha("senhaAtualCriptografada");

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenhaAtual("senhaAtual");
        dto.setSenha(null);
        dto.setConfirmarSenha("novaSenha");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        assertThrows(ParametroInvalidoException.class,
                () -> usuarioService.editarUsuario(id, dto));
    }

    @Test
    void deveLancarExcecaoQuandoNovaSenhaEDiferenteDaConfirmacao() {
        UUID id = UUID.randomUUID();

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setSenha("senhaAntiga");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaAntiga", usuario.getSenha())).thenReturn(true);

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenhaAtual("senhaAntiga");
        dto.setSenha("novaSenha123");
        dto.setConfirmarSenha("senhaDiferente456");

        assertThatThrownBy(() -> usuarioService.editarUsuario(id, dto))
                .isInstanceOf(ParametroInvalidoException.class)
                .hasMessage("Nova senha e confirmação não coincidem.");
    }

    @Test
    void deveLancarExcecaoQuandoNovaSenhaInformadaSemSenhaAtual() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setSenha("senhaAtualCriptografada");

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenha("novaSenha");
        dto.setConfirmarSenha("novaSenha");
        dto.setSenhaAtual(null); // senha atual não informada

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        assertThrows(ParametroInvalidoException.class,
                () -> usuarioService.editarUsuario(id, dto));
    }

    @Test
    void deveLancarExcecaoAoEditarUsuarioInexistente() {
        UUID id = UUID.randomUUID();
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class, () -> usuarioService.editarUsuario(id, dto));
    }

    @Test
    void deveLancarExcecaoAoEditarUsuarioComSenhaInvalida() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setSenha("senhaAtualCriptografada");

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setSenhaAtual("senhaErrada");
        dto.setSenha("novaSenha");
        dto.setConfirmarSenha("novaSenha");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaErrada", "senhaAtualCriptografada")).thenReturn(false);

        assertThrows(ParametroInvalidoException.class,
                () -> usuarioService.editarUsuario(id, dto));
    }


    @Test
    void deveLancarExcecaoAoRemoverUsuarioInexistente() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class,
                () -> usuarioService.remover(id));

        verify(usuarioRepository, never()).delete(any());
    }


    @Test
    void deveRetornarUsuarioLogado() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Nome");
        usuario.setEmail("email@test.com");
        usuario.setImagemUrl("url");

        when(jwtUtil.extrairUsuarioId("token")).thenReturn(id);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        Map<String, Object> resultado = usuarioService.getUsuarioLogado("token");

        assertThat(resultado.get("id")).isEqualTo(id);
        assertThat(resultado.get("nome")).isEqualTo("Nome");
        assertThat(resultado.get("email")).isEqualTo("email@test.com");
        assertThat(resultado.get("imagemUrl")).isEqualTo("url");
    }

    @Test
    void deveLancarExcecaoQuandoTokenInvalido() {
        when(jwtUtil.extrairUsuarioId("tokenErrado")).thenThrow(new IllegalArgumentException("Token inválido"));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.getUsuarioLogado("tokenErrado"));
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioDoTokenNaoExiste() {
        UUID id = UUID.randomUUID();
        when(jwtUtil.extrairUsuarioId("token")).thenReturn(id);
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class, () -> usuarioService.getUsuarioLogado("token"));
    }



}
