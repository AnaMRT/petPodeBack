package app.pet_pode_back.service;

import app.pet_pode_back.dto.PetUpdateDTO;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.exception.SemPermissaoException;
import app.pet_pode_back.exception.PetNotFoundException;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PetRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.util.Map;
import java.util.List;
import java.util.UUID;

@Service
public class PetService {
    @Autowired
    private Cloudinary cloudinary;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    public Pet salvarPet(Pet pet, UUID usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        pet.setUsuario(usuario);
        return petRepository.save(pet);
    }

    public List<Pet> listarPetsPorUsuario(UUID usuarioId) {
        return petRepository.findAllByUsuario_Id(usuarioId);
    }

    public Pet editarPet(UUID petId, UUID usuarioId, PetUpdateDTO dto) {
        Pet pet = buscarPet(petId);

        validarDono(pet, usuarioId);

        if (dto.getNome() != null) pet.setNome(dto.getNome());
        if (dto.getEspecie() != null) pet.setEspecie(dto.getEspecie());

        return petRepository.save(pet);
    }

    public String atualizarImagemPet(UUID petId, UUID usuarioId, MultipartFile file) throws IOException {
        Pet pet = buscarPet(petId);
        validarDono(pet, usuarioId);

        if (pet.getImagemPublicId() != null) {
            cloudinary.uploader().destroy(pet.getImagemPublicId(), ObjectUtils.emptyMap());
        }

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "pets/" + petId,
                        "overwrite", true,
                        "resource_type", "image"
                ));

        pet.setImagemUrl(uploadResult.get("secure_url").toString());
        pet.setImagemPublicId(uploadResult.get("public_id").toString());
        petRepository.save(pet);

        return pet.getImagemUrl();
    }

    private void excluirImagemDoCloud(UUID petId, Pet pet) {

        if (pet.getImagemPublicId() != null) {
            try {
                cloudinary.uploader().destroy(pet.getImagemPublicId(), ObjectUtils.emptyMap());
            } catch (Exception e) {
                System.out.println("Erro ao remover imagem do Cloudinary: " + e.getMessage());
            }
        }


        try {
            cloudinary.api().deleteFolder("pets/" + petId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.out.println("Pasta ainda não está vazia ou não pode ser deletada: " + e.getMessage());
        }
    }

    public void excluirPetDoUsuario(UUID usuarioId, UUID petId) {
        Pet pet = buscarPet(petId);

        validarDono(pet, usuarioId);
        excluirImagemDoCloud(petId, pet);
        petRepository.delete(pet);
    }

    private Usuario buscarUsuario(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado."));
    }

    private Pet buscarPet(UUID petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException("Pet não encontrado."));
    }

    private void validarDono(Pet pet, UUID usuarioId) {
        if (!pet.getUsuario().getId().equals(usuarioId)) {
            throw new SemPermissaoException("Você não tem permissão para alterar esse pet.");
        }
    }
}
