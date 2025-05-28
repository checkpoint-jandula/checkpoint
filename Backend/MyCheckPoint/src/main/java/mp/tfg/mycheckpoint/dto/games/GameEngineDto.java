package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO que representa un motor de videojuego (Game Engine).
 */
@Schema(description = "DTO para un motor de videojuego (Game Engine).") // Añadido Schema a nivel de clase
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameEngineDto {
    /**
     * ID único del motor de juego, generalmente proveniente de una fuente externa como IGDB.
     */
    @Schema(description = "ID del motor de juego desde la fuente externa (ej. IGDB).", example = "13") // Añadido Schema
    @JsonProperty("id")
    private Long igdbId;

    /**
     * Nombre del motor de juego.
     */
    @Schema(description = "Nombre del motor de juego.", example = "Unreal Engine") // Añadido Schema
    private String name;
}
