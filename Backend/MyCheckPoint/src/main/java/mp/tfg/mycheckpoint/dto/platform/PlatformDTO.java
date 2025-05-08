package mp.tfg.mycheckpoint.dto.platform;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Corresponde a Plataforma en OpenAPI modificado a snake_case
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("id_igdb")
    private Integer idigdb;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("nombre_alternativo")
    private String nombreAlternativo;

    @JsonProperty("logo_url")
    private String logoUrl;
}