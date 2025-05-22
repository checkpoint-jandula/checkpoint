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
public class TierSectionRequestDTO {

    @NotBlank(message = "El nombre de la sección no puede estar vacío.")
    @Size(min = 1, max = 100, message = "El nombre de la sección debe tener entre 1 y 100 caracteres.")
    @JsonProperty("name")
    private String name;
}