package app.pet_pode_back.unit.service;

import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.service.FavoritosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoritosServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PlantaRepository plantaRepository;

    @InjectMocks
    private FavoritosService favoritosService;

    private UUID usuarioId;
    private UUID plantaId;
    private Usuario usuario;
    private Plantas planta;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        usuarioId = UUID.randomUUID();
        plantaId = UUID.randomUUID();

        usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setFavoritos(new HashSet<>());

        planta = new Plantas();
        planta.setId(plantaId);
    }



    @Test
    void deveAdicionarFavoritoComSucesso() {
        // Arrange
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(plantaRepository.findById(plantaId)).thenReturn(Optional.of(planta));


        favoritosService.adicionarFavorito(usuarioId, plantaId);

        assertTrue(usuario.getFavoritos().contains(planta));
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveLancarErroQuandoUsuarioNaoExisteAoAdicionarFavorito() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class,
                () -> favoritosService.adicionarFavorito(usuarioId, plantaId));
    }

    @Test
    void deveLancarErroQuandoPlantaNaoExisteAoAdicionarFavorito() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(plantaRepository.findById(plantaId)).thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class,
                () -> favoritosService.adicionarFavorito(usuarioId, plantaId));
    }


    @Test
    void deveRemoverFavoritoComSucesso() {
        usuario.getFavoritos().add(planta);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(plantaRepository.findById(plantaId)).thenReturn(Optional.of(planta));

        favoritosService.removerFavorito(usuarioId, plantaId);

        assertFalse(usuario.getFavoritos().contains(planta));
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveLancarErroQuandoUsuarioNaoExisteAoRemoverFavorito() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class,
                () -> favoritosService.removerFavorito(usuarioId, plantaId));
    }

    @Test
    void deveLancarErroQuandoPlantaNaoExisteAoRemoverFavorito() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(plantaRepository.findById(plantaId)).thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class,
                () -> favoritosService.removerFavorito(usuarioId, plantaId));
    }



    @Test
    void deveListarFavoritosComSucesso() {
        usuario.getFavoritos().add(planta);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        Set<Plantas> favoritos = favoritosService.listarFavoritos(usuarioId);

        assertEquals(1, favoritos.size());
        assertTrue(favoritos.contains(planta));
    }

    @Test
    void deveLancarErroQuandoUsuarioNaoExisteAoListarFavoritos() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(RegistroNaoEncontradoException.class,
                () -> favoritosService.listarFavoritos(usuarioId));
    }
}
