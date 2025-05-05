package mp.tfg.mycheckpoint.dto.niveltier;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NivelTierCreateDTO {

    @NotBlank(message = "El nombre del nivel no puede estar vacío")
    @Size(max = 100, message = "El nombre del nivel no puede exceder los 100 caracteres")
    private String nombre;

    @NotNull(message = "El orden del nivel es obligatorio")
    @Min(value = 0, message = "El orden del nivel no puede ser negativo")
    private Integer orden;

    @Size(max = 7, message = "El color debe tener un formato hexadecimal válido (ej. #RRGGBB)")
    private String color; // Puede ser null

    private String descripcion; // Puede ser null

    // El ID de la TierList padre se obtiene de la URL.
}