package app.pet_pode_back.controller;

import app.pet_pode_back.dto.PetUpdateDTO;
import app.pet_pode_back.exception.PermissionDeniedException;
import app.pet_pode_back.exception.PetNotFoundException;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.security.JwtUtil;
import app.pet_pode_back.service.Petservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "pet")
public class PetController {

    @Autowired
    private Petservice petService;

    @Autowired
    private JwtUtil jwtUtil;

    private UUID extrairUsuarioIdDoHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new io.jsonwebtoken.JwtException("Token inválido");
        }
        String token = authorizationHeader.substring(7);
        return JwtUtil.extrairUsuarioId(token);
    }

    @PostMapping
    public ResponseEntity<Pet> cadastrarPet(@RequestBody Pet pet,
                                            @RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        UUID usuarioId = JwtUtil.extrairUsuarioId(jwt);

        Pet novoPet = petService.salvarPet(pet, usuarioId);
        return ResponseEntity.ok(novoPet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarPet(
            @PathVariable("id") UUID petId,
            @RequestBody PetUpdateDTO dto,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            UUID usuarioId = extrairUsuarioIdDoHeader(authorizationHeader);
            Pet petAtualizado = petService.editarPet(usuarioId, petId, dto);
            return ResponseEntity.ok(petAtualizado);

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        } catch (PetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerPet(
            @PathVariable("id") UUID petId,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            UUID usuarioId = extrairUsuarioIdDoHeader(authorizationHeader);
            petService.removerPet(petId, usuarioId);
            return ResponseEntity.noContent().build();

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        } catch (PetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Pet>> listarPets(@RequestHeader("Authorization") String token) {
        UUID usuarioId = extrairUsuarioIdDoHeader(token);
        List<Pet> pets = petService.listarPetsPorUsuario(usuarioId);
        return ResponseEntity.ok(pets);
    }


}






