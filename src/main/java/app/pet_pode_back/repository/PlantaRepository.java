
package app.pet_pode_back.repository;

import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlantaRepository extends JpaRepository<Plantas, UUID> {
    List<Plantas> findByNomePopularContainingIgnoreCase(String nomePopular);
    List<Plantas> findByNomeCientificoContainingIgnoreCase(String nomeCientifico);
    List<Plantas> findByToxicaParaCaninosTrue();
    List<Plantas> findByToxicaParaFelinosTrue();
}
