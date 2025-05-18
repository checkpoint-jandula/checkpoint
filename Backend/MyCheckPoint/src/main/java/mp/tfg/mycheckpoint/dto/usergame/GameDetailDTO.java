package mp.tfg.mycheckpoint.dto.usergame;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.comment.PublicGameCommentDTO;
import mp.tfg.mycheckpoint.dto.games.GameDto; // El DTO general de juego

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDetailDTO {

    @JsonProperty("game_info")
    private GameDto gameInfo; // Información general del juego (de IGDB/tu BD)

    @JsonProperty("user_game_data")
    @JsonInclude(JsonInclude.Include.NON_NULL) // Solo incluir si el usuario tiene este juego en su biblioteca
    private UserGameResponseDTO userGameData; // Información específica del usuario para este juego

    @JsonProperty("public_comments")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PublicGameCommentDTO> publicComments;

}
