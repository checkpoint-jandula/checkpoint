package mp.tfg.mycheckpoint.dto.igdb;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO para la entidad Keyword.
 * Se utiliza para mapear los datos de la API de IGDB.
 */
@Data public class VideoDTO {
    @JsonProperty("id") private Long id;
    @JsonProperty("id_igdb_video_id") private String idigdbVideoId;
    @JsonProperty("nombre") private String nombre;
    @JsonProperty("video_id") private String videoId;
}