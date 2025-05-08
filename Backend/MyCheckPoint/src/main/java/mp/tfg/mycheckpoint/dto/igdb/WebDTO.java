package mp.tfg.mycheckpoint.dto.igdb;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO para la entidad Keyword.
 * Se utiliza para mapear los datos de la API de IGDB.
 */
@Data public class WebDTO {
    @JsonProperty("id") private Long id;
    @JsonProperty("id_igdb_website_id") private Long idigdbWebsiteId;
    @JsonProperty("categoria") private Integer categoria;
    @JsonProperty("url") private String url;
    @JsonProperty("trusted") private Boolean trusted;
}