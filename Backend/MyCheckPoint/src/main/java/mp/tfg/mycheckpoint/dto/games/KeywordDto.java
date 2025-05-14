package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeywordDto {
    @JsonProperty("id")
    private Long igdbId; // ID de la palabra clave desde la fuente externa

    private String name;
}