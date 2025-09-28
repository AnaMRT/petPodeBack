package app.pet_pode_back.exception.handler;

public class ErroResponse {

    private Integer codigo;
    private String mensagem;

    public ErroResponse() {
    }

    public ErroResponse(Integer codigo, String mensagem) {
        this.codigo = codigo;
        this.mensagem = mensagem;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getMensagem() {
        return mensagem;
    }
}
