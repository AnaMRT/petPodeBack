package app.pet_pode_back.controller;

import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.exception.SemPermissaoException;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.util.JwtUtil;
import app.pet_pode_back.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.cloudinary.Cloudinary;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.util.*;
import java.util.UUID;

@RestController
@RequestMapping(path = "usuario")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private Cloudinary cloudinary;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Usuario>> get() {
        return ResponseEntity.status(HttpStatus.OK).body(usuarioService.listarTodos());
    }
    @PutMapping
    public ResponseEntity<Usuario> editar(
            @RequestBody @Valid UsuarioUpdateDTO dto,
            @RequestHeader("Authorization") String token) {

        UUID usuarioId = extrairId(token);
        Usuario atualizado = usuarioService.editarUsuario(usuarioId, dto);

        return ResponseEntity.ok(atualizado);
    }
    @DeleteMapping
    public ResponseEntity<Void> remover(@RequestHeader("Authorization") String token) {
        UUID usuarioId = extrairId(token);
        usuarioService.remover(usuarioId);
        return ResponseEntity.noContent().build();
    }
    @PutMapping(value = "/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> atualizarImagem(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) throws IOException {

        UUID usuarioId = extrairId(token);
        String url = usuarioService.atualizarImagemUsuario(usuarioId, file);

        return ResponseEntity.ok(Map.of("imagemUrl", url));
    }
    @GetMapping("/logado")
    public ResponseEntity<Map<String, Object>> getUsuarioLogado(
            @RequestHeader("Authorization") String tokenHeader) {

        String token = tokenHeader.replace("Bearer ", "").trim();

        Map<String, Object> usuario = usuarioService.getUsuarioLogado(token);

        return ResponseEntity.ok(usuario);
    }
    private UUID extrairId(String tokenHeader) {
        try {
            String tokenLimpo = tokenHeader.replace("Bearer ", "").trim();
            return jwtUtil.extrairUsuarioId(tokenLimpo);
        } catch (IllegalArgumentException e) {
            throw new SemPermissaoException("Token inválido ou expirado");
        }
    }
}
