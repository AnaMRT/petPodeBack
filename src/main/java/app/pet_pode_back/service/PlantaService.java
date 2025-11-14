
package app.pet_pode_back.service;

import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.util.StringUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class PlantaService {
    @Autowired
    private PlantaRepository plantaRepository;
    public PlantaService(PlantaRepository plantaRepository) {
        this.plantaRepository = plantaRepository;
    }
    public Plantas cadastrar(@Valid Plantas plantas) {
        plantaRepository.save(plantas);
        return plantas;
    }
    public List<Plantas> listarTodos() {
        return plantaRepository.findAll();
    }
    public List<Plantas> buscarPlantas(String termo, Pet pet) {
        String termoNormalizado = StringUtils.normalize(termo.trim());
        Set<Plantas> resultado = new HashSet<>();

        List<Plantas> todas = plantaRepository.findAll();
        for (Plantas planta : todas) {
            if (match(planta, termoNormalizado)) {
                resultado.add(planta);
            }
        }

        if (isTermoCanino(termoNormalizado)) {
            resultado.addAll(plantaRepository.findByToxicaParaCaninosTrue());
        }

        if (isTermoFelino(termoNormalizado)) {
            resultado.addAll(plantaRepository.findByToxicaParaFelinosTrue());
        }

        if (pet != null && matchPet(termoNormalizado, pet)) {
            if (pet.getEspecie().toUpperCase().contains("CANINO")) {
                resultado.addAll(plantaRepository.findByToxicaParaCaninosTrue());
            } else if (pet.getEspecie().toUpperCase().contains("FELINO")) {
                resultado.addAll(plantaRepository.findByToxicaParaFelinosTrue());
            }
        }

        return new ArrayList<>(resultado);
    }

    private boolean match(Plantas planta, String termoNormalizado) {
        return StringUtils.normalize(planta.getNomePopular()).contains(termoNormalizado)
                || StringUtils.normalize(planta.getNomeCientifico()).contains(termoNormalizado);
    }
    private boolean matchPet(String termoNormalizado, Pet pet) {
        return termoNormalizado.contains(StringUtils.normalize(pet.getNome()));
    }
    private boolean isTermoCanino(String termo) {
        return termo.contains("canin") ||
                termo.contains("cao") ||
                termo.contains("cachorr");
    }
    private boolean isTermoFelino(String termo) {
        return termo.contains("felin") ||
                termo.contains("gat");
    }
    public void remover(UUID id) {
        Plantas planta = plantaRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Planta não encontrada"));
        plantaRepository.delete(planta);
    }
}
