package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO que representa el logo de una plataforma de videojuegos.
 */
@Schema(description = "DTO para el logo de una plataforma de videojuegos.") // Añadido Schema a nivel de clase
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformLogoDto {
    /**
     * ID único del logo, generalmente proveniente de una fuente externa como IGDB.
     */
    @Schema(description = "ID del logo desde la fuente externa (ej. IGDB).", example = "170") // Añadido Schema
    @JsonProperty("id")
    private Long igdbId;

    /**
     * URL donde se puede encontrar la imagen del logo.
     */
    @Schema(description = "URL de la imagen del logo de la plataforma.", example = "//images.igdb.com/igdb/image/upload/t_logo_med/pl6e.png") // Añadido Schema
    private String url;
}
