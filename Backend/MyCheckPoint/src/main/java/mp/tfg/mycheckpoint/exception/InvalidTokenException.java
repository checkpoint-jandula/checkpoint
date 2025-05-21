package mp.tfg.mycheckpoint.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends MyCheckPointException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST; // O NOT_FOUND si el token no se encuentra

    public InvalidTokenException(String message) {
        super(message, STATUS);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause, STATUS);
    }
}
