
package app.pet_pode_back.service;

import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PetRepository;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.util.StringUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlantaService {


    @Autowired
    private PlantaRepository plantaRepository;

    public Plantas cadastrar(@Valid Plantas plantas) {

        plantaRepository.save(plantas);
        return plantas;
    }

    public List<Plantas> listarTodos() {
        return plantaRepository.findAll();
    }


    public PlantaService(PlantaRepository plantaRepository) {
        this.plantaRepository = plantaRepository;
    }

    public List<Plantas> buscarPlantas(String termo, Pet pet) {
        Set<Plantas> resultado = new HashSet<>();

        String termoNormalizado = StringUtils.normalize(termo);

        resultado.addAll(plantaRepository.findByNomePopularContainingIgnoreCase(termo));
        resultado.addAll(plantaRepository.findByNomeCientificoContainingIgnoreCase(termo));

        if (termoNormalizado.contains("canin") ||
                termoNormalizado.contains("cao") ||
                termoNormalizado.contains("cachorr")) {

            resultado.addAll(plantaRepository.findByToxicaParaCaninosTrue());
        }

        else if (termoNormalizado.contains("felin") ||
                termoNormalizado.contains("gato")) {

            resultado.addAll(plantaRepository.findByToxicaParaFelinosTrue());
        }


        List<Plantas> todas = plantaRepository.findAll();

        for (Plantas planta : todas) {
            String nomePopular = StringUtils.normalize(planta.getNomePopular());
            String nomeCientifico = StringUtils.normalize(planta.getNomeCientifico());

            if (nomePopular.contains(termoNormalizado) || nomeCientifico.contains(termoNormalizado)) {
                resultado.add(planta);
            }
        }

        if (pet != null && pet.getNome() != null && pet.getEspecie() != null) {
            String nomePetNormalizado = StringUtils.normalize(pet.getNome());
            String especiePet = pet.getEspecie().toUpperCase();

            if (termoNormalizado.contains(nomePetNormalizado)) {
                if (especiePet.contains("CANINO")) {
                    resultado.addAll(plantaRepository.findByToxicaParaCaninosTrue());
                } else if (especiePet.contains("FELINO")) {
                    resultado.addAll(plantaRepository.findByToxicaParaFelinosTrue());
                }
            }
        }

        return new ArrayList<>(resultado);
    }

    public void remover(UUID id) {
        Optional<Plantas> busca = plantaRepository.findById(id);
        if (busca.isPresent()) {
            plantaRepository.delete(busca.get());
        } else {
            throw new RegistroNaoEncontradoException("planta nao encontrada");
        }
    }
}
