package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data // Lombok: genera getters, setters, toString, equals, hashCode
public class CoverDto {
    @JsonProperty("id") // Mapea el campo "id" del JSON a este atributo
    private Long igdbId;

    private String url;
}