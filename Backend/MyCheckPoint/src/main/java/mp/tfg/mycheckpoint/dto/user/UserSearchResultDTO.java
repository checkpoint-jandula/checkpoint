package mp.tfg.mycheckpoint.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResultDTO {

    @JsonProperty("public_id")
    private UUID publicId;

    @JsonProperty("nombre_usuario")
    private String nombreUsuario;

    @JsonProperty("foto_perfil")
    private String fotoPerfil;

    // Podrías añadir un campo para indicar el estado de amistad con el usuario actual (ej. "FRIENDS", "PENDING_SENT", "PENDING_RECEIVED", "NOT_FRIENDS")
    // @JsonProperty("friendship_status_with_current_user")
    // private String friendshipStatusWithCurrentUser; // Esto requeriría más lógica al construir el DTO
}