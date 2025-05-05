package mp.tfg.mycheckpoint.dto.security;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtAuthenticationRequest {

    // Puede ser nombre de usuario o email, dependiendo de cómo implementes el login
    @NotBlank(message = "El nombre de usuario o email no puede estar vacío")
    private String usernameOrEmail;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;

}