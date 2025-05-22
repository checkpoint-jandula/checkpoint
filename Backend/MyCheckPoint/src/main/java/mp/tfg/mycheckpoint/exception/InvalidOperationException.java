package mp.tfg.mycheckpoint.exception;

import org.springframework.http.HttpStatus;

// Asumo que ya tienes una clase base MyCheckPointException como se discutió anteriormente.
// Si no, puedes hacer que esta extienda RuntimeException directamente y anotar con @ResponseStatus.
public class InvalidOperationException extends MyCheckPointException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public InvalidOperationException(String message) {
        super(message, STATUS);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause, STATUS);
    }
}