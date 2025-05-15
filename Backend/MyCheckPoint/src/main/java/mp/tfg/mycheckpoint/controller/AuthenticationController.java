package mp.tfg.mycheckpoint.controller;

import jakarta.validation.Valid;
import mp.tfg.mycheckpoint.dto.auth.JwtResponseDTO;
import mp.tfg.mycheckpoint.dto.auth.LoginRequestDTO;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
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

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired // Si es un controlador nuevo o necesitas userService aquí
    public AuthenticationController(AuthenticationManager authenticationManager,
                                    JwtTokenProvider jwtTokenProvider,
                                    UserService userService /* Añadir */) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService; // Asignar
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
            String jwt = jwtTokenProvider.generateToken(authentication);

            return ResponseEntity.ok(new JwtResponseDTO(jwt));

        } catch (BadCredentialsException e) {
            logger.warn("Intento de login fallido por credenciales incorrectas para el identificador: {}", loginRequest.getIdentificador());
            // Es importante no dar demasiada información sobre por qué falló (usuario no existe vs contraseña incorrecta)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Credenciales inválidas.");
        } catch (DisabledException e) {
            logger.warn("Intento de login fallido porque la cuenta está deshabilitada (email no verificado) para el identificador: {}", loginRequest.getIdentificador());
            // Aquí puedes ser más específico porque el usuario ya pasó la validación de contraseña.
            // Opcionalmente, podrías iniciar el flujo de reenvío de email de verificación aquí si el usuario lo solicita.
            return ResponseEntity.status(HttpStatus.FORBIDDEN) // 403 Forbidden es apropiado aquí
                    .body("Error: Por favor, verifica tu dirección de correo electrónico para activar tu cuenta.");
        } catch (AuthenticationException e) {
            // Captura otras excepciones de autenticación
            logger.error("Error de autenticación inesperado para el identificador: {}", loginRequest.getIdentificador(), e);
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
}