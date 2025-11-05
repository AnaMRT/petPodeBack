package app.pet_pode_back.exception.handler;

import app.pet_pode_back.exception.ParametroInvalidoException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class RestExceptionHandler {

    // Trata exceções de registro não encontrado
    @ExceptionHandler
    public ResponseEntity<ErroResponse> trataRegistroNaoEncontradoException(RegistroNaoEncontradoException ex) {
        ErroResponse errorResponse = new ErroResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Trata parâmetros inválidos
    @ExceptionHandler
    public ResponseEntity<ErroResponse> trataParametroInvalidoException(ParametroInvalidoException ex) {
        ErroResponse errorResponse = new ErroResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Trata conflitos de dados (ex: usuário já existe)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String mensagem = "Este usuário já existe.";
        ErroResponse errorResponse = new ErroResponse(HttpStatus.CONFLICT.value(), mensagem);
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // Trata erros de validação (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            sb.append(error.getField())
                    .append(" - ")
                    .append(error.getDefaultMessage())
                    .append("; ");
        }
        ErroResponse errorResponse = new ErroResponse(HttpStatus.BAD_REQUEST.value(), sb.toString());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Trata ResponseStatusException do service
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponse> handleResponseStatusException(ResponseStatusException ex) {
        ErroResponse errorResponse = new ErroResponse(ex.getStatusCode().value(), ex.getReason());
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    // Trata qualquer outra exceção inesperada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGenericException(Exception ex) {
        ex.printStackTrace(); // opcional, para log no console
        ErroResponse errorResponse = new ErroResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
