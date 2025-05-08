package mp.tfg.mycheckpoint.dto.igdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Corresponde a Genero en OpenAPI modificado a snake_case
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreDTO {
    @JsonProperty("id") // Este ID podría ser el interno o el id_igdb, depende de qué quieras exponer
    private Long id; // Podría ser el id_igdb para consistencia con IGDB

    @JsonProperty("nombre")
    private String nombre;
}
