package app.pet_pode_back.exception.handler;

import java.time.LocalDateTime;
import java.util.List;

public class ErroResponse {

    private int codigo;
    private String mensagem;
    private String path;
    private LocalDateTime timestamp;
    private List<String> erros;

    public ErroResponse(int codigo, String mensagem, String path) {
        this.codigo = codigo;
        this.mensagem = mensagem;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public ErroResponse(int codigo, String mensagem, String path, List<String> erros) {
        this(codigo, mensagem, path);
        this.erros = erros;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getPath() {
        return path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public List<String> getErros() {
        return erros;
    }
}
