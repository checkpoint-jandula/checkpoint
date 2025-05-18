package mp.tfg.mycheckpoint.dto.gameList;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddGameToCustomListRequestDTO {

    // Se usará el internalId de la entidad UserGame,
    // ya que representa un juego específico en la biblioteca de un usuario.
    @NotNull(message = "UserGame ID cannot be null")
    @JsonProperty("user_game_id")
    private Long userGameId;
}
