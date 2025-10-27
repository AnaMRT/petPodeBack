package app.pet_pode_back.controller;

import app.pet_pode_back.dto.PetUpdateDTO;
import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.exception.PermissionDeniedException;
import app.pet_pode_back.exception.PetNotFoundException;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.security.JwtUtil;
import app.pet_pode_back.service.Petservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;


import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "pet")
public class PetController {

    @Autowired
    private Petservice petService;

    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping
    public ResponseEntity<Pet> cadastrarPet(@RequestBody Pet pet,
                                            @RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        UUID usuarioId = JwtUtil.extrairUsuarioId(jwt);

        Pet novoPet = petService.salvarPet(pet, usuarioId);
        return ResponseEntity.ok(novoPet);
    }


    @GetMapping
    public ResponseEntity<List<Pet>> listarPetsDoUsuario(
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "").trim();
            UUID usuarioId = JwtUtil.extrairUsuarioId(token);

            List<Pet> pets = petService.listarPetsPorUsuario(usuarioId);
            return ResponseEntity.ok(pets);

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirPet(
            @PathVariable("id") UUID petId,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            String token = authorizationHeader.replace("Bearer ", "").trim();
            UUID usuarioId = JwtUtil.extrairUsuarioId(token);

            petService.excluirPetDoUsuario(usuarioId, petId);
            return ResponseEntity.noContent().build();

        } catch (PetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pet> editarPet(
            @PathVariable("id") UUID petId,
            @RequestBody PetUpdateDTO dto,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {

            String token = authorizationHeader.replace("Bearer ", "").trim();
            UUID usuarioId = JwtUtil.extrairUsuarioId(token);


            Pet petEditado = petService.editarPet(petId, usuarioId, dto);

            return ResponseEntity.ok(petEditado);

        } catch (PetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/{id}/imagem")
    public ResponseEntity<?> atualizarImagemPet(
            @PathVariable("id") UUID petId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            String token = authorizationHeader.replace("Bearer ", "").trim();
            UUID usuarioId = JwtUtil.extrairUsuarioId(token);

            // Valida se o arquivo é uma imagem
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("Arquivo de imagem não enviado");
            }

            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body("Arquivo enviado não é uma imagem");
            }

            if (file.getSize() > 5 * 1024 * 1024) { // limite 5MB
                return ResponseEntity.badRequest().body("Arquivo muito grande. Máximo 5MB");
            }

            // Faz upload para Cloudinary e atualiza URL no pet
            String imagemUrl = petService.atualizarImagemPet(petId, usuarioId, file);

            return ResponseEntity.ok(Map.of("imagemUrl", imagemUrl));

        } catch (PetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido ou expirado");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao fazer upload da imagem: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro inesperado: " + e.getMessage());
        }
    }

}






