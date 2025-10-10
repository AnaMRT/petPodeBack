package app.pet_pode_back.service;

import app.pet_pode_back.dto.PetUpdateDTO;
import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.exception.PermissionDeniedException;
import app.pet_pode_back.exception.PetNotFoundException;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PetRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
public class Petservice {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


    public Pet salvarPet(Pet pet, UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        pet.setUsuario(usuario);
        return petRepository.save(pet);
    }



    public List<Pet> listarPetsPorUsuario(UUID usuarioId) {
        System.out.println("Buscando pets para usuarioId: " + usuarioId);
        List<Pet> pets = petRepository.findAllByUsuario_Id(usuarioId);
        System.out.println("Pets encontrados: " + pets.size());
        return pets;
    }


    public void excluirPetDoUsuario(UUID usuarioId, UUID petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet não encontrado"));

        if (!pet.getUsuario().getId().equals(usuarioId)) {
            throw new PermissionDeniedException("Você não tem permissão para excluir este pet.");
        }

        petRepository.delete(pet);
    }

    public Pet editarPet(UUID petId, UUID usuarioId, PetUpdateDTO dto) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet não encontrado"));

        if (!pet.getUsuario().getId().equals(usuarioId)) {
            throw new PermissionDeniedException("Você não tem permissão para editar este pet.");
        }

        if (dto.getNome() != null) {
            pet.setNome(dto.getNome());
        }

        if (dto.getEspecie() != null) {
            pet.setEspecie(dto.getEspecie());
        }


        return petRepository.save(pet);
    }

}
