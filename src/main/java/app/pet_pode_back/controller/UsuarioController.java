package app.pet_pode_back.controller;

import app.pet_pode_back.dto.PetUpdateDTO;
import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.exception.PermissionDeniedException;
import app.pet_pode_back.exception.PetNotFoundException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Pet;
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
            String token = authorizationHeader.replace("Bearer ", "").trim();
            UUID usuarioId = JwtUtil.extrairUsuarioId(token);

            Usuario usuarioEditado = usuarioService.editarUsuario(usuarioId, dto);
            return ResponseEntity.ok(usuarioEditado);

        } catch (RegistroNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> removerUsuario(
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            String token = authorizationHeader.replace("Bearer ", "").trim();
            UUID usuarioId = JwtUtil.extrairUsuarioId(token);

            usuarioService.remover(usuarioId);
            return ResponseEntity.noContent().build();

        } catch (RegistroNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/imagem")
    public ResponseEntity<String> atualizarImagem(
            @RequestParam("imagemUrl") String imagemUrl,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "").trim();
            UUID usuarioId = JwtUtil.extrairUsuarioId(token);

            usuarioService.atualizarImagemUsuario(usuarioId, imagemUrl);
            return ResponseEntity.ok("Imagem atualizada com sucesso!");
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar imagem");
        }
    }

}
