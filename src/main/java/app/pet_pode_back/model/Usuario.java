package app.pet_pode_back.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue
    @Column
    private UUID id;


    @NotBlank(message = "Nome nao pode ser nulo")
    @Column
    private String nome;

    @NotBlank(message = "Email nao pode ser nulo")
    @Column(unique = true, nullable = false)
    @Email
    private String email;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Pet> pets;

  // @Size(min = 5, max = 10, message = "senha não pode ter mais que 10 caracteres e menos de 5")
    @NotBlank(message = "Senha nao pode ser nula")
    @Column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    private String resetToken;

    public Usuario() {
    }

    public Usuario(UUID id, String nome, String email, List<Pet> pets, String senha, String resetToken) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.pets = pets;
        this.senha = senha;
        this.resetToken = resetToken;
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

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) && Objects.equals(nome, usuario.nome) && Objects.equals(email, usuario.email) && Objects.equals(pets, usuario.pets) && Objects.equals(senha, usuario.senha) && Objects.equals(resetToken, usuario.resetToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, email, pets, senha, resetToken);
    }
}
