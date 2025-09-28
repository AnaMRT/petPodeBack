package app.pet_pode_back.controller;

import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.security.JwtUtil;
import app.pet_pode_back.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(path = "usuario")
public class UsuarioController {

     @Autowired
    private UsuarioService usuarioService;

     @Autowired
     private JwtUtil jwtUtil;

    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<List<Usuario>> get() {
        return ResponseEntity.status(HttpStatus.OK).body(usuarioService.listarTodos());
    }

    @PostMapping( consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Usuario> adicionar(@RequestBody @Valid Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.cadastrar(usuario));
    }

    @PutMapping
    public ResponseEntity<Usuario> editarUsuario(
            @RequestBody UsuarioUpdateDTO dto,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            String token = authorizationHeader;
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            UUID usuarioId = JwtUtil.extrairUsuarioId(token.trim());
            Usuario usuarioEditado = usuarioService.editarUsuario(usuarioId, dto);

            return ResponseEntity.ok(usuarioEditado);

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Usuario> removerUsuario(
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            String token = authorizationHeader;
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            UUID usuarioId = JwtUtil.extrairUsuarioId(token.trim());
            usuarioService.remover(usuarioId);

            return ResponseEntity.noContent().build();

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


}
