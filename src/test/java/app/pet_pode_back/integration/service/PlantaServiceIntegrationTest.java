package app.pet_pode_back.integration.service;

import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.service.PlantaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:.env.test")
class PlantaServiceIntegrationTest {

    @Autowired
    private PlantaService plantaService;

    @Autowired
    private PlantaRepository plantaRepository;

    private Plantas planta1;
    private Plantas planta2;

    @BeforeEach
    void setup() {
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

    @Test
    void deveBuscarPlantasPorNomeCientifico() {
        List<Plantas> resultado = plantaService.buscarPlantas("Nephrolepis", null);
        assertThat(resultado).contains(planta1);
    }

    @Test
    void deveIgnorarEspacosAntesEDepoisDoTermo() {
        List<Plantas> resultado = plantaService.buscarPlantas("   samambaia   ", null);
        assertThat(resultado).contains(planta1);
    }


    @Test
    void naoDeveGerarDuplicacaoQuandoPlantaApareceEmMaisDeUmaRegra() {
        List<Plantas> resultado = plantaService.buscarPlantas("canino", null);
        assertThat(resultado).hasSize(1);
    }

    @Test
    void deveRetornarVazioQuandoTermoPossuiCaracteresInvalidos() {
        List<Plantas> resultado = plantaService.buscarPlantas("@#$%", null);
        assertThat(resultado).isEmpty();
    }

    @Test
    void deveBuscarPlantasRelacionadasAoNomeDoPet() {
        Pet pet = new Pet();
        pet.setNome("Rex");
        pet.setEspecie("CANINO");

        List<Plantas> resultado = plantaService.buscarPlantas("rex", pet);
        assertThat(resultado).contains(planta2);
    }

    @Test
    void listarTodosRetornaVazioQuandoNaoHaPlantas() {
        plantaRepository.deleteAll();
        List<Plantas> resultado = plantaService.listarTodos();
        assertThat(resultado).isEmpty();
    }


    @Test
    void deveEncontrarPlantaIgnorandoAcentos() {
        Plantas acentuada = new Plantas();
        acentuada.setNomePopular("Costela-de-Adão");
        acentuada.setNomeCientifico("Monstera deliciosa");
        acentuada.setToxicaParaFelinos(true);
        acentuada.setToxicaParaCaninos(true);


        plantaRepository.save(acentuada);

        List<Plantas> resultado = plantaService.buscarPlantas("adao", null);
        assertThat(resultado).contains(acentuada);
    }

    @Test
    void deveRetornarPlantasToxicasParaCaninosQuandoTermoRelacionadoAcaes() {
        List<Plantas> resultado = plantaService.buscarPlantas("cachorro", null);

        assertThat(resultado).contains(planta2);
    }


    @Test
    void naoDeveGerarDuplicacaoComPetERegrasDeToxicidade() {
        planta2.setNomePopular("rex");
        plantaRepository.save(planta2);

        Pet pet = new Pet();
        pet.setNome("Rex");
        pet.setEspecie("CANINO");

        List<Plantas> resultado = plantaService.buscarPlantas("rex", pet);

        assertThat(resultado).hasSize(1).contains(planta2);
    }


}
