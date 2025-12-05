
package app.pet_pode_back.controller;

import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.security.JwtUtil;
import app.pet_pode_back.service.PlantaService;
import app.pet_pode_back.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "plantas")
public class PlantasController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PlantaService plantaService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Plantas>> listar() {
        return ResponseEntity.ok(plantaService.listarTodos());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Plantas>> buscar(
            @RequestParam(value = "termo", defaultValue = "") String termo,
            @RequestHeader("Authorization") String token
    ) {
        String jwt = token.replace("Bearer ", "");
        UUID usuarioId = jwtUtil.extrairUsuarioId(jwt);

        Usuario usuario = usuarioService.buscarUsuarioPorId(usuarioId);
        Pet pet = usuario.getPets().stream().findFirst().orElse(null);

        return ResponseEntity.ok(plantaService.buscarPlantas(termo, pet));
    }
}


