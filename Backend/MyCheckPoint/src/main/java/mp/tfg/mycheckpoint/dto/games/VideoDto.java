package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
    @JsonProperty("id")
    private Long igdbId; // ID del video desde la fuente externa

    private String name;

    @JsonProperty("video_id") // Para mapear la clave JSON "video_id"
    private String videoId;
}
