package mp.tfg.mycheckpoint.controller;

import jakarta.validation.Valid;
import mp.tfg.mycheckpoint.dto.auth.JwtResponseDTO;
import mp.tfg.mycheckpoint.dto.auth.LoginRequestDTO;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.security.UserDetailsImpl; // Para obtener el principal
import mp.tfg.mycheckpoint.security.jwt.JwtTokenProvider;
import mp.tfg.mycheckpoint.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

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

        // Spring Security usa UserDetailsService para cargar el usuario por loginRequest.getIdentificador()
        // y PasswordEncoder para comparar loginRequest.getContraseña() con la hasheada.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getIdentificador(), // Esto se pasará como 'username' a UserDetailsService
                        loginRequest.getContraseña()
                )
        );

        // Si la autenticación es exitosa, establece la autenticación en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Genera el token JWT
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Obtén UserDetails del objeto Authentication para pasar info al DTO de respuesta si es necesario
        // UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponseDTO(jwt));
        // Si quisieras añadir más info al JwtResponseDTO:
        // return ResponseEntity.ok(new JwtResponseDTO(jwt, userDetails.getPublicId(), userDetails.getUsername()));
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