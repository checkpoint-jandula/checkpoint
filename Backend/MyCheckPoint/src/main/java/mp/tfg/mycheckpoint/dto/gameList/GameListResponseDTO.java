package mp.tfg.mycheckpoint.dto.gameList;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.usergame.UserGameResponseDTO; // Para los juegos dentro de la lista

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameListResponseDTO {

    @JsonProperty("public_id")
    private UUID publicId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("is_public")
    private boolean isPublic;

    @JsonProperty("owner_username") // Para mostrar quién es el dueño
    private String ownerUsername;

    @JsonProperty("games_in_list")
    private List<UserGameResponseDTO> gamesInList; // Lista de juegos (con sus datos de UserGame)

    @JsonProperty("game_count")
    private int gameCount;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime updatedAt;
}
