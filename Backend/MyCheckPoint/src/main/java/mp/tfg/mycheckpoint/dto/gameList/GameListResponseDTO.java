package mp.tfg.mycheckpoint.dto.gameList;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.usergame.UserGameResponseDTO; // Para los juegos dentro de la lista

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "DTO que representa una lista de juegos personalizada, incluyendo sus detalles y los juegos que contiene.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameListResponseDTO {

    @Schema(description = "ID público único de la lista de juegos.", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479", format = "uuid", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("public_id")
    private UUID publicId;

    @Schema(description = "Nombre de la lista de juegos.", example = "Mis Juegos Favoritos de RPG")
    @JsonProperty("name")
    private String name;

    @Schema(description = "Descripción de la lista de juegos.", example = "Una colección de los RPGs que más he disfrutado.", nullable = true)
    @JsonProperty("description")
    private String description;

    @Schema(description = "Indica si la lista de juegos es pública (true) o privada (false).", example = "false")
    @JsonProperty("is_public")
    private boolean isPublic;

    @Schema(description = "Nombre de usuario del propietario de la lista.", example = "usuarioEjemplo", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("owner_username") // Para mostrar quién es el dueño
    private String ownerUsername;

    @Schema(description = "Lista de juegos (entradas de la biblioteca del usuario) incluidos en esta lista. Puede estar vacía.", nullable = true)
    @JsonProperty("games_in_list")
    private List<UserGameResponseDTO> gamesInList; // Lista de juegos (con sus datos de UserGame)

    @Schema(description = "Número total de juegos en la lista.", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("game_count")
    private int gameCount;

    @Schema(description = "Fecha y hora de creación de la lista (formato ISO 8601).", example = "2024-05-01T10:00:00.000Z", format = "date-time", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime createdAt;

    @Schema(description = "Fecha y hora de la última actualización de la lista (formato ISO 8601).", example = "2024-05-15T14:30:00.000Z", format = "date-time", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime updatedAt;
}
