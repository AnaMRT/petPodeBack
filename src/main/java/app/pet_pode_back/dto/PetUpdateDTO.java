package app.pet_pode_back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PetUpdateDTO {
    @NotBlank(message = "Nome nao pode ser nulo")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres.")
    private String nome;
    @NotBlank(message = "Especie nao pode ser nula")
    private String especie;

    public PetUpdateDTO() {
    }
    public PetUpdateDTO(String nome, String especie) {
        this.nome = nome;
        this.especie = especie;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getEspecie() {
        return especie;
    }
    public void setEspecie(String especie) {
        this.especie = especie;
    }

}
