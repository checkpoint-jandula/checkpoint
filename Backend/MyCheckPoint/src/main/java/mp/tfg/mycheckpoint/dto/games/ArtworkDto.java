package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtworkDto {
    @JsonProperty("id")
    private Long igdbId; // El ID del artwork que viene de IGDB u otra fuente

    private String url;
}