package app.pet_pode_back.exception.handler;

import app.pet_pode_back.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import io.jsonwebtoken.*;

@ControllerAdvice
public class RestExceptionHandler {

    private ResponseEntity<ErroResponse> buildResponse(
            HttpStatus status,
            String mensagem,
            HttpServletRequest request) {

        ErroResponse error = new ErroResponse(
                status.value(),
                mensagem,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler({
            JwtException.class,
            MalformedJwtException.class,
            ExpiredJwtException.class,
            UnsupportedJwtException.class,
            SignatureException.class
    })
    public ResponseEntity<ErroResponse> trataJwtInvalido(
            RuntimeException ex,
            HttpServletRequest request) {

        return buildResponse(HttpStatus.BAD_REQUEST, "Token inválido.", request);
    }
    @ExceptionHandler(RegistroNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> trataRegistroNaoEncontrado(
            RegistroNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PetNotFoundException.class)
    public ResponseEntity<ErroResponse> trataPetNaoEncontrado(
            PetNotFoundException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ParametroInvalidoException.class)
    public ResponseEntity<ErroResponse> trataParametroInvalido(
            ParametroInvalidoException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(SemPermissaoException.class)
    public ResponseEntity<ErroResponse> trataSemPermissao(
            SemPermissaoException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> trataIntegridade(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String mensagem = "Operação inválida: já existe um registro com esses dados.";
        System.out.println("Erro: " + ex.getMostSpecificCause().getMessage());

        return buildResponse(HttpStatus.CONFLICT, mensagem, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> trataValidacoes(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        StringBuilder sb = new StringBuilder();
        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            sb.append(err.getField())
                    .append(": ")
                    .append(err.getDefaultMessage())
                    .append(". ");
        }

        return buildResponse(HttpStatus.BAD_REQUEST, sb.toString(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponse> trataResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return buildResponse(status, ex.getReason(), request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErroResponse> trataHeaderFaltando(
            MissingRequestHeaderException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "O header " + ex.getHeaderName() + " é obrigatório.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> trataErroGenerico(
            Exception ex,
            HttpServletRequest request) {

        System.out.println("Erro inesperado: " + ex.getMessage());
        ex.printStackTrace();

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro inesperado. Tente novamente.",
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> trataIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        // Pode ser 400 se considerar erro de parâmetro
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

}
