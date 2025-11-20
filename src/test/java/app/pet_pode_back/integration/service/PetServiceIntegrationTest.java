package app.pet_pode_back.integration.service;

import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PetRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.service.PetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PetServiceIntegrationTest {

    @Autowired
    private PetService petService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PetRepository petRepository;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        // Cria um usuário de teste
        usuario = new Usuario();
        usuario.setNome("Rafaela");
        usuario.setEmail("rafa@test.com");
        usuario.setSenha("123456");
        usuario = usuarioRepository.save(usuario);
    }

    @Test
    void deveSalvarPetParaUsuario() {
        // Criação do pet usando construtor correto
        Pet pet = new Pet(null, "Fido", "Cachorro", usuario);

        Pet salvo = petService.salvarPet(pet, usuario.getId());

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo("Fido");
        assertThat(salvo.getUsuario().getId()).isEqualTo(usuario.getId());
    }

    @Test
    void deveListarPetsDoUsuario() {
        Pet pet1 = new Pet(null, "Fido", "Cachorro", usuario);
        Pet pet2 = new Pet(null, "Mimi", "Gato", usuario);
        petRepository.saveAll(List.of(pet1, pet2));

        List<Pet> pets = petService.listarPetsPorUsuario(usuario.getId());

        assertThat(pets).hasSize(2);
        assertThat(pets).extracting("nome").containsExactlyInAnyOrder("Fido", "Mimi");
    }
}
