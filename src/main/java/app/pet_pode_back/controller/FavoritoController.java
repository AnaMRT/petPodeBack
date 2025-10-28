package app.pet_pode_back.controller;

import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.security.JwtUtil;
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
    private JwtUtil jwtUtil;


    @Autowired
    private PlantaRepository plantaRepo;

    @PutMapping("/{plantaId}")
    public ResponseEntity<?> adicionarFavorito(
            @PathVariable UUID plantaId,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.replace("Bearer ", "").trim();
        UUID usuarioId = JwtUtil.extrairUsuarioId(token);

        Usuario usuario = usuarioRepo.findById(usuarioId)
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
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.replace("Bearer ", "").trim();
        UUID usuarioId = JwtUtil.extrairUsuarioId(token);

        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Plantas planta = plantaRepo.findById(plantaId)
                .orElseThrow(() -> new RuntimeException("Planta não encontrada"));

        usuario.removeFavorito(planta);
        usuarioRepo.save(usuario);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Set<Plantas>> listarFavoritos(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.replace("Bearer ", "").trim();
        UUID usuarioId = JwtUtil.extrairUsuarioId(token);

        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(usuario.getFavoritos());
    }

}
