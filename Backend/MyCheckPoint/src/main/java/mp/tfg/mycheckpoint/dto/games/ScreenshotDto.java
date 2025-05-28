package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenshotDto {
    @JsonProperty("id")
    private Long igdbId; // ID del screenshot desde la fuente externa

    private String url;
}
