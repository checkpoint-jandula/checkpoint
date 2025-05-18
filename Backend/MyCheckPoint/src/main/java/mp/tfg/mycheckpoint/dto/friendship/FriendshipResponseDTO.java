package mp.tfg.mycheckpoint.dto.friendship;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.FriendshipStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipResponseDTO {

    @JsonProperty("friendship_id") // El ID interno de la amistad/solicitud
    private Long friendshipId;

    // Información del "otro" usuario en la relación
    @JsonProperty("user_public_id")
    private UUID userPublicId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("profile_picture_url")
    private String profilePictureUrl;

    @JsonProperty("status")
    private FriendshipStatus status;

    // Indica si el usuario actual fue quien envió la solicitud originalmente
    // Útil para el frontend para mostrar "Solicitud enviada" vs "Solicitud recibida"
    @JsonProperty("is_initiated_by_current_user")
    private Boolean isInitiatedByCurrentUser;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime createdAt; // Fecha de creación de la solicitud/amistad

    @JsonProperty("updated_at") // Fecha de aceptación/última actualización
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime updatedAt;
}