package mp.tfg.mycheckpoint.dto.tierlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierListCreateRequestDTO {

    @NotBlank(message = "El nombre de la Tier List no puede estar vacío.")
    @Size(min = 1, max = 150, message = "El nombre de la Tier List debe tener entre 1 y 150 caracteres.")
    @JsonProperty("name")
    private String name;

    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres.")
    @JsonProperty("description")
    private String description;

    @JsonProperty("is_public")
    private Boolean isPublic = false;
}