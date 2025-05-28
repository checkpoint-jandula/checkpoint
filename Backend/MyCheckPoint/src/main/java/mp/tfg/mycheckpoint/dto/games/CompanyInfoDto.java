package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO que contiene información básica sobre una compañía (desarrolladora, editora, etc.).
 */
@Schema(description = "DTO con información básica de una compañía (desarrolladora, editora).") // Añadido Schema a nivel de clase
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyInfoDto {
    /**
     * ID único de la compañía, generalmente proveniente de una fuente externa como IGDB.
     */
    @Schema(description = "ID de la compañía desde la fuente externa (ej. IGDB).", example = "1") // Añadido Schema
    @JsonProperty("id")
    private Long igdbId;

    /**
     * Nombre de la compañía.
     */
    @Schema(description = "Nombre de la compañía.", example = "Nintendo") // Añadido Schema
    private String name;
}
