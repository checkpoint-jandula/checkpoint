package mp.tfg.mycheckpoint.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedOperationException extends MyCheckPointException { // Hereda de la nueva base

    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN; // O HttpStatus.UNAUTHORIZED según el caso de uso exacto

    public UnauthorizedOperationException(String message) {
        super(message, STATUS);
    }

    public UnauthorizedOperationException(String message, Throwable cause) {
        super(message, cause, STATUS);
    }
}
