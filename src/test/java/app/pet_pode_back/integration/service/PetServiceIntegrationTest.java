package app.pet_pode_back.integration.service;

import app.pet_pode_back.dto.PetUpdateDTO;
import app.pet_pode_back.exception.PetNotFoundException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.exception.SemPermissaoException;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PetRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.service.PetService;
import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.cloudinary.Uploader;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:.env.test")
@Transactional
public class PetServiceIntegrationTest {

    @Autowired
    private PetService petService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PetRepository petRepository;

    @MockBean
    private Cloudinary cloudinary;

    private Usuario usuario;
    private Uploader uploaderMock;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setNome("Rafaela");
        usuario.setEmail("rafa@test.com");
        usuario.setSenha("123456");
        usuario = usuarioRepository.save(usuario);


    }

    @BeforeEach
    void setupMocks() throws Exception {
        uploaderMock = mock(Uploader.class);

        when(cloudinary.uploader()).thenReturn(uploaderMock);

        when(uploaderMock.destroy(anyString(), anyMap()))
                .thenReturn(Map.of("result", "ok"));

        when(uploaderMock.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of(
                        "secure_url", "https://fake.com/pet.png",
                        "public_id", "public_fake_id"
                ));
    }

    @Test
    void deveSalvarPetQuandoUsuarioExiste() {
        Pet pet = new Pet(null, "Fido", "Canino", usuario);

        Pet salvo = petService.salvarPet(pet, usuario.getId());

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getUsuario().getId()).isEqualTo(usuario.getId());
    }

    @Test
    void deveLancarErroAoSalvarPetComUsuarioInexistente() {
        Pet pet = new Pet(null, "Fido", "Canino", null);

        assertThatThrownBy(() ->
                petService.salvarPet(pet, UUID.randomUUID()))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado.");
    }


    @Test
    void deveListarPetsPorUsuario() {
        Pet p1 = new Pet(null, "Rex", "Canino", usuario);
        Pet p2 = new Pet(null, "Mimi", "Felino", usuario);
        petRepository.saveAll(List.of(p1, p2));

        List<Pet> pets = petService.listarPetsPorUsuario(usuario.getId());

        assertThat(pets).hasSize(2);
        assertThat(pets).extracting("nome")
                .containsExactlyInAnyOrder("Rex", "Mimi");
    }


    @Test
    void deveEditarPetComSucesso() {
        Pet pet = petRepository.save(new Pet(null, "Rex", "Canino", usuario));

        PetUpdateDTO dto = new PetUpdateDTO();
        dto.setNome("Thor");
        dto.setEspecie("Felino");

        Pet atualizado = petService.editarPet(pet.getId(), usuario.getId(), dto);

        assertThat(atualizado.getNome()).isEqualTo("Thor");
        assertThat(atualizado.getEspecie()).isEqualTo("Felino");
    }

    @Test
    void deveLancarErroAoEditarPetInexistente() {
        PetUpdateDTO dto = new PetUpdateDTO();
        dto.setNome("Qualquer");
        dto.setEspecie("Felino");

        assertThatThrownBy(() ->
                petService.editarPet(UUID.randomUUID(), usuario.getId(), dto))
                .isInstanceOf(PetNotFoundException.class)
                .hasMessage("Pet não encontrado.");
    }


    @Test
    void deveLancarErroAoEditarPetDeOutroUsuario() {
        Usuario outro = new Usuario();
        outro.setNome("Joao");
        outro.setEmail("j@test.com");
        outro.setSenha("123");
        outro = usuarioRepository.save(outro);

        Pet pet = petRepository.save(new Pet(null, "Rex", "Canino", outro));

        PetUpdateDTO dto = new PetUpdateDTO();
        dto.setNome("Novo");
        dto.setEspecie("Felino");

        assertThatThrownBy(() ->
                petService.editarPet(pet.getId(), usuario.getId(), dto))
                .isInstanceOf(SemPermissaoException.class)
                .hasMessage("Você não tem permissão para alterar esse pet.");
    }


    @Test
    void deveExcluirPetComSucesso() {
        Pet pet = petRepository.save(new Pet(null, "Rex", "Canino", usuario));

        assertThatCode(() ->
                petService.excluirPetDoUsuario(usuario.getId(), pet.getId()))
                .doesNotThrowAnyException();

        assertThat(petRepository.findById(pet.getId())).isEmpty();
    }

    @Test
    void deveLancarErroAoExcluirPetDeOutroUsuario() {
        Usuario outro = new Usuario();
        outro.setNome("Joao");
        outro.setEmail("j@test.com");
        outro.setSenha("123");
        outro = usuarioRepository.save(outro);

        Pet pet = petRepository.save(new Pet(null, "Rex", "Canino", outro));

        assertThatThrownBy(() ->
                petService.excluirPetDoUsuario(usuario.getId(), pet.getId()))
                .isInstanceOf(SemPermissaoException.class);
    }

    @Test
    void deveLancarErroAoExcluirPetInexistente() {
        assertThatThrownBy(() ->
                petService.excluirPetDoUsuario(usuario.getId(), UUID.randomUUID()))
                .isInstanceOf(PetNotFoundException.class)
                .hasMessage("Pet não encontrado.");
    }

    @Test
    void deveAtualizarImagemPetComSucesso() throws IOException {
        Pet pet = petRepository.save(new Pet(null, "Rex", "Canino", usuario));

        Uploader uploaderMock = mock(Uploader.class);
        Map<String, Object> resultadoUpload = Map.of(
                "secure_url", "https://fakeurl.com/pet.jpg",
                "public_id", "fakeid"
        );
        Map<String, Object> resultadoDestroy = Map.of("result", "ok");

        when(cloudinary.uploader()).thenReturn(uploaderMock);
        when(uploaderMock.upload(any(byte[].class), any(Map.class))).thenReturn(resultadoUpload);
        when(uploaderMock.destroy(any(String.class), any(Map.class))).thenReturn(resultadoDestroy);

        MultipartFile file = new MockMultipartFile(
                "file", "pet.jpg", "image/jpeg", "conteudoFake".getBytes()
        );

        String url = petService.atualizarImagemPet(pet.getId(), usuario.getId(), file);

        assertThat(url).isEqualTo("https://fakeurl.com/pet.jpg");
    }

    @Test
    void deveLancarErroAoAtualizarImagemDePetInexistente() {
        MultipartFile file = new MockMultipartFile("img", "foto.png", "image/png", "abc".getBytes());

        assertThatThrownBy(() ->
                petService.atualizarImagemPet(UUID.randomUUID(), usuario.getId(), file)
        )
                .isInstanceOf(PetNotFoundException.class)
                .hasMessage("Pet não encontrado.");
    }

    @Test
    void deveLancarErroAoAtualizarImagemDePetDeOutroUsuario() throws Exception {
        Usuario outro = new Usuario();
        outro.setNome("Joao");
        outro.setEmail("j@teste.com");
        outro.setSenha("123456");
        outro = usuarioRepository.save(outro);

        Pet pet = petRepository.save(new Pet(null, "Rex", "Canino", outro));

        MultipartFile file = new MockMultipartFile("img", "foto.png", "image/png", "abc".getBytes());

        assertThatThrownBy(() ->
                petService.atualizarImagemPet(pet.getId(), usuario.getId(), file)
        )
                .isInstanceOf(SemPermissaoException.class)
                .hasMessage("Você não tem permissão para alterar esse pet.");
    }

    @Test
    void deveLancarErroQuandoUploadFalhar() throws Exception {
        Pet pet = petRepository.save(new Pet(null, "Rex", "Canino", usuario));

        MultipartFile file = new MockMultipartFile("img", "foto.png", "image/png", "abc".getBytes());

        when(uploaderMock.upload(any(byte[].class), anyMap()))
                .thenThrow(new IOException("Falha ao enviar imagem"));

        assertThatThrownBy(() ->
                petService.atualizarImagemPet(pet.getId(), usuario.getId(), file)
        )
                .isInstanceOf(IOException.class)
                .hasMessage("Falha ao enviar imagem");
    }


    @Test
    void deveDeletarImagemAntigaAntesDeAtualizar() throws Exception {
        Pet pet = petRepository.save(new Pet(null, "Rex", "Canino", usuario));
        pet.setImagemPublicId("old-img");
        petRepository.save(pet);

        MultipartFile file = new MockMultipartFile("img", "foto.png", "image/png", "abc".getBytes());

        petService.atualizarImagemPet(pet.getId(), usuario.getId(), file);

        verify(uploaderMock).destroy(eq("old-img"), any());
    }

    @Test
    void deveLancarErroQuandoPetForNull() {
        assertThatThrownBy(() ->
                petService.salvarPet(null, usuario.getId())
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void deveRetornarMensagemCorretaQuandoUsuarioNaoEncontrado() {
        Pet pet = new Pet(null, "Rex", "Canino", null);

        UUID usuarioInexistente = UUID.randomUUID();

        assertThatThrownBy(() ->
                petService.salvarPet(pet, usuarioInexistente)
        )
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado.");
    }

}
