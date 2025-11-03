package app.pet_pode_back.controller;

import app.pet_pode_back.dto.PetUpdateDTO;
import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.exception.PermissionDeniedException;
import app.pet_pode_back.exception.PetNotFoundException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.security.JwtUtil;
import app.pet_pode_back.service.UsuarioService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.util.*;

import app.pet_pode_back.service.UsuarioService;


import java.util.UUID;

import static com.cloudinary.AccessControlRule.AccessType.token;

@Controller
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
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
    public ResponseEntity<?> atualizarImagem(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {

            String token = authorizationHeader.replace("Bearer ", "").trim();
            UUID usuarioId = JwtUtil.extrairUsuarioId(token);


            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("Arquivo de imagem não enviado");
            }

            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body("Arquivo enviado não é uma imagem");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("Arquivo muito grande. Máximo 5MB");
            }


            String imagemUrl = usuarioService.atualizarImagemUsuario(usuarioId, file);


            return ResponseEntity.ok(Map.of("imagemUrl", imagemUrl));

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token inválido ou expirado");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao fazer upload da imagem: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar imagem do usuário: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro inesperado: " + e.getMessage());
        }
    }

    @GetMapping("/logado")
    public ResponseEntity<?> getUsuarioLogado(HttpServletRequest request) {
        System.out.println("=== [GET /usuario/logado] Buscando usuário logado ===");

        String authHeader = request.getHeader("Authorization");
        System.out.println("Header Authorization recebido: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println(" Token não enviado ou formato incorreto");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token não enviado ou inválido");
        }

        String token = authHeader.replace("Bearer ", "").trim();
        System.out.println("Token extraído: " + token);

        try {
            UUID usuarioId = JwtUtil.extrairUsuarioId(token);
            System.out.println(" UUID extraído do token: " + usuarioId);

            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

            if (usuarioOpt.isEmpty()) {
                System.out.println(" UUID do token não encontrado no banco: " + usuarioId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuário não encontrado");
            }

            Usuario usuario = usuarioOpt.get();
            System.out.println("✅ Usuário encontrado: " + usuario.getNome() + " (" + usuario.getEmail() + ")");

            Map<String, Object> usuarioMap = new HashMap<>();
            usuarioMap.put("id", usuario.getId());
            usuarioMap.put("nome", usuario.getNome());
            usuarioMap.put("email", usuario.getEmail());
            usuarioMap.put("imagemUrl", usuario.getImagemUrl());

            System.out.println("Retornando dados do usuário logado");
            return ResponseEntity.ok(usuarioMap);

        } catch (io.jsonwebtoken.JwtException e) {
            System.out.println(" Erro ao validar token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token inválido ou expirado");
        } catch (Exception e) {
            System.out.println(" Erro inesperado ao processar token: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro inesperado");
        }
    }
}
