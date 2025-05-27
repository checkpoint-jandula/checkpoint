package mp.tfg.mycheckpoint.dto.gameList;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para añadir un juego existente de la biblioteca del usuario a una lista de juegos personalizada.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddGameToCustomListRequestDTO {

    // Se usará el internalId de la entidad UserGame,
    // ya que representa un juego específico en la biblioteca de un usuario.
    @Schema(description = "ID interno de la entrada 'UserGame' (juego en la biblioteca del usuario) que se desea añadir a la lista. Es obligatorio.",
            example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "UserGame ID cannot be null")
    @JsonProperty("user_game_id")
    private Long userGameId;
}
