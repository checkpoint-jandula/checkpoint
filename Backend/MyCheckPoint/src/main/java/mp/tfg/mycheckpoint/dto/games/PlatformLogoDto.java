package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformLogoDto {
    @JsonProperty("id")
    private Long igdbId; // ID del logo desde la fuente externa

    private String url;
}
