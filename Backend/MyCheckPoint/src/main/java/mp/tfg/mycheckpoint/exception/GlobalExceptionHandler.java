package mp.tfg.mycheckpoint.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Ya no son necesarios aquí si las excepciones usan @ResponseStatus
    // @ExceptionHandler(DuplicateEntryException.class)
    // public ResponseEntity<String> handleDuplicateEntryException(DuplicateEntryException ex) {
    //     return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    // }

    // @ExceptionHandler(ResourceNotFoundException.class)
    // public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
    //     return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    // }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        return new ResponseEntity<>(getErrorsMap(errors), HttpStatus.BAD_REQUEST); // 400
    }

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }

    // Handler genérico para otras excepciones no controladas explícitamente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        // Loggear la excepción aquí es una buena práctica
        // logger.error("Excepción no controlada: ", ex);
        return new ResponseEntity<>("Ocurrió un error interno en el servidor.", HttpStatus.INTERNAL_SERVER_ERROR); // 500
    }
}