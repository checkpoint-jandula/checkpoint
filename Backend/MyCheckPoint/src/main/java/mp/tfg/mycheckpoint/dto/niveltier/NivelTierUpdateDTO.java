package mp.tfg.mycheckpoint.dto.niveltier;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NivelTierUpdateDTO {

    @Size(max = 100, message = "El nombre del nivel no puede exceder los 100 caracteres")
    private String nombre; // Puede ser null en la petición si no se actualiza

    @Min(value = 0, message = "El orden del nivel no puede ser negativo")
    private Integer orden; // Puede ser null en la petición

    @Size(max = 7, message = "El color debe tener un formato hexadecimal válido (ej. #RRGGBB)")
    private String color; // Puede ser null

    private String descripcion; // Puede ser null

    // No se incluye el ID del nivel ni el de la tier list.
    // Los campos null en este DTO pueden significar "no cambiar este campo".
}