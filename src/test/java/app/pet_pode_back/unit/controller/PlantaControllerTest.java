package app.pet_pode_back.unit.controller;

import app.pet_pode_back.controller.PlantasController;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.service.PlantaService;
import app.pet_pode_back.service.UsuarioService;
import app.pet_pode_back.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlantaControllerTest {

    @InjectMocks
    private PlantasController plantasController;

    @Mock
    private PlantaService plantaService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void deveListarTodasAsPlantas() {
        List<Plantas> plantas = List.of(new Plantas(), new Plantas());
        when(plantaService.listarTodos()).thenReturn(plantas);

        ResponseEntity<List<Plantas>> resposta = plantasController.listar();

        assertEquals(200, resposta.getStatusCode().value());
        assertEquals(plantas, resposta.getBody());
        verify(plantaService, times(1)).listarTodos();
    }


    @Test
    void deveBuscarPlantasComTokenValido() {
        UUID userId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();

        Usuario usuario = new Usuario();
        Pet pet = new Pet();
        pet.setId(petId);
        pet.setEspecie("Canino");

        usuario.setPets(List.of(pet));

        List<Plantas> resultado = List.of(new Plantas());

        when(jwtUtil.extrairUsuarioId("token123")).thenReturn(userId);
        when(usuarioService.buscarUsuarioPorId(userId)).thenReturn(usuario);
        when(plantaService.buscarPlantas("rosa", pet)).thenReturn(resultado);

        ResponseEntity<List<Plantas>> resposta =
                plantasController.buscar("rosa", "Bearer token123");

        assertEquals(200, resposta.getStatusCode().value());
        assertEquals(resultado, resposta.getBody());

        verify(jwtUtil).extrairUsuarioId("token123");
        verify(usuarioService).buscarUsuarioPorId(userId);
        verify(plantaService).buscarPlantas("rosa", pet);
    }


    @Test
    void deveBuscarMesmoSeUsuarioNaoTemPet() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setPets(Collections.emptyList());

        when(jwtUtil.extrairUsuarioId("token123")).thenReturn(id);
        when(usuarioService.buscarUsuarioPorId(id)).thenReturn(usuario);

        when(plantaService.buscarPlantas("rosa", null))
                .thenReturn(List.of());

        ResponseEntity<List<Plantas>> resposta =
                plantasController.buscar("rosa", "Bearer token123");

        assertEquals(200, resposta.getStatusCode().value());
        assertTrue(resposta.getBody().isEmpty());
    }


    @Test
    void deveListarPlantasVazia() {
        when(plantaService.listarTodos()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Plantas>> resposta = plantasController.listar();

        assertEquals(200, resposta.getStatusCodeValue());
        assertTrue(resposta.getBody().isEmpty());
        verify(plantaService, times(1)).listarTodos();
    }

    @Test
    void deveBuscarPlantasComTermoParcial() {
        UUID id = UUID.randomUUID();

        Usuario usuario = new Usuario();
        Pet pet = new Pet();
        pet.setEspecie("Canino");
        usuario.setPets(List.of(pet));

        List<Plantas> resultado = List.of(new Plantas());

        when(jwtUtil.extrairUsuarioId("token")).thenReturn(id);
        when(usuarioService.buscarUsuarioPorId(id)).thenReturn(usuario);
        when(plantaService.buscarPlantas("azal", pet)).thenReturn(resultado);

        ResponseEntity<List<Plantas>> resposta =
                plantasController.buscar("azal", "Bearer token");

        assertEquals(200, resposta.getStatusCodeValue());
        assertEquals(resultado, resposta.getBody());
    }

    @Test
    void deveBuscarIgnorandoAcentuacao() {
        UUID id = UUID.randomUUID();

        Usuario usuario = new Usuario();
        Pet pet = new Pet();
        pet.setEspecie("Canino");
        usuario.setPets(List.of(pet));

        Plantas planta = new Plantas();
        planta.setNomePopular("Costela-de-Adão");

        when(jwtUtil.extrairUsuarioId("token")).thenReturn(id);
        when(usuarioService.buscarUsuarioPorId(id)).thenReturn(usuario);
        when(plantaService.buscarPlantas("adao", pet)).thenReturn(List.of(planta));

        ResponseEntity<List<Plantas>> resposta =
                plantasController.buscar("adao", "Bearer token");

        assertEquals(200, resposta.getStatusCodeValue());
        assertEquals("Costela-de-Adão", resposta.getBody().get(0).getNomePopular());
    }

    @Test
    void deveLancarExcecaoQuandoTokenMalFormado() {
        UUID id = UUID.randomUUID();

        when(jwtUtil.extrairUsuarioId("tokenInvalido"))
                .thenThrow(new IllegalArgumentException("Token inválido"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> plantasController.buscar("rosa", "tokenInvalido")
        );

        assertEquals("Token inválido", ex.getMessage());
    }

    @Test
    void deveTratarTokenSemBearer() {

        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setPets(Collections.emptyList());

        when(jwtUtil.extrairUsuarioId("abc")).thenReturn(id);
        when(usuarioService.buscarUsuarioPorId(id)).thenReturn(usuario);
        when(plantaService.buscarPlantas("", null)).thenReturn(List.of());

        ResponseEntity<List<Plantas>> resposta =
                plantasController.buscar("", "abc");

        assertEquals(200, resposta.getStatusCode().value());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoForEncontrado() {
        UUID id = UUID.randomUUID();

        when(jwtUtil.extrairUsuarioId("token123")).thenReturn(id);
        when(usuarioService.buscarUsuarioPorId(id))
                .thenThrow(new RegistroNaoEncontradoException("Usuário não encontrado"));

        RegistroNaoEncontradoException ex = assertThrows(
                RegistroNaoEncontradoException.class,
                () -> plantasController.buscar("rosa", "Bearer token123")
        );

        assertEquals("Usuário não encontrado", ex.getMessage());
        verify(plantaService, never()).buscarPlantas(any(), any());
    }


    @Test
    void deveRetornar404QuandoUsuarioNaoExistirAoBuscarPlantas() {
        UUID idInexistente = UUID.randomUUID();

        when(jwtUtil.extrairUsuarioId("token123")).thenReturn(idInexistente);
        when(usuarioService.buscarUsuarioPorId(idInexistente))
                .thenThrow(new RegistroNaoEncontradoException("Usuário não encontrado"));

        RegistroNaoEncontradoException ex = assertThrows(
                RegistroNaoEncontradoException.class,
                () -> plantasController.buscar("rosa", "Bearer token123")
        );

        assertEquals("Usuário não encontrado", ex.getMessage());
        verify(plantaService, never()).buscarPlantas(any(), any());
    }


}
