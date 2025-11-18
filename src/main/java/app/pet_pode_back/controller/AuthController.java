package app.pet_pode_back.controller;

import app.pet_pode_back.dto.LoginRequest;
import app.pet_pode_back.dto.ResetPasswordDTO;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.service.AuthService;
import app.pet_pode_back.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest dto) {
        return ResponseEntity.ok(Collections.singletonMap("token", authService.login(dto)));
    }
    @PostMapping("/cadastro")
    public ResponseEntity<?> register(@Valid @RequestBody Usuario usuario) {
        return ResponseEntity.ok(Collections.singletonMap("token", authService.registrar(usuario)));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) {
        try {
            authService.solicitarRedefinicaoSenha(email);
            return ResponseEntity.ok("Código de redefinição de senha enviado por e-mail.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDTO dto) {
        try {
            authService.redefinirSenha(dto.getCodigo(), dto.getNovaSenha());
            return ResponseEntity.ok("Senha redefinida com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
