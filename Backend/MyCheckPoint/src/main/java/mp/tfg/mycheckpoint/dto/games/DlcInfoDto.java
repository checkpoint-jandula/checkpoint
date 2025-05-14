package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.GameType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DlcInfoDto {
    @JsonProperty("id")
    private Long igdbId;

    private CoverDto cover; // Reutilizamos CoverDto

    private String name;

    @JsonProperty("total_rating")
    private Double totalRating;

    @JsonProperty("game_type") // Debería ser 1 (DLC) o 2 (EXPANSION)
    private GameType gameType;

    @JsonProperty("slug") // Campo añadido
    private String slug;
}
