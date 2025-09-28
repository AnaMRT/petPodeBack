package app.pet_pode_back.dto;

public class ResetPasswordDTO {
    private String token;
    private String novaSenha;

    // Getters e Setters
    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public String getNovaSenha() { return novaSenha; }

    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
}
