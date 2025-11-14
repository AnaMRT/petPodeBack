
package app.pet_pode_back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "plantas")
public class Plantas {
    @Column
    @NotBlank(message = "O nome popular é obrigatório.")
    private String nomePopular;
    @Column
    @NotBlank(message = "O nome científico é obrigatório.")
    private String nomeCientifico;
    @Column(length = 1000)
    private String descricao;
    @Column
    @NotNull(message = "Informe se a planta é tóxica para caninos.")
    private Boolean toxicaParaCaninos;
    @Column
    @NotNull(message = "Informe se a planta é tóxica para felinos.")
    private Boolean toxicaParaFelinos;
    @Column
    private String imagemUrl;
    @Id
    @GeneratedValue
    @Column
    private UUID id;
    public Plantas() {
    }
    public Plantas(String nomePopular, String nomeCientifico, String descricao, Boolean toxicaParaFelinos, Boolean toxicaParaCaninos, String imagemUrl, UUID id) {
        this.nomePopular = nomePopular;
        this.nomeCientifico = nomeCientifico;
        this.descricao = descricao;
        this.toxicaParaFelinos = toxicaParaFelinos;
        this.toxicaParaCaninos = toxicaParaCaninos;
        this.imagemUrl = imagemUrl;
        this.id = id;
    }

    public String getNomePopular() {
        return nomePopular;
    }

    public String getNomeCientifico() {
        return nomeCientifico;
    }

    public String getDescricao() {
        return descricao;
    }

    public Boolean getToxicaParaCaninos() {
        return toxicaParaCaninos;
    }

    public Boolean getToxicaParaFelinos() {
        return toxicaParaFelinos;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plantas plantas = (Plantas) o;
        return Objects.equals(nomePopular, plantas.nomePopular) && Objects.equals(nomeCientifico, plantas.nomeCientifico) && Objects.equals(descricao, plantas.descricao) && Objects.equals(toxicaParaCaninos, plantas.toxicaParaCaninos) && Objects.equals(toxicaParaFelinos, plantas.toxicaParaFelinos) && Objects.equals(imagemUrl, plantas.imagemUrl) && Objects.equals(id, plantas.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(nomePopular, nomeCientifico, descricao, toxicaParaCaninos, toxicaParaFelinos, imagemUrl, id);
    }
}

