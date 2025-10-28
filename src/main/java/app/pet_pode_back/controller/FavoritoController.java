package app.pet_pode_back.controller;

import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/favoritos")
public class FavoritoController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PlantaRepository plantaRepo;

    @PutMapping("/{plantaId}")
    public ResponseEntity<?> adicionarFavorito(
            @PathVariable UUID plantaId,
            Authentication authentication) {

        Usuario usuario = usuarioRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Plantas planta = plantaRepo.findById(plantaId)
                .orElseThrow(() -> new RuntimeException("Planta não encontrada"));

        usuario.addFavorito(planta);
        usuarioRepo.save(usuario);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{plantaId}")
    public ResponseEntity<?> removerFavorito(
            @PathVariable UUID plantaId,
            Authentication authentication) {

        Usuario usuario = usuarioRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Plantas planta = plantaRepo.findById(plantaId)
                .orElseThrow(() -> new RuntimeException("Planta não encontrada"));

        usuario.removeFavorito(planta);
        usuarioRepo.save(usuario);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Set<Plantas>> listarFavoritos(Authentication authentication) {
        Usuario usuario = usuarioRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(usuario.getFavoritos());
    }
}
