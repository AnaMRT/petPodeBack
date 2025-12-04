package app.pet_pode_back.integration.service;

import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.service.FavoritosService;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource(locations = "classpath:.env.test")
class FavoritosServiceIntegrationTest {

    @Autowired
    private FavoritosService favoritosService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PlantaRepository plantaRepository;


    private Usuario usuario;
    private Plantas planta;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setNome("Teste Usuario");
        usuario.setEmail("teste@usuario.com");
        usuario.setSenha("123456");
        usuario = usuarioRepository.save(usuario);

        planta = new Plantas();
        planta.setNomeCientifico("Planta Cientifica");
        planta.setNomePopular("Planta Popular");
        planta.setDescricao("Descrição da planta");
        planta.setToxicaParaCaninos(false);
        planta.setToxicaParaFelinos(false);
        planta = plantaRepository.save(planta);
    }

    @Test
    void deveAdicionarFavoritoComSucesso() {
        favoritosService.adicionarFavorito(usuario.getId(), planta.getId());

        Set<Plantas> favoritos = favoritosService.listarFavoritos(usuario.getId());
        assertEquals(1, favoritos.size());
        assertTrue(favoritos.contains(planta));
    }

    @Test
    void deveRemoverFavoritoComSucesso() {
        favoritosService.adicionarFavorito(usuario.getId(), planta.getId());
        favoritosService.removerFavorito(usuario.getId(), planta.getId());

        Set<Plantas> favoritos = favoritosService.listarFavoritos(usuario.getId());
        assertTrue(favoritos.isEmpty());
    }

    @Test
    void deveLancarExcecaoSeUsuarioNaoExistir() {
        UUID idInvalido = UUID.randomUUID();
        RegistroNaoEncontradoException ex = assertThrows(RegistroNaoEncontradoException.class,
                () -> favoritosService.adicionarFavorito(idInvalido, planta.getId()));
        assertEquals("Usuário não encontrado", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoSePlantaNaoExistir() {
        UUID idInvalido = UUID.randomUUID();
        RegistroNaoEncontradoException ex = assertThrows(RegistroNaoEncontradoException.class,
                () -> favoritosService.adicionarFavorito(usuario.getId(), idInvalido));
        assertEquals("Planta não encontrada", ex.getMessage());
    }

    @Test
    void deveAdicionarEManterPersistenciaNoBanco() {
        favoritosService.adicionarFavorito(usuario.getId(), planta.getId());

        Usuario usuarioBanco = usuarioRepository.findById(usuario.getId()).orElseThrow();

        assertEquals(1, usuarioBanco.getFavoritos().size());
        assertTrue(usuarioBanco.getFavoritos().contains(planta));
    }
    @Test
    void deveRetornarListaVaziaQuandoNaoExistemFavoritos() {
        Set<Plantas> favoritos = favoritosService.listarFavoritos(usuario.getId());
        assertTrue(favoritos.isEmpty());
    }
    @Test
    void deveAdicionarMesmaPlantaApenasUmaVez() {
        favoritosService.adicionarFavorito(usuario.getId(), planta.getId());
        favoritosService.adicionarFavorito(usuario.getId(), planta.getId()); // segunda vez

        Set<Plantas> favoritos = favoritosService.listarFavoritos(usuario.getId());
        assertEquals(1, favoritos.size(), "A planta não deve ser duplicada");
        assertTrue(favoritos.contains(planta));
    }

    @Test
    void deveRemoverPlantaQueNaoEstaNosFavoritosSemErro() {
        favoritosService.removerFavorito(usuario.getId(), planta.getId());

        Set<Plantas> favoritos = favoritosService.listarFavoritos(usuario.getId());
        assertTrue(favoritos.isEmpty(), "Favoritos devem continuar vazios");
    }

    @Test
    void deveListarFavoritosComMaisDeUmFavorito() {
        Plantas planta2 = new Plantas();
        planta2.setNomeCientifico("Outra Planta Científica");
        planta2.setNomePopular("Outra Planta Popular");
        planta2.setDescricao("Descrição");
        planta2.setToxicaParaCaninos(false);
        planta2.setToxicaParaFelinos(false);
        planta2 = plantaRepository.save(planta2);

        favoritosService.adicionarFavorito(usuario.getId(), planta.getId());
        favoritosService.adicionarFavorito(usuario.getId(), planta2.getId());

        Set<Plantas> favoritos = favoritosService.listarFavoritos(usuario.getId());
        assertEquals(2, favoritos.size());
        assertTrue(favoritos.contains(planta));
        assertTrue(favoritos.contains(planta2));
    }

    @Test
    void deveLancarErroQuandoPlantaNaoExisteAoRemoverFavorito() {
        UUID plantaInexistente = UUID.randomUUID();

        RegistroNaoEncontradoException ex =
                assertThrows(RegistroNaoEncontradoException.class,
                        () -> favoritosService.removerFavorito(usuario.getId(), plantaInexistente));

        assertEquals("Planta não encontrada", ex.getMessage());
    }
    @Test
    void deveListarFavoritosComSucesso() {
        // Adiciona uma planta como favorito
        favoritosService.adicionarFavorito(usuario.getId(), planta.getId());

        // Lista os favoritos
        Set<Plantas> favoritos = favoritosService.listarFavoritos(usuario.getId());

        // Verifica se o favorito foi retornado corretamente
        assertEquals(1, favoritos.size());
        assertTrue(favoritos.contains(planta));
    }

    @Test
    void deveLancarErroQuandoUsuarioNaoExisteAoListarFavoritos() {
        UUID usuarioInexistente = UUID.randomUUID();

        // Tenta listar favoritos de um usuário inexistente
        RegistroNaoEncontradoException ex = assertThrows(RegistroNaoEncontradoException.class,
                () -> favoritosService.listarFavoritos(usuarioInexistente));

        assertEquals("Usuário não encontrado", ex.getMessage());
    }


}
