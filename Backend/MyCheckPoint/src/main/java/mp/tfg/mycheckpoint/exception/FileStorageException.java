package mp.tfg.mycheckpoint.exception;

import org.springframework.http.HttpStatus; // Necesario para HttpStatus

// Ahora extiende MyCheckPointException
public class FileStorageException extends MyCheckPointException {

    // Constructor que define un HttpStatus por defecto (ej. BAD_REQUEST)
    public FileStorageException(String message) {
        super(message, HttpStatus.BAD_REQUEST); // O el HttpStatus más común para este tipo de error
    }

    // Constructor que permite especificar el HttpStatus
    public FileStorageException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

    // Constructor que define un HttpStatus por defecto y acepta una causa
    public FileStorageException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST); // O el HttpStatus más común
    }

    // Constructor que permite especificar el HttpStatus y acepta una causa
    public FileStorageException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause, httpStatus);
    }
}