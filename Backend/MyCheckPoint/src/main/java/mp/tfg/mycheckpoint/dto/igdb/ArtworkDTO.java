package mp.tfg.mycheckpoint.dto.igdb;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO para la entidad Keyword.
 * Se utiliza para mapear los datos de la API de IGDB.
 */
@Data
public class ArtworkDTO {
    @JsonProperty("id") private Long id;
    @JsonProperty("id_igdb_image_id") private String idigdbImageId;
    @JsonProperty("url") private String url;
}