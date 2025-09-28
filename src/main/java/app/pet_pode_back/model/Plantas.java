
package app.pet_pode_back.model;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "plantas")
public class Plantas {

    @Column
    private String nomePopular;
    @Column
    private String nomeCientifico;
    @Column(length = 1000)
    private String descricao;

    @Column
    private Boolean toxicaParaCaninos;

    @Column
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
}

