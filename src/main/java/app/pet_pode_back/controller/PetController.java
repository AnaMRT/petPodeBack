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

@Controller
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

    @PutMapping("/{id}")
    public ResponseEntity<Pet> editarPet(
            @PathVariable("id") UUID petId,
            @RequestBody PetUpdateDTO dto,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            String token = authorizationHeader.startsWith("Bearer ") ?
                    authorizationHeader.substring(7) : authorizationHeader;

            UUID usuarioId = JwtUtil.extrairUsuarioId(token.trim());

            Pet petAtualizado = petService.editarPet(usuarioId, petId, dto);
            return ResponseEntity.ok(petAtualizado);

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerPet(
            @PathVariable("id") UUID petId,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            String token = authorizationHeader.startsWith("Bearer ") ?
                    authorizationHeader.substring(7) : authorizationHeader;

            UUID usuarioId = JwtUtil.extrairUsuarioId(token.trim());

            petService.removerPet(petId, usuarioId);
            return ResponseEntity.noContent().build();

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (PetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }


    @GetMapping
    public ResponseEntity<List<Pet>> listarPets(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        UUID usuarioId = JwtUtil.extrairUsuarioId(jwt);

        List<Pet> pets = petService.listarPetsPorUsuario(usuarioId);
        return ResponseEntity.ok(pets);
    }

}






