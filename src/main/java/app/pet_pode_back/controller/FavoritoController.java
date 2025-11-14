package app.pet_pode_back.controller;

import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.service.UsuarioService;
import app.pet_pode_back.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/favoritos")
public class FavoritoController {
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private JwtUtil jwtUtil;
    @PutMapping("/{plantaId}")
    public ResponseEntity<Void> adicionar(@PathVariable UUID plantaId,
                                          @RequestHeader("Authorization") String token) {
        UUID usuarioId = jwtUtil.extrairUsuarioId(token.replace("Bearer ", "").trim());
        usuarioService.adicionarFavorito(usuarioId, plantaId);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{plantaId}")
    public ResponseEntity<Void> remover(@PathVariable UUID plantaId,
                                        @RequestHeader("Authorization") String token) {
        UUID usuarioId = jwtUtil.extrairUsuarioId(token.replace("Bearer ", "").trim());
        usuarioService.removerFavorito(usuarioId, plantaId);
        return ResponseEntity.ok().build();
    }
    @GetMapping
    public ResponseEntity<Set<Plantas>> listar(@RequestHeader("Authorization") String token) {
        UUID usuarioId = jwtUtil.extrairUsuarioId(token.replace("Bearer ", "").trim());
        return ResponseEntity.ok(usuarioService.listarFavoritos(usuarioId));
    }
}
