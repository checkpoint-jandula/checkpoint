package mp.tfg.mycheckpoint.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handler para todas las excepciones que extiendan de MyCheckPointException
    @ExceptionHandler(MyCheckPointException.class)
    public ResponseEntity<Map<String, String>> handleMyCheckPointException(MyCheckPointException ex) {
        logger.warn("MyCheckPointException capturada: {} - HTTP Status: {} - Mensaje: {}",
                ex.getClass().getSimpleName(), ex.getHttpStatus(), ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        // También podrías añadir más detalles si los tienes en tus excepciones personalizadas
        // errorResponse.put("errorCode", ex.getErrorCode()); // Si tuvieras un código de error
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    // Mantener el handler para errores de validación de DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        logger.warn("Error de validación: {}", errors);
        return new ResponseEntity<>(getErrorsMap(errors), HttpStatus.BAD_REQUEST);
    }

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }

    // Handler para BadCredentialsException (si no la envuelves en una personalizada)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        logger.warn("Error de credenciales: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Credenciales inválidas."); // Mensaje genérico para el usuario
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Handler para AccessDeniedException de Spring Security (cuando un usuario autenticado no tiene permisos)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Acceso denegado: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "No tienes permiso para realizar esta acción.");
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }


    // El manejador para MaxUploadSizeExceededException (límite global de Spring) sigue siendo útil
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        logger.warn("Intento de subir un archivo que excede el límite máximo global: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "El archivo es demasiado grande. El tamaño máximo de subida está configurado en el servidor.");
        // Para ser más precisos, podrías parsear el tamaño máximo de la configuración general de Spring si es necesario.
        // O usar un mensaje más genérico como "El archivo excede el tamaño máximo permitido."
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // Handler genérico para otras excepciones no controladas explícitamente
    // Este debería ser el último.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        logger.error("Excepción no controlada: ", ex); // Loguear el stack trace completo
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Ocurrió un error interno inesperado en el servidor.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}