package mp.tfg.mycheckpoint.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Schema(description = "DTO que representa un resultado de búsqueda de usuario, mostrando información pública básica.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResultDTO {

    @Schema(description = "ID público del usuario encontrado.", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @JsonProperty("public_id")
    private UUID publicId;

    @Schema(description = "Nombre de usuario del usuario encontrado.", example = "juanPerez")
    @JsonProperty("nombre_usuario")
    private String nombreUsuario;

    @Schema(description = "URL de la foto de perfil del usuario encontrado. Puede ser nulo.", example = "/profile-pictures/a1b2c3d4-e5f6-7890-1234-567890abcdef.png", nullable = true)
    @JsonProperty("foto_perfil")
    private String fotoPerfil;

    // Podrías añadir un campo para indicar el estado de amistad con el usuario actual (ej. "FRIENDS", "PENDING_SENT", "PENDING_RECEIVED", "NOT_FRIENDS")
    // @JsonProperty("friendship_status_with_current_user")
    // private String friendshipStatusWithCurrentUser; // Esto requeriría más lógica al construir el DTO
}