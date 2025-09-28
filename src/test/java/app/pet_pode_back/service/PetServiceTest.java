package app.pet_pode_back.service;

import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PetRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private Petservice petService;

    private Usuario usuario;
    private Pet pet;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("João");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("senha123");

        pet = new Pet();
        pet.setNome("Rex");
        pet.setEspecie("Cachorro");
    }

    @Test
    public void SalvarPetComSucesso() {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(petRepository.save(any(Pet.class))).thenAnswer(i -> i.getArgument(0));

        Pet petSalvo = petService.salvarPet(pet, usuario.getId());

        assertEquals("Rex", petSalvo.getNome());
        assertEquals("Cachorro", petSalvo.getEspecie());
        assertEquals(usuario, petSalvo.getUsuario());
    }

    @Test
    public void salvarPetUsuarioNaoEncontrado() {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            petService.salvarPet(pet, usuario.getId());
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    public void salvarPetFalhaNoBanco() {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(petRepository.save(any(Pet.class))).thenThrow(new RuntimeException("Erro ao salvar pet"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            petService.salvarPet(pet, usuario.getId());
        });

        assertEquals("Erro ao salvar pet", exception.getMessage());
    }



    @Test
    void deveListarPetsComSucesso() {
        List<Pet> listaPet = new ArrayList<>();
        Pet pet1 = new Pet();
        pet1.setId(UUID.randomUUID());
        pet1.setNome("Teste");
        pet1.setEspecie("canino");
        listaPet.add(pet1);

        when(petRepository.findAll()).thenReturn(listaPet);

        List<Pet> retorno = petService.listarTodos();

        verify(petRepository, times(1)).findAll();
        assertEquals(1, retorno.size(), "Não retornou o tamanho correto");
        assertEquals(listaPet.get(0).getId(), retorno.get(0).getId(), "Não retornou o pet correto");
    }

    @Test
    void listarPetsVazio() {
        when(petRepository.findAll()).thenReturn(new ArrayList<>());

        List<Pet> retorno = petService.listarTodos();

        verify(petRepository, times(1)).findAll();
        assertTrue(retorno.isEmpty(), "A lista deveria estar vazia");
    }

    @Test
    void listarPetsFalhaNoBanco() {
        when(petRepository.findAll()).thenThrow(new RuntimeException("Erro ao acessar banco"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            petService.listarTodos();
        });

        assertEquals("Erro ao acessar banco", exception.getMessage());
    }


}
