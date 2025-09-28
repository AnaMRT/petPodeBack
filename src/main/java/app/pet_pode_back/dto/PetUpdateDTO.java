package app.pet_pode_back.dto;

import java.util.Objects;

public class PetUpdateDTO {

    private String nome;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PetUpdateDTO that = (PetUpdateDTO) o;
        return Objects.equals(nome, that.nome) && Objects.equals(especie, that.especie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, especie);
    }
}
