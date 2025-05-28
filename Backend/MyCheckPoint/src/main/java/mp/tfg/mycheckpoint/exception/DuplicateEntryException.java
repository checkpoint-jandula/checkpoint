package mp.tfg.mycheckpoint.exception;

import org.springframework.http.HttpStatus;


public class DuplicateEntryException extends MyCheckPointException { // Hereda de la nueva base

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;

    public DuplicateEntryException(String message) {
        super(message, STATUS);
    }

    public DuplicateEntryException(String message, Throwable cause) {
        super(message, cause, STATUS);
    }
}