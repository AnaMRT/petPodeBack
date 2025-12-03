package app.pet_pode_back.unit.service;

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
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PetServiceTest {

    @InjectMocks
    private PetService petService;

    @Mock
    private PetRepository petRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private MultipartFile file;

    @Mock
    private Uploader uploader;

    private Usuario usuario;
    private Pet pet;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        pet = new Pet();
        pet.setId(UUID.randomUUID());
        pet.setUsuario(usuario);

        when(cloudinary.uploader()).thenReturn(uploader);
    }


    @Test
    void deveSalvarPetQuandoUsuarioExiste() {
        when(usuarioRepository.findById(usuario.getId()))
                .thenReturn(Optional.of(usuario));

        when(petRepository.save(any(Pet.class)))
                .thenReturn(pet);

        Pet salvo = petService.salvarPet(pet, usuario.getId());

        assertEquals(usuario, salvo.getUsuario());
        verify(petRepository, times(1)).save(pet);
    }

    @Test
    void deveLancarErroAoSalvarPetComUsuarioInexistente() {
        when(usuarioRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> petService.salvarPet(pet, UUID.randomUUID())
        );
    }


    @Test
    void deveListarPetsPorUsuario() {
        when(petRepository.findAllByUsuario_Id(usuario.getId()))
                .thenReturn(List.of(pet));

        List<Pet> resultado = petService.listarPetsPorUsuario(usuario.getId());

        assertEquals(1, resultado.size());
    }


    @Test
    void deveEditarPetComSucesso() {
        PetUpdateDTO dto = new PetUpdateDTO();
        dto.setNome("Novo nome");
        dto.setEspecie("Gato");

        when(petRepository.findById(pet.getId()))
                .thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class)))
                .thenReturn(pet);

        Pet atualizado = petService.editarPet(pet.getId(), usuario.getId(), dto);

        assertEquals("Novo nome", atualizado.getNome());
        assertEquals("Gato", atualizado.getEspecie());
    }

    @Test
    void deveLancarErroAoEditarPetDeOutroUsuario() {
        usuario.setId(UUID.randomUUID());
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(UUID.randomUUID());
        pet.setUsuario(outroUsuario);

        when(petRepository.findById(pet.getId()))
                .thenReturn(Optional.of(pet));

        assertThrows(
                SemPermissaoException.class,
                () -> petService.editarPet(pet.getId(), usuario.getId(), new PetUpdateDTO())
        );
    }

    @Test
    void deveLancarErroAoEditarPetInexistente() {
        when(petRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                PetNotFoundException.class,
                () -> petService.editarPet(UUID.randomUUID(), usuario.getId(), new PetUpdateDTO())
        );
    }


    @Test
    void deveAtualizarImagemPetComSucesso() throws Exception {
        when(file.getBytes()).thenReturn("imagem".getBytes());

        when(petRepository.findById(pet.getId()))
                .thenReturn(Optional.of(pet));

        Map resultado = Map.of(
                "secure_url", "http://imagem.com/pet.jpg",
                "public_id", "pets/123/img"
        );

        when(uploader.upload(any(), any()))
                .thenReturn(resultado);

        String url = petService.atualizarImagemPet(pet.getId(), usuario.getId(), file);

        assertEquals("http://imagem.com/pet.jpg", url);
        verify(petRepository).save(pet);
    }

    @Test
    void deveLancarErroAoAtualizarImagemDePetDeOutroUsuario() {
        Usuario outro = new Usuario();
        outro.setId(UUID.randomUUID());
        pet.setUsuario(outro);

        when(petRepository.findById(pet.getId()))
                .thenReturn(Optional.of(pet));

        assertThrows(
                SemPermissaoException.class,
                () -> petService.atualizarImagemPet(pet.getId(), usuario.getId(), file)
        );
    }


    @Test
    void deveLancarErroAoExcluirPetDeOutroUsuario() {
        Usuario dono = new Usuario();
        dono.setId(UUID.randomUUID());
        pet.setUsuario(dono);

        when(petRepository.findById(pet.getId()))
                .thenReturn(Optional.of(pet));

        assertThrows(
                SemPermissaoException.class,
                () -> petService.excluirPetDoUsuario(usuario.getId(), pet.getId())
        );
    }

    @Test
    void deveLancarErroAoExcluirPetInexistente() {
        when(petRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                PetNotFoundException.class,
                () -> petService.excluirPetDoUsuario(usuario.getId(), UUID.randomUUID())
        );
    }

    @Test
    void deveLancarErroQuandoPetForNullAoSalvar() {
        when(usuarioRepository.findById(usuario.getId()))
                .thenReturn(Optional.of(usuario));

        assertThrows(NullPointerException.class,
                () -> petService.salvarPet(null, usuario.getId()));
    }

    @Test
    void deveManterValoresQuandoDtoVazio() {
        pet.setNome("Rex");
        pet.setEspecie("Cachorro");

        when(petRepository.findById(pet.getId()))
                .thenReturn(Optional.of(pet));
        when(petRepository.save(any()))
                .thenReturn(pet);

        PetUpdateDTO dto = new PetUpdateDTO(); // tudo null

        Pet atualizado = petService.editarPet(pet.getId(), usuario.getId(), dto);

        assertEquals("Rex", atualizado.getNome());
        assertEquals("Cachorro", atualizado.getEspecie());
    }

    @Test
    void deveLancarErroAoAtualizarImagemDePetInexistente() throws Exception {
        when(petRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                PetNotFoundException.class,
                () -> petService.atualizarImagemPet(UUID.randomUUID(), usuario.getId(), file)
        );

        verify(uploader, never()).upload(any(), any());
    }

    @Test
    void deveExcluirPetComSucesso() {
        when(petRepository.findById(pet.getId()))
                .thenReturn(Optional.of(pet));

        assertDoesNotThrow(() ->
                petService.excluirPetDoUsuario(usuario.getId(), pet.getId())
        );

        verify(petRepository).delete(pet);
    }

    @Test
    void deveDeletarImagemAntigaAntesDeAtualizar() throws Exception {
        pet.setImagemPublicId("old-img");

        when(petRepository.findById(pet.getId()))
                .thenReturn(Optional.of(pet));

        when(file.getBytes()).thenReturn("img".getBytes());
        when(uploader.upload(any(), any()))
                .thenReturn(Map.of("secure_url", "x", "public_id", "y"));

        petService.atualizarImagemPet(pet.getId(), usuario.getId(), file);

        verify(uploader).destroy(eq("old-img"), any());
    }

    @Test
    void deveRetornarMensagemCorretaQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.findById(any()))
                .thenReturn(Optional.empty());

        RegistroNaoEncontradoException ex =
                assertThrows(RegistroNaoEncontradoException.class,
                        () -> petService.salvarPet(pet, UUID.randomUUID()));

        assertEquals("Usuário não encontrado.", ex.getMessage());
    }
    @Test
    void deveLancarErroQuandoUploadFalhar() throws Exception {
        when(petRepository.findById(pet.getId())).thenReturn(Optional.of(pet));
        when(file.getBytes()).thenReturn("abc".getBytes());
        when(uploader.upload(any(), any())).thenThrow(new IOException("Falha ao enviar imagem"));

        assertThrows(IOException.class, () ->
                petService.atualizarImagemPet(pet.getId(), usuario.getId(), file));
    }

}
