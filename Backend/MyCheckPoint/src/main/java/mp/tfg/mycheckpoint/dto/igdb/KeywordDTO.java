package mp.tfg.mycheckpoint.dto.igdb;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO para la entidad Keyword.
 * Se utiliza para mapear los datos de la API de IGDB.
 */

@Data
public class KeywordDTO {
    @JsonProperty("id") private Long id; // Mapeado desde idigdb
    @JsonProperty("nombre") private String nombre;
}