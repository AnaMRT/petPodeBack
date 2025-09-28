package app.pet_pode_back.service;

import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.dto.UsuarioUpdateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setNome("Teste");
        usuario.setEmail("teste@email.com");
        usuario.setSenha("senha123");
        usuario.setId(UUID.randomUUID());
    }

    @Test
    void cadastrarUsuarioComSucesso() {
        when(passwordEncoder.encode(usuario.getSenha())).thenReturn("senhaCriptografada");
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario usuarioSalvo = usuarioService.cadastrar(usuario);

        assertNotNull(usuarioSalvo);
        assertEquals("Teste", usuarioSalvo.getNome());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void cadastrarUsuarioFalhaNoBanco() {
        when(passwordEncoder.encode(usuario.getSenha())).thenReturn("senhaCriptografada");
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenThrow(new RuntimeException("Erro ao salvar usuário"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.cadastrar(usuario);
        });

        assertEquals("Erro ao salvar usuário", exception.getMessage());
    }



    @Test
    void removerUsuarioComSucesso() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        usuarioService.remover(usuarioId);

        verify(usuarioRepository, times(1)).delete(usuario);
    }

    @Test
    void removerUsuarioNaoEncontrado() {
        UUID usuarioId = UUID.randomUUID();

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.remover(usuarioId);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(usuarioRepository, times(0)).delete(any(Usuario.class));
    }

    @Test
    void removerUsuarioFalhaNoBanco() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        doThrow(new RuntimeException("Erro ao remover usuário")).when(usuarioRepository).delete(usuario);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.remover(usuarioId);
        });

        assertEquals("Erro ao remover usuário", exception.getMessage());
    }


    @Test
    void editarUsuarioComSucesso() {
        UUID usuarioId = usuario.getId();
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNome("Novo Nome");
        dto.setEmail("novo@email.com");
        dto.setSenha("novaSenha");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha")).thenReturn("senhaCriptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario usuarioEditado = usuarioService.editarUsuario(usuarioId, dto);

        assertNotNull(usuarioEditado);
        assertEquals("Novo Nome", usuarioEditado.getNome());
        assertEquals("novo@email.com", usuarioEditado.getEmail());
        assertEquals("senhaCriptografada", usuarioEditado.getSenha());
        verify(usuarioRepository, times(1)).save(usuario);
    }


    @Test
    void deveListarUsuariosComSucesso() {
        List<Usuario> listaUsuarios = new ArrayList<>();
        Usuario usuario1 = new Usuario();
        usuario1.setId(UUID.randomUUID());
        usuario1.setNome("Teste");
        usuario1.setEmail("teste@email.com");
        listaUsuarios.add(usuario1);

        when(usuarioRepository.findAll()).thenReturn(listaUsuarios);

        List<Usuario> retorno = usuarioService.listarTodos();

        verify(usuarioRepository, times(1)).findAll();
        assertEquals(1, retorno.size(), "Não retornou o tamanho correto");
        assertEquals(listaUsuarios.get(0).getId(), retorno.get(0).getId(), "Não retornou o usuário correto");
    }

    @Test
    void deveRetornarUsuarioSeEmailNaoExistir() {
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());

        Usuario resultado = usuarioService.verificarEmailExistente(usuario);

        assertNotNull(resultado);
        assertEquals("teste@email.com", resultado.getEmail());
        assertEquals("Teste", resultado.getNome());

        verify(usuarioRepository, times(1)).findByEmail(usuario.getEmail());
    }
}
