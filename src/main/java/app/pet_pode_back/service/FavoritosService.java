package app.pet_pode_back.service;

import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
@Service
public class FavoritosService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PlantaRepository plantaRepository;

    public void adicionarFavorito(UUID usuarioId, UUID plantaId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado"));

        Plantas planta = plantaRepository.findById(plantaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Planta não encontrada"));

        usuario.addFavorito(planta);
        usuarioRepository.save(usuario);
    }
    public void removerFavorito(UUID usuarioId, UUID plantaId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado"));

        Plantas planta = plantaRepository.findById(plantaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Planta não encontrada"));

        usuario.removeFavorito(planta);
        usuarioRepository.save(usuario);
    }
    public Set<Plantas> listarFavoritos(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado"));
        return usuario.getFavoritos();
    }

}
