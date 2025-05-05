package mp.tfg.mycheckpoint.dto.security;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtAuthenticationResponse {

    private String token;
    private String type = "Bearer"; // Standard practice to indicate token type

    // Opcionalmente, podrías incluir información básica del usuario autenticado
    // private Long userId;
    // private UUID publicId;
    // private String username;
    // private String email;

}