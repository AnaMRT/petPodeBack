package app.pet_pode_back.integration.service;

import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.service.EmailService;
import app.pet_pode_back.service.PlantaService;
import app.pet_pode_back.util.JwtUtil;
import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PlantaServiceIntegrationTest {

    @Autowired
    private PlantaService plantaService;

    @Autowired
    private PlantaRepository plantaRepository;

    @MockBean
    private Cloudinary cloudinary; // mock do Cloudinary

    @MockBean
    private JwtUtil jwtUtil;


    @MockBean
    private EmailService sendGrid; // mock do SendGrid

    private Plantas planta1;
    private Plantas planta2;

    @BeforeEach
    void setup() {
        // Limpa o banco antes de cada teste
        plantaRepository.deleteAll();

        planta1 = new Plantas();
        planta1.setNomePopular("Samambaia");
        planta1.setNomeCientifico("Nephrolepis exaltata");
        planta1.setToxicaParaCaninos(false);
        planta1.setToxicaParaFelinos(false);

        planta2 = new Plantas();
        planta2.setNomePopular("Comigo-ninguém-pode");
        planta2.setNomeCientifico("Dieffenbachia");
        planta2.setToxicaParaCaninos(true);
        planta2.setToxicaParaFelinos(true);

        plantaRepository.save(planta1);
        plantaRepository.save(planta2);
    }

    @Test
    void deveListarTodasPlantas() {
        List<Plantas> todas = plantaService.listarTodos();
        assertThat(todas).hasSize(2);
    }

    @Test
    void deveBuscarPlantasPorNomePopular() {
        List<Plantas> resultado = plantaService.buscarPlantas("samambaia", null);
        assertThat(resultado).contains(planta1);
    }

    @Test
    void deveBuscarPlantasToxicasParaCaninos() {
        List<Plantas> resultado = plantaService.buscarPlantas("canino", null);
        assertThat(resultado).contains(planta2);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoEncontrar() {
        List<Plantas> resultado = plantaService.buscarPlantas("plantaInexistente", null);
        assertThat(resultado).isEmpty();
    }

    @Test
    void deveBuscarPlantasToxicasConsiderandoPetCanino() {
        Pet pet = new Pet();
        pet.setNome("Rex");
        pet.setEspecie("CANINO");

        List<Plantas> resultado = plantaService.buscarPlantas("", pet);
        assertThat(resultado).contains(planta2);
    }

    @Test
    void deveBuscarPlantasToxicasConsiderandoPetFelino() {
        Pet pet = new Pet();
        pet.setNome("Mimi");
        pet.setEspecie("FELINO");

        List<Plantas> resultado = plantaService.buscarPlantas("", pet);
        assertThat(resultado).contains(planta2);
    }
}
