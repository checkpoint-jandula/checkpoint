package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO que representa un modo de juego (ej. Un jugador, Multijugador).
 */
@Schema(description = "DTO para un modo de juego.") // Añadido Schema a nivel de clase
@Data
public class GameModeDto {
    /**
     * ID único del modo de juego, generalmente proveniente de IGDB.
     */
    @Schema(description = "ID del modo de juego desde IGDB.", example = "1") // Añadido Schema
    @JsonProperty("id")
    private Long igdbId;

    /**
     * Nombre del modo de juego.
     */
    @Schema(description = "Nombre del modo de juego.", example = "Single player") // Añadido Schema
    private String name;
}