package mp.tfg.mycheckpoint.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

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
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
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
    public ResponseEntity<String> confirmUserAccount(@RequestParam("token") String token) {
        try {
            String resultMessage = userService.confirmEmailVerification(token);
            // Aquí, sin frontend, simplemente devolvemos el mensaje.
            // Un frontend redirigiría a una página de login o de éxito.
            return ResponseEntity.ok(resultMessage);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) { // Podrías usar excepciones más específicas
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
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