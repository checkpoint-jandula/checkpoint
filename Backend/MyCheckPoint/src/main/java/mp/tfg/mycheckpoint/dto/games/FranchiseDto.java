package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FranchiseDto {

    @JsonProperty("id")
    private Long igdbId; // ID de la franquicia en IGDB


    private String name; // Nombre de la franquicia
}
