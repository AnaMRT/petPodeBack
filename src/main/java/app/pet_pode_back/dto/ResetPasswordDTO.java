package app.pet_pode_back.dto;
import jakarta.validation.constraints.NotBlank;

public class ResetPasswordDTO {

    @NotBlank(message = "Código não pode ser vazio")
    private String codigo;

    @NotBlank(message = "Nova senha não pode ser vazia")
    private String novaSenha;
    public String getCodigo() {
        return codigo;
    }
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    public String getNovaSenha() {
        return novaSenha;
    }
    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }

}
