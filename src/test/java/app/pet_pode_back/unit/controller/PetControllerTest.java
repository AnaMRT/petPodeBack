package app.pet_pode_back.unit.controller;

import app.pet_pode_back.controller.PetController;
import app.pet_pode_back.dto.PetUpdateDTO;
import app.pet_pode_back.exception.PetNotFoundException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.exception.SemPermissaoException;
import app.pet_pode_back.exception.handler.RestExceptionHandler;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.service.PetService;
import app.pet_pode_back.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PetControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private PetController petController;

    @Mock
    private PetService petService;

    @Mock
    private JwtUtil jwtUtil;

    private ObjectMapper mapper = new ObjectMapper();

    private UUID usuarioId;
    private UUID petId;

    private Pet pet;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders
                .standaloneSetup(petController)
                .setControllerAdvice(new RestExceptionHandler())
                .build();


        usuarioId = UUID.randomUUID();
        petId = UUID.randomUUID();

        pet = new Pet();
        pet.setId(petId);
        pet.setNome("Rex");
        pet.setEspecie("Canino");

        when(jwtUtil.extrairUsuarioId(anyString())).thenReturn(usuarioId);
    }


    @Test
    void deveCadastrarPetComSucesso() throws Exception {
        when(petService.salvarPet(any(Pet.class), eq(usuarioId))).thenReturn(pet);

        mockMvc.perform(post("/pet")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(pet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(petId.toString()))
                .andExpect(jsonPath("$.nome").value("Rex"))
                .andExpect(jsonPath("$.especie").value("Canino"));

    }

    @Test
    void deveRetornarBadRequestQuandoPetNomeInvalidoNoCadastro() throws Exception {
        Pet petInvalido = new Pet();
        petInvalido.setNome("A");

        mockMvc.perform(post("/pet")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(petInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem",
                        org.hamcrest.Matchers.containsString("O nome deve ter entre 2 e 100 caracteres")));
    }

    @Test
    void deveRetornarNotFoundQuandoUsuarioNaoExisteAoCadastrar() throws Exception {
        when(petService.salvarPet(any(), any()))
                .thenThrow(new RegistroNaoEncontradoException("Usuário não encontrado."));

        mockMvc.perform(post("/pet")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(pet)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado."));
    }


    @Test
    void deveListarPetsDoUsuario() throws Exception {
        when(petService.listarPetsPorUsuario(usuarioId)).thenReturn(List.of(pet));

        mockMvc.perform(get("/pet")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(petId.toString()));
    }

    @Test
    void deveExcluirPetComSucesso() throws Exception {
        mockMvc.perform(delete("/pet/" + petId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());

        verify(petService).excluirPetDoUsuario(usuarioId, petId);
    }

    @Test
    void deveRetornarNotFoundAoExcluirPetInexistente() throws Exception {
        doThrow(new PetNotFoundException("Pet não encontrado."))
                .when(petService).excluirPetDoUsuario(any(), any());

        mockMvc.perform(delete("/pet/" + petId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Pet não encontrado."));
    }

    @Test
    void deveRetornarForbiddenAoExcluirPetDeOutroUsuario() throws Exception {
        doThrow(new SemPermissaoException("Você não tem permissão para alterar esse pet."))
                .when(petService).excluirPetDoUsuario(any(), any());

        mockMvc.perform(delete("/pet/" + petId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Você não tem permissão para alterar esse pet."));
    }


    @Test
    void deveEditarPetComSucesso() throws Exception {
        PetUpdateDTO dto = new PetUpdateDTO();
        dto.setNome("Novo nome");
        dto.setEspecie("Felino");

        when(petService.editarPet(eq(petId), eq(usuarioId), any(PetUpdateDTO.class)))
                .thenReturn(pet);

        mockMvc.perform(put("/pet/" + petId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(petId.toString()))
                .andExpect(jsonPath("$.nome").value("Rex"));
    }

    @Test
    void deveRetornarBadRequestQuandoNomeInvalidoNaEdicao() throws Exception {

        PetUpdateDTO dto = new PetUpdateDTO();
        dto.setNome("A");

        mockMvc.perform(put("/pet/" + petId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem",
                        org.hamcrest.Matchers.containsString("O nome deve ter entre 2 e 100 caracteres")));
    }

    @Test
    void naoDeveEditarPetDeOutroUsuario() throws Exception {
        PetUpdateDTO dto = new PetUpdateDTO();
        dto.setNome("NovoNome");
        dto.setEspecie("Felino");

        doThrow(new SemPermissaoException("Você não tem permissão para alterar esse pet."))
                .when(petService).editarPet(any(), eq(usuarioId), any());

        mockMvc.perform(put("/pet/" + petId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Você não tem permissão para alterar esse pet."));
    }


    @Test
    void deveRetornarNotFoundQuandoPetNaoExisteNoEditar() throws Exception {
        when(petService.editarPet(any(), any(), any()))
                .thenThrow(new PetNotFoundException("Pet não encontrado."));

        PetUpdateDTO dto = new PetUpdateDTO();
        dto.setNome("NovoNome");
        dto.setEspecie("Felino");

        mockMvc.perform(put("/pet/" + petId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Pet não encontrado."));
    }

    @Test
    void deveAtualizarImagemComSucesso() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "pet.jpg", "image/jpeg", "conteudo".getBytes()
        );

        when(petService.atualizarImagemPet(eq(petId), eq(usuarioId), any()))
                .thenReturn("http://imagem.com/pet.jpg");

        mockMvc.perform(multipart("/pet/" + petId + "/imagem")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagemUrl").value("http://imagem.com/pet.jpg"));
    }


    @Test
    void deveRetornarErro500QuandoOcorreErroInterno() throws Exception {
        when(petService.listarPetsPorUsuario(any()))
                .thenThrow(new RuntimeException("Erro inesperado."));

        mockMvc.perform(get("/pet")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Erro inesperado. Tente novamente."));
    }

    @Test
    void deveRetornarNotFoundQuandoPetNaoExisteNoUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "pet.jpg", "image/jpeg", "conteudo".getBytes()
        );

        when(petService.atualizarImagemPet(eq(petId), eq(usuarioId), any()))
                .thenThrow(new PetNotFoundException("Pet não encontrado."));

        mockMvc.perform(multipart("/pet/" + petId + "/imagem")
                        .file(file)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        })
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Pet não encontrado."));
    }

    @Test
    void deveRetornarErroInternoQuandoFalhaUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "pet.jpg", "image/jpeg", "conteudo".getBytes()
        );

        when(petService.atualizarImagemPet(eq(petId), eq(usuarioId), any()))
                .thenThrow(new IOException("Falha no upload"));

        mockMvc.perform(multipart("/pet/" + petId + "/imagem")
                        .file(file)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        })
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Erro inesperado. Tente novamente."));
    }


}
