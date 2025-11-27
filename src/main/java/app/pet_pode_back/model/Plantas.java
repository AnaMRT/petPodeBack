
package app.pet_pode_back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "plantas")
public class Plantas {

    @Id
    @GeneratedValue
    @Column
    private UUID id;
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

    public Plantas() {
    }

    public Plantas(UUID id, String nomePopular, String nomeCientifico, String descricao, Boolean toxicaParaCaninos, Boolean toxicaParaFelinos, String imagemUrl) {
        this.id = id;
        this.nomePopular = nomePopular;
        this.nomeCientifico = nomeCientifico;
        this.descricao = descricao;
        this.toxicaParaCaninos = toxicaParaCaninos;
        this.toxicaParaFelinos = toxicaParaFelinos;
        this.imagemUrl = imagemUrl;
    }

    public void setNomePopular(String nomePopular) {
        this.nomePopular = nomePopular;
    }

    public void setNomeCientifico(String nomeCientifico) {
        this.nomeCientifico = nomeCientifico;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setToxicaParaCaninos(Boolean toxicaParaCaninos) {
        this.toxicaParaCaninos = toxicaParaCaninos;
    }

    public void setToxicaParaFelinos(Boolean toxicaParaFelinos) {
        this.toxicaParaFelinos = toxicaParaFelinos;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public void setId(UUID id) {
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
        return Objects.equals(id, plantas.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

