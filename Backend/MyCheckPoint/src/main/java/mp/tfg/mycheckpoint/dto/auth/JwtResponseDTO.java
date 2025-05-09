package mp.tfg.mycheckpoint.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDTO {

    @JsonProperty("token_acceso")
    private String accessToken;

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