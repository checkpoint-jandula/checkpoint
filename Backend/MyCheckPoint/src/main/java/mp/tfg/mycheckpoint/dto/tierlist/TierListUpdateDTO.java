package mp.tfg.mycheckpoint.dto.tierlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierListUpdateDTO {

    @NotBlank(message = "El nombre de la Tier List no puede estar vacío")
    @Size(max = 255, message = "El nombre de la Tier List no puede exceder los 255 caracteres")
    private String nombre;

    // Solo se permite actualizar el nombre según tu OpenAPI.
}