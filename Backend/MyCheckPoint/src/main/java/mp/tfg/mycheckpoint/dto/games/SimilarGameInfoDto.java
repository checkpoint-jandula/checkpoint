package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarGameInfoDto {
    @JsonProperty("id")
    private Long igdbId;

    private CoverDto cover;

    private String name;
    private String slug;
    private String summary;

    @JsonProperty("total_rating")
    private Double totalRating;

    // No incluimos game_type aquí, ya que asumiremos que es GAME (0)
    // al procesar estos datos parciales, según tu descripción.
}
