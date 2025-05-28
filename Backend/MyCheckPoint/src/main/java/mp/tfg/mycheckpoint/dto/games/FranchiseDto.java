package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa una franquicia de videojuegos.
 */
@Schema(description = "DTO para una franquicia de videojuegos.") // Añadido Schema a nivel de clase
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FranchiseDto {

    /**
     * ID único de la franquicia, generalmente proveniente de IGDB.
     */
    @Schema(description = "ID de la franquicia desde IGDB.", example = "136") // Añadido Schema
    @JsonProperty("id")
    private Long igdbId;


    /**
     * Nombre de la franquicia.
     */
    @Schema(description = "Nombre de la franquicia.", example = "The Legend of Zelda") // Añadido Schema
    private String name;
}