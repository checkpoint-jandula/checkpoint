package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformDto {
    @JsonProperty("id")
    private Long igdbId;

    @JsonProperty("alternative_name")
    private String alternativeName;

    private String name;

    @JsonProperty("platform_logo")
    private PlatformLogoDto platformLogo;
}
