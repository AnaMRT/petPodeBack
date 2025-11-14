package app.pet_pode_back.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue
    @Column
    private UUID id;
    @NotBlank(message = "Nome nao pode ser nulo")
    //@Size(max = 100, message = "O nome deve ter ate 100 caracteres")
    @Column
    private String nome;
    @NotBlank(message = "Email nao pode ser nulo")
   // @Size(min = 6, max = 30, message = "O e-mail deve ter entre 6 e 30 caracteres")
    @Column(unique = true, nullable = false)
    @Email
    private String email;
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Pet> pets;
    @NotBlank(message = "Senha nao pode ser nula")
   // @Size(min = 6, max = 10, message = "A senha deve ter entre 6 e 10 caracteres")
    @Column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;
    private String resetToken;
    @Column
    private String imagemUrl;
    @Column(name = "imagem_public_id")
    private String imagemPublicId;
    @ManyToMany
    @JoinTable(
            name = "usuario_favoritos",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "planta_id")
    )
    private Set<Plantas> favoritos = new HashSet<>();

    public Set<Plantas> getFavoritos() {
        return favoritos;
    }
    public void addFavorito(Plantas planta) {
        this.favoritos.add(planta);
    }
    public void removeFavorito(Plantas planta) {
        this.favoritos.remove(planta);
    }
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
        this.email = email == null ? null : email.toLowerCase();
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
    public String getImagemUrl() {
        return imagemUrl;
    }
    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }
    public String getImagemPublicId() {
        return imagemPublicId;
    }
    public void setImagemPublicId(String imagemPublicId) {
        this.imagemPublicId = imagemPublicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) && Objects.equals(nome, usuario.nome) && Objects.equals(email, usuario.email) && Objects.equals(pets, usuario.pets) && Objects.equals(senha, usuario.senha) && Objects.equals(resetToken, usuario.resetToken) && Objects.equals(imagemUrl, usuario.imagemUrl) && Objects.equals(imagemPublicId, usuario.imagemPublicId) && Objects.equals(favoritos, usuario.favoritos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, email, pets, senha, resetToken, imagemUrl, imagemPublicId, favoritos);
    }
}
