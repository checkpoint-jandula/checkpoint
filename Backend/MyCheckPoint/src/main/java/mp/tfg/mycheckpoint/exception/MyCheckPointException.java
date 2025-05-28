package mp.tfg.mycheckpoint.exception;

import org.springframework.http.HttpStatus;

public abstract class MyCheckPointException extends RuntimeException {

    private final HttpStatus httpStatus;

    public MyCheckPointException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public MyCheckPointException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
