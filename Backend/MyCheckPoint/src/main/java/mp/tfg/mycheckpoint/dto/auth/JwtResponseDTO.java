package mp.tfg.mycheckpoint.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para la respuesta de inicio de sesión exitoso. Contiene el token de acceso JWT.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDTO {

    @Schema(description = "Token de acceso JWT generado para el usuario autenticado.",
            example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c3VhcmlvQGV4YW1wbGUuY29tIiwiaWF0IjoxNj...")
    @JsonProperty("token_acceso")
    private String accessToken;

    @Schema(description = "Tipo de token, generalmente 'Bearer'.", example = "Bearer")
    @JsonProperty("tipo_token")
    private String tokenType = "Bearer"; // Estándar para tokens Bearer

    // Opcional: podrías incluir aquí información del usuario si es útil
    // como el public_id o el nombre_usuario para el frontend.
    // @JsonProperty("public_id")
    // private UUID publicId;
    //
    // @JsonProperty("nombre_usuario")
    // private String nombreUsuario;

    public JwtResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }
}