package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GenreDto {
    @JsonProperty("id")
    private Long igdbId;

    private String name;
}