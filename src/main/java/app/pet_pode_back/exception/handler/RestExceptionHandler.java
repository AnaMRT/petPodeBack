package app.pet_pode_back.exception.handler;
import app.pet_pode_back.exception.ParametroInvalidoException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErroResponse> trataRegistroNaoEcontradoException(RegistroNaoEncontradoException ex) {
        ErroResponse errorResponse = new ErroResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<ErroResponse>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErroResponse> trataParametroInvalidoException(ParametroInvalidoException ex) {
        ErroResponse errorResponse = new ErroResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<ErroResponse>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErroResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder(50);
        ex.getBindingResult().getAllErrors().forEach( error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            sb.append(fieldName);
            sb.append(" - ");
            sb.append(errorMessage);
            sb.append("; ");
        });
        ErroResponse errorResponse = new ErroResponse(HttpStatus.BAD_REQUEST.value(), sb.toString());
        return new ResponseEntity<ErroResponse>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}