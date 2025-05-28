package mp.tfg.mycheckpoint.dto.usergame;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.comment.PublicGameCommentDTO;
import mp.tfg.mycheckpoint.dto.games.GameDto; // El DTO general de juego

import java.util.List;

@Schema(description = "DTO que contiene los detalles completos de un juego, incluyendo información general, " +
        "datos específicos del usuario (si está autenticado y el juego está en su biblioteca), " +
        "y comentarios públicos.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDetailDTO {

    @Schema(description = "Información general y detallada del juego (obtenida de IGDB y/o base de datos local).")
    @JsonProperty("game_info")
    private GameDto gameInfo; // Información general del juego (de IGDB/tu BD)

    @Schema(description = "Datos específicos del usuario para este juego (estado, puntuación, comentarios privados, etc.). " +
            "Este campo será nulo si el usuario no está autenticado o si el juego no está en su biblioteca.",
            nullable = true)
    @JsonProperty("user_game_data")
    @JsonInclude(JsonInclude.Include.NON_NULL) // Solo incluir si el usuario tiene este juego en su biblioteca
    private UserGameResponseDTO userGameData; // Información específica del usuario para este juego

    @Schema(description = "Lista de comentarios públicos realizados por otros usuarios sobre este juego. " +
            "Puede estar vacía si no hay comentarios o si el juego no existe en la base de datos local para asociar comentarios.",
            nullable = true)
    @JsonProperty("public_comments")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PublicGameCommentDTO> publicComments;

}
