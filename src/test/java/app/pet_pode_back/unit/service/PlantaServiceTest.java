package app.pet_pode_back.unit.service;

import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.service.PlantaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PlantaServiceTest {

    @Mock
    private PlantaRepository plantaRepository;

    @InjectMocks
    private PlantaService plantaService;

    private Plantas planta1;
    private Plantas planta2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        planta1 = new Plantas();
        planta1.setNomePopular("Costela-de-Adão");
        planta1.setNomeCientifico("Monstera deliciosa");

        planta2 = new Plantas();
        planta2.setNomePopular("Lírio");
        planta2.setNomeCientifico("Lilium");
    }


    @Test
    void deveEncontrarMesmoQuandoTermoTEMsemAcentoENomeTEMacento() {
        when(plantaRepository.findAll()).thenReturn(List.of(planta1)); // Costela-de-Adão

        List<Plantas> resultado = plantaService.buscarPlantas("adao", null);

        assertThat(resultado).contains(planta1);
    }

    @Test
    void deveIgnorarEspacosAntesEDepoisDoTermo() {
        when(plantaRepository.findAll()).thenReturn(List.of(planta1));

        List<Plantas> resultado = plantaService.buscarPlantas("   costela   ", null);

        assertThat(resultado).contains(planta1);
    }

    @Test
    void naoDeveGerarDuplicacaoQuandoPlantaApareceEmMaisDeUmaRegra() {
        when(plantaRepository.findAll()).thenReturn(List.of(planta1));
        when(plantaRepository.findByToxicaParaCaninosTrue()).thenReturn(List.of(planta1));

        List<Plantas> resultado = plantaService.buscarPlantas("costela", null);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void deveEncontrarPorNomeCientifico() {
        when(plantaRepository.findAll()).thenReturn(List.of(planta1)); // Monstera deliciosa

        List<Plantas> resultado = plantaService.buscarPlantas("deliciosa", null);

        assertThat(resultado).contains(planta1);
    }
    @Test
    void deveRetornarListaVaziaQuandoTermoIndicaEspecieMasNaoHaPlantasToxicas() {
        when(plantaRepository.findAll()).thenReturn(List.of());
        when(plantaRepository.findByToxicaParaFelinosTrue()).thenReturn(List.of());

        List<Plantas> resultado = plantaService.buscarPlantas("gato", null);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveRetornarVazioQuandoTermoPossuiCaracteresInvalidos() {
        when(plantaRepository.findAll()).thenReturn(List.of(planta1, planta2));

        List<Plantas> resultado = plantaService.buscarPlantas("@#$%¨&*", null);

        assertThat(resultado).isEmpty();
    }


    @Test
    void deveListarTodasAsPlantas() {
        when(plantaRepository.findAll()).thenReturn(List.of(planta1, planta2));

        List<Plantas> resultado = plantaService.listarTodos();

        assertThat(resultado).hasSize(2);
        verify(plantaRepository, times(1)).findAll();
    }



    @Test
    void deveBuscarPlantasPorTermoNoNome() {
        when(plantaRepository.findAll()).thenReturn(List.of(planta1, planta2));

        List<Plantas> resultado = plantaService.buscarPlantas("costela", null);

        assertThat(resultado).containsExactly(planta1);
    }


    @Test
    void deveRetornarPlantasToxicasParaCaninosQuandoTermoRelacionadoAcaes() {
        when(plantaRepository.findAll()).thenReturn(List.of());
        when(plantaRepository.findByToxicaParaCaninosTrue()).thenReturn(List.of(planta1));

        List<Plantas> resultado = plantaService.buscarPlantas("cachorro", null);

        assertThat(resultado).containsExactly(planta1);
    }


    @Test
    void deveRetornarPlantasToxicasParaFelinosQuandoTermoRelacionadoAgatos() {
        when(plantaRepository.findAll()).thenReturn(List.of());
        when(plantaRepository.findByToxicaParaFelinosTrue()).thenReturn(List.of(planta1));

        List<Plantas> resultado = plantaService.buscarPlantas("gato", null);

        assertThat(resultado).containsExactly(planta1);
    }


    @Test
    void deveBuscarPlantasRelacionadasAoNomeDoPet() {
        Pet pet = new Pet();
        pet.setNome("Thor");
        pet.setEspecie("CANINO");

        when(plantaRepository.findAll()).thenReturn(List.of(planta1));
        when(plantaRepository.findByToxicaParaCaninosTrue()).thenReturn(List.of(planta2));

        List<Plantas> resultado = plantaService.buscarPlantas("thor", pet);

        assertThat(resultado).contains(planta2);
    }


    @Test
    void deveAdicionarPlantasToxicasParaCaninosQuandoPetForCanino() {
        Pet pet = new Pet();
        pet.setNome("Rex");
        pet.setEspecie("CANINO");

        when(plantaRepository.findAll()).thenReturn(List.of());
        when(plantaRepository.findByToxicaParaCaninosTrue()).thenReturn(List.of(planta1));

        List<Plantas> resultado = plantaService.buscarPlantas("rex", pet);

        assertThat(resultado).containsExactly(planta1);
    }

    @Test
    void deveAdicionarPlantasToxicasParaFelinosQuandoPetForFelino() {
        Pet pet = new Pet();
        pet.setNome("Mimi");
        pet.setEspecie("FELINO");

        when(plantaRepository.findAll()).thenReturn(List.of());
        when(plantaRepository.findByToxicaParaFelinosTrue()).thenReturn(List.of(planta2));

        List<Plantas> resultado = plantaService.buscarPlantas("mimi", pet);

        assertThat(resultado).containsExactly(planta2);
    }


    @Test
    void deveRetornarListaVaziaQuandoNadaEncontrado() {
        when(plantaRepository.findAll()).thenReturn(List.of());

        List<Plantas> resultado = plantaService.buscarPlantas("xxxxxxxx", null);

        assertThat(resultado).isEmpty();
    }



}
