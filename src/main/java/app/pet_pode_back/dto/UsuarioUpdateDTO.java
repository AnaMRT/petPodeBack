package app.pet_pode_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UsuarioUpdateDTO {

    @NotBlank(message = "Nome nao pode ser nulo")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres.")
    private String nome;
    @NotBlank(message = "Email nao pode ser nulo")
    @Size(min = 5, max = 25, message = "O email deve ter entre 5 e 25 caracteres.")
    @Email
    private String email;
    private String senha;
    private String confirmarSenha;
    private String senhaAtual;

    public UsuarioUpdateDTO() {
    }
    public UsuarioUpdateDTO(String nome, String email, String senha, String confirmarSenha, String senhaAtual) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.confirmarSenha = confirmarSenha;
        this.senhaAtual = senhaAtual;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
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
    public String getConfirmarSenha() {
        return confirmarSenha;
    }
    public void setConfirmarSenha(String confirmarSenha) {
        this.confirmarSenha = confirmarSenha;
    }
    public String getSenhaAtual() {
        return senhaAtual;
    }
    public void setSenhaAtual(String senhaAtual) {
        this.senhaAtual = senhaAtual;
    }
}
