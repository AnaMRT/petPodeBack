package app.pet_pode_back.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "pet")
public class Pet {

    @Id
    @GeneratedValue
    @Column
    private UUID id;

    @Column
    private String nome;

    @Column
    private String especie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonBackReference
    private Usuario usuario;

    public Pet(UUID id, String nome, String especie, Usuario usuario) {
        this.id = id;
        this.nome = nome;
        this.especie = especie;
        this.usuario = usuario;
    }

    public Pet() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Pet pet = (Pet) o;
        return Objects.equals(id, pet.id) && Objects.equals(nome, pet.nome) && Objects.equals(especie, pet.especie) && Objects.equals(usuario, pet.usuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, especie, usuario);
    }
}