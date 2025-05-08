package mp.tfg.mycheckpoint.dto.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.GameTypeEnum;

// DTO simple para representar DLCs, Expansiones, Juegos Similares
@Data
@Builder // Builder puede ser útil aquí
@NoArgsConstructor
@AllArgsConstructor
public class RelatedGameDTO {
    @JsonProperty("id")
    private Long id; // ID interno del juego relacionado

    @JsonProperty("id_igdb")
    private Long idigdb; // ID de IGDB

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("cover_url")
    private String coverUrl;

    @JsonProperty("game_type")
    private GameTypeEnum gameType; // Para saber si es DLC, EXPANSION, etc.

    // Podrías añadir más campos si son necesarios, como total_rating
    @JsonProperty("total_rating")
    private Float totalRating;
}