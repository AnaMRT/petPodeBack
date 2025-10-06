
package app.pet_pode_back.controller;

import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.security.JwtUtil;
import app.pet_pode_back.service.Petservice;
import app.pet_pode_back.service.PlantaService;
import app.pet_pode_back.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "plantas")
public class PlantaController {


    @Autowired
    private PlantaService plantaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PlantaRepository plantaRepository;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Plantas>> get() {
        return ResponseEntity.status(HttpStatus.OK).body(plantaService.listarTodos());
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Plantas> adicionar(@RequestBody @Valid Plantas plantas) {
        return ResponseEntity.status(HttpStatus.CREATED).body(plantaService.cadastrar(plantas));
    }


    @GetMapping("/search")
    public ResponseEntity<List<Plantas>> search(
            @RequestParam(value = "termo", required = false, defaultValue = "") String termo,
            @RequestHeader("Authorization") String token
    ) {
        String jwt = token.replace("Bearer ", "");
        UUID usuarioId = JwtUtil.extrairUsuarioId(jwt);

        Usuario usuario = usuarioService.buscarUsuarioPorId(usuarioId);
        Pet pet = usuario.getPets().stream().findFirst().orElse(null);

        List<Plantas> plantas = plantaService.buscarPlantas(termo, pet);
        return ResponseEntity.ok(plantas);
    }


    @DeleteMapping(value = { "{id}" },
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Plantas> remover(@PathVariable("id") UUID id) {
        plantaService.remover(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}


