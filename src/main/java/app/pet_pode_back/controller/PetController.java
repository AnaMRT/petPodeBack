package app.pet_pode_back.controller;

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


    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Pet>> get() {
        return ResponseEntity.status(HttpStatus.OK).body(petService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<Pet> cadastrarPet(@RequestBody Pet pet,
                                            @RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        UUID usuarioId = JwtUtil.extrairUsuarioId(jwt);

        Pet novoPet = petService.salvarPet(pet, usuarioId);
        return ResponseEntity.ok(novoPet);
    }

}






