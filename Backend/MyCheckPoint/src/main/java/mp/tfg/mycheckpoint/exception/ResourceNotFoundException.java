package mp.tfg.mycheckpoint.exception;

import org.springframework.http.HttpStatus;


public class ResourceNotFoundException extends MyCheckPointException { // Hereda de la nueva base

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public ResourceNotFoundException(String message) {
        super(message, STATUS);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, STATUS);
    }
}