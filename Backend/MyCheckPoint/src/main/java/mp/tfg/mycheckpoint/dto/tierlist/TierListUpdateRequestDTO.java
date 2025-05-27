package mp.tfg.mycheckpoint.dto.tierlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para actualizar los metadatos de una Tier List existente (nombre, descripción, visibilidad). " +
        "Solo los campos proporcionados (no nulos) serán actualizados.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierListUpdateRequestDTO {

    @Schema(description = "Nuevo nombre para la Tier List. Si se proporciona, debe tener entre 1 y 150 caracteres.",
            example = "Mis Juegos Indie Favoritos (Actualizado)", minLength = 1, maxLength = 150, nullable = true)
    @Size(min = 1, max = 150, message = "El nombre de la Tier List debe tener entre 1 y 150 caracteres.")
    @JsonProperty("name")
    private String name;

    @Schema(description = "Nueva descripción para la Tier List. Si se proporciona, no puede exceder los 1000 caracteres.",
            example = "Una lista actualizada de mis juegos indie preferidos.", maxLength = 1000, nullable = true)
    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres.")
    @JsonProperty("description")
    private String description;

    @Schema(description = "Nuevo estado de visibilidad para la Tier List (true para pública, false para privada). " +
            "Si se proporciona, se actualizará el estado.",
            example = "true", nullable = true)
    @JsonProperty("is_public")
    private Boolean isPublic;
}