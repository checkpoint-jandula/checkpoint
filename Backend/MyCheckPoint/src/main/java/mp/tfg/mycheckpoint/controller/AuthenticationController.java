package mp.tfg.mycheckpoint.controller;

import jakarta.validation.Valid;
import mp.tfg.mycheckpoint.dto.auth.JwtResponseDTO;
import mp.tfg.mycheckpoint.dto.auth.LoginRequestDTO;
import mp.tfg.mycheckpoint.security.UserDetailsImpl; // Para obtener el principal
import mp.tfg.mycheckpoint.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
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
}