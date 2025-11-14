package app.pet_pode_back.exception;

public class SemPermissaoException extends RuntimeException {
    public SemPermissaoException(String message) {
        super(message);
    }
}
