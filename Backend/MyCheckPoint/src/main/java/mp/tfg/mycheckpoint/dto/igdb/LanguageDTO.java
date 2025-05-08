package mp.tfg.mycheckpoint.dto.igdb;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO para la entidad Keyword.
 * Se utiliza para mapear los datos de la API de IGDB.
 */
@Data
public class LanguageDTO {
    @JsonProperty("id") private Long id;
    @JsonProperty("id_igdb_language_id") private Long idigdbLanguageId;
    @JsonProperty("nombre") private String nombre;
    @JsonProperty("locale") private String locale;
}