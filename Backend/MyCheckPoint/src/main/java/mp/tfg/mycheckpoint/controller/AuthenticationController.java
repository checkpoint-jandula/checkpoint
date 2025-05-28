package mp.tfg.mycheckpoint.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mp.tfg.mycheckpoint.dto.auth.JwtResponseDTO;
import mp.tfg.mycheckpoint.dto.auth.LoginRequestDTO;
import mp.tfg.mycheckpoint.dto.user.ForgotPasswordDTO;
import mp.tfg.mycheckpoint.dto.user.ResetPasswordDTO;
import mp.tfg.mycheckpoint.entity.User;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.repository.UserRepository;
import mp.tfg.mycheckpoint.security.UserDetailsImpl; // Para obtener el principal
import mp.tfg.mycheckpoint.security.jwt.JwtTokenProvider;
import mp.tfg.mycheckpoint.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;

@Tag(name = "Autenticación Controller", description = "API para la autenticación de usuarios y gestión de tokens")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired // Si es un controlador nuevo o necesitas userService aquí
    public AuthenticationController(AuthenticationManager authenticationManager,
                                    JwtTokenProvider jwtTokenProvider,
                                    UserService userService /* Añadir */,
                                    UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService; // Asignar
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario y obtener token JWT",
            description = "Permite a un usuario iniciar sesión proporcionando su identificador (email o nombre de usuario) y contraseña. " +
                    "Si las credenciales son válidas y la cuenta está activa, se devuelve un token JWT. " +
                    "Si el usuario tenía una eliminación de cuenta programada y la fecha aún no ha pasado, esta se cancela.",
            operationId = "authenticateUser",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody( // <- Aquí importa esta
                    description = "Credenciales del usuario para iniciar sesión.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequestDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa. Devuelve el token JWT.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = JwtResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos. El identificador o la contraseña no cumplen los requisitos de formato o están vacíos.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "No autorizado. Credenciales incorrectas o fallo general de autenticación.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. La cuenta está deshabilitada (ej. email no verificado) o la cuenta ha sido eliminada porque su fecha de eliminación programada ya pasó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))), // Usamos ErrorResponse ya que el cuerpo es {"error": "mensaje"}
            @ApiResponse(responseCode = "404", description = "No encontrado. El usuario autenticado no se pudo encontrar en la base de datos (error interno anómalo durante la cancelación de eliminación).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<?> authenticateUser(@Valid @org.springframework.web.bind.annotation.RequestBody LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getIdentificador(),
                            loginRequest.getContraseña()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // --- INICIO: Lógica para cancelar eliminación programada ---
            User userEntity = userRepository.findByEmail(userDetails.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado en la base de datos: " + userDetails.getEmail()));

            if (userEntity.getFechaEliminacion() != null) {
                if (userEntity.getFechaEliminacion().isAfter(OffsetDateTime.now())) {
                    logger.info("Usuario {} ha iniciado sesión. Cancelando la eliminación programada para {}.", userEntity.getEmail(), userEntity.getFechaEliminacion());
                    userEntity.setFechaEliminacion(null);
                    userRepository.save(userEntity);
                    logger.info("Eliminación programada cancelada para el usuario {}.", userEntity.getEmail());
                } else {
                    logger.warn("Usuario {} inició sesión, pero su fecha de eliminación programada ({}) ya pasó. La cuenta debería haber sido eliminada por la tarea programada.", userEntity.getEmail(), userEntity.getFechaEliminacion());
                    SecurityContextHolder.clearContext(); // Invalidar sesión
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Error: Esta cuenta ha sido eliminada.");
                }
            }
            // --- FIN: Lógica para cancelar eliminación programada ---

            String jwt = jwtTokenProvider.generateToken(authentication);
            return ResponseEntity.ok(new JwtResponseDTO(jwt));

        } catch (BadCredentialsException e) {
            logger.warn("Intento de login fallido por credenciales incorrectas para el identificador: {}", loginRequest.getIdentificador());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Credenciales inválidas.");
        } catch (DisabledException e) {
            logger.warn("Intento de login fallido porque la cuenta está deshabilitada (UserDetails.isEnabled() es false) para el identificador: {}", loginRequest.getIdentificador());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error: La cuenta está deshabilitada. Por favor, verifica tu correo electrónico o contacta con el soporte.");
        } catch (AuthenticationException e) {
            logger.error("Error de autenticación inesperado para el identificador: {}: {}", loginRequest.getIdentificador(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Fallo en la autenticación.");
        }
    }

    @GetMapping("/confirm-account")
    @Operation(summary = "Confirmar la dirección de correo electrónico de un usuario",
            description = "Valida un token de verificación enviado al correo electrónico del usuario tras el registro. " +
                    "Si el token es válido y no ha expirado, la cuenta del usuario se marca como verificada. " +
                    "Este endpoint es público y se accede a través del enlace en el correo de verificación.",
            operationId = "confirmUserAccount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Correo electrónico verificado exitosamente.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, // El cuerpo es un String simple
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "¡Tu correo electrónico ha sido verificado exitosamente! Ahora puedes iniciar sesión.")
                    )),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta. El token es inválido (ej. ya fue usado, ha expirado, o el correo ya estaba verificado).",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, // El cuerpo es un String simple
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Este enlace de verificación ya ha sido utilizado.")
                    )),
            @ApiResponse(responseCode = "404", description = "No encontrado. El token de verificación proporcionado no existe o es inválido.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, // El cuerpo es un String simple
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Token de verificación inválido o no encontrado.")
                    )),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    // Aunque el método no lo captura explícitamente, GlobalExceptionHandler sí lo haría.
                    // Aquí, el cuerpo del error de GlobalExceptionHandler sería JSON.
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<Void> confirmUserAccount(
            @Parameter(name = "token",
                    description = "El token de verificación único enviado al correo electrónico del usuario.",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    schema = @Schema(type = "string"))
            @RequestParam("token") String token) {

        // Define la URL base de tu frontend.
        // Para desarrollo, es http://localhost:5173 (el puerto de Vite)
        // Para producción, esta debería ser la URL pública de tu frontend.
        // Podrías hacerla configurable a través de application.properties.
        String frontendLoginUrl = "http://localhost:5173/login";

        try {
            // Asumo que userService.confirmEmailVerification(token)
            // lanza una excepción si el token no es válido o ya fue usado,
            // y no devuelve nada o devuelve algo que no necesitamos para la redirección.
            userService.confirmEmailVerification(token);

            // Si la verificación es exitosa, redirige al login del frontend con un parámetro de éxito
            URI successUri = URI.create(frontendLoginUrl + "?verification_success=true");
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(successUri);
            return new ResponseEntity<>(headers, HttpStatus.FOUND); // Código 302 Found para redirección

        } catch (ResourceNotFoundException e) { // Si el token no se encuentra
            // Redirige al login del frontend con un parámetro de error específico
            URI errorUri = URI.create(frontendLoginUrl + "?verification_error=token_not_found");
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(errorUri);
            return new ResponseEntity<>(headers, HttpStatus.FOUND);

        } catch (IllegalStateException e) { // Si el token es inválido (ej. expirado, ya usado)
            // Redirige al login del frontend con otro parámetro de error
            URI errorUri = URI.create(frontendLoginUrl + "?verification_error=invalid_token");
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(errorUri);
            return new ResponseEntity<>(headers, HttpStatus.FOUND);

        } catch (Exception e) { // Un catch-all para otros posibles errores durante la verificación
            // Loggear el error en el servidor es importante aquí
            // logger.error("Error inesperado durante la confirmación del email: ", e);
            URI errorUri = URI.create(frontendLoginUrl + "?verification_error=server_error");
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(errorUri);
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar restablecimiento de contraseña",
            description = "Inicia el proceso para restablecer la contraseña de un usuario. El usuario proporciona su dirección de correo electrónico. " +
                    "Si el correo está registrado, se enviará un email con un token e instrucciones para restablecer la contraseña. " +
                    "Para no revelar si un email existe en el sistema, este endpoint siempre devuelve una respuesta genérica de éxito, " +
                    "independientemente de si el email fue encontrado o no. Este endpoint es público.",
            operationId = "forgotPassword",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "DTO que contiene el correo electrónico del usuario que ha olvidado su contraseña.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ForgotPasswordDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitud procesada. Se enviará un correo si el email está registrado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            // Esquema para: {"message": "string"}
                            schema = @Schema(type = "object"), // O implementation = SuccessMessageDTO.class si lo tienes
                            examples = @ExampleObject(
                                    name = "ForgotPasswordSuccess",
                                    value = "{\"message\": \"Si tu dirección de correo electrónico está registrada, recibirás un enlace para restablecer tu contraseña.\"}"
                            )
                    )),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos. El formato del email proporcionado no es válido o el campo está vacío.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse")))
            // No se documentan explícitamente 404 o 500 aquí porque el método siempre devuelve 200 OK con un mensaje genérico
            // para el caso de éxito y para las excepciones capturadas internamente.
            // El único error que el cliente vería con otro código sería un 400 por validación del DTO.
    })
    public ResponseEntity<Object> forgotPassword(@Valid @org.springframework.web.bind.annotation.RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        try {
            userService.processForgotPassword(forgotPasswordDTO);
            // Siempre devolver una respuesta genérica para no revelar si un email existe en el sistema
            return ResponseEntity.ok().body(java.util.Collections.singletonMap("message",
                    "Si tu dirección de correo electrónico está registrada, recibirás un enlace para restablecer tu contraseña."));
        } catch (Exception e) {
            // Loguear el error pero devolver respuesta genérica
            logger.error("Error inesperado durante el proceso de forgot-password para email {}: {}", forgotPasswordDTO.getEmail(), e.getMessage(), e);
            return ResponseEntity.ok().body(java.util.Collections.singletonMap("message",
                    "Si tu dirección de correo electrónico está registrada, recibirás un enlace para restablecer tu contraseña."));
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer la contraseña del usuario utilizando un token",
            description = "Permite a un usuario establecer una nueva contraseña utilizando el token de restablecimiento que recibió por correo electrónico. " +
                    "El token debe ser válido y no haber expirado. Este endpoint es público.",
            operationId = "resetPassword",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "DTO que contiene el token de restablecimiento y la nueva contraseña deseada.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ResetPasswordDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            // Esquema para: {"message": "string"}
                            schema = @Schema(type = "object"), // O implementation = SuccessMessageDTO.class si lo tienes
                            examples = @ExampleObject(
                                    name = "PasswordResetSuccess",
                                    value = "{\"message\": \"Tu contraseña ha sido restablecida exitosamente. Ahora puedes iniciar sesión con tu nueva contraseña.\"}"
                            )
                    )),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta. Los datos proporcionados en `ResetPasswordDTO` no son válidos (ej. token vacío, contraseña nueva no cumple requisitos), el token ya fue usado, ha expirado, o la nueva contraseña es la misma que la actual.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationPasswordErrorResponse")
                    )
            ),
    @ApiResponse(responseCode = "404", description = "No encontrado. El token de restablecimiento proporcionado no existe o es inválido.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
})
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        try {
            String resultMessage = userService.processResetPassword(resetPasswordDTO);
            return ResponseEntity.ok().body(java.util.Collections.singletonMap("message", resultMessage));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Collections.singletonMap("error", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado durante el proceso de reset-password para token {}: {}", resetPasswordDTO.getToken(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Collections.singletonMap("error", "Ocurrió un error inesperado al restablecer la contraseña."));
        }
    }

}