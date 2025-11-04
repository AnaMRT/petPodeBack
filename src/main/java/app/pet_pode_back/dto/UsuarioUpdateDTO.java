package app.pet_pode_back.dto;

public class UsuarioUpdateDTO {

    private String nome;
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

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getConfirmarSenha() { return confirmarSenha; }
    public void setConfirmarSenha(String confirmarSenha) { this.confirmarSenha = confirmarSenha; }

    public String getSenhaAtual() { return senhaAtual; }
    public void setSenhaAtual(String senhaAtual) { this.senhaAtual = senhaAtual; }
}
