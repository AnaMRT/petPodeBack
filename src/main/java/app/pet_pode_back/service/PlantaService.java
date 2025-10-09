
package app.pet_pode_back.service;

import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PetRepository;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.repository.UsuarioRepository;
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


        resultado.addAll(plantaRepository.findByNomePopularContainingIgnoreCase(termo));


        resultado.addAll(plantaRepository.findByNomeCientificoContainingIgnoreCase(termo));


        if ("CANINO".equalsIgnoreCase(termo)) {
            resultado.addAll(plantaRepository.findByToxicaParaCaninosTrue());
        } else if ("FELINO".equalsIgnoreCase(termo)) {
            resultado.addAll(plantaRepository.findByToxicaParaFelinosTrue());
        }


        if (pet != null && pet.getNome().equalsIgnoreCase(termo)) {
            if ("CANINO".equalsIgnoreCase(pet.getEspecie())) {
                resultado.addAll(plantaRepository.findByToxicaParaCaninosTrue());
            } else if ("FELINO".equalsIgnoreCase(pet.getEspecie())) {
                resultado.addAll(plantaRepository.findByToxicaParaFelinosTrue());
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
