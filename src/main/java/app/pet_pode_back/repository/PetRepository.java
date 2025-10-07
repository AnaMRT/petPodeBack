package app.pet_pode_back.repository;

import app.pet_pode_back.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PetRepository extends JpaRepository<Pet, UUID> {

    List<Pet> findAllByUsuarioId(UUID usuarioId);

}
