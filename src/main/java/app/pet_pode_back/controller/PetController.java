package app.pet_pode_back.controller;

import app.pet_pode_back.dto.PetUpdateDTO;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.security.JwtUtil;
import app.pet_pode_back.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "pet")
public class PetController {

    @Autowired
    private PetService petService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Pet> cadastrarPet(
            @RequestBody Pet pet,
            @RequestHeader("Authorization") String token) {

        UUID usuarioId = jwtUtil.extrairUsuarioId(token.replace("Bearer ", ""));
        return ResponseEntity.ok(petService.salvarPet(pet, usuarioId));
    }
    @GetMapping
    public ResponseEntity<List<Pet>> listarPets(
            @RequestHeader("Authorization") String token) {

        UUID usuarioId = jwtUtil.extrairUsuarioId(token.replace("Bearer ", ""));
        return ResponseEntity.ok(petService.listarPetsPorUsuario(usuarioId));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token) {

        UUID usuarioId = jwtUtil.extrairUsuarioId(token.replace("Bearer ", ""));
        petService.excluirPetDoUsuario(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<Pet> editar(
            @PathVariable UUID id,
            @RequestBody PetUpdateDTO dto,
            @RequestHeader("Authorization") String token) {

        UUID usuarioId = jwtUtil.extrairUsuarioId(token.replace("Bearer ", ""));
        return ResponseEntity.ok(petService.editarPet(id, usuarioId, dto));
    }
    @PutMapping("/{id}/imagem")
    public ResponseEntity<?> atualizarImagem(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) throws IOException {

        UUID usuarioId = jwtUtil.extrairUsuarioId(token.replace("Bearer ", ""));
        String url = petService.atualizarImagemPet(id, usuarioId, file);
        return ResponseEntity.ok(Map.of("imagemUrl", url));
    }

}






