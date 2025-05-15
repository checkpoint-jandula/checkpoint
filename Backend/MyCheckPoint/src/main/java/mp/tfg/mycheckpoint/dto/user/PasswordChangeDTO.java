package mp.tfg.mycheckpoint.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDTO {

    @NotBlank(message = "La contraseña actual no puede estar vacía.")
    @JsonProperty("contraseña_actual")
    private String contraseñaActual;

    @NotBlank(message = "La nueva contraseña no puede estar vacía.")
    @Size(min = 8, max = 100, message = "La nueva contraseña debe tener entre 8 y 100 caracteres.")
    @JsonProperty("nueva_contraseña")
    private String nuevaContraseña;

    // Opcional: Confirmación de la nueva contraseña para evitar errores de tipeo.
    // Si lo añades, asegúrate de validarlo en el servicio.
    /*
    @NotBlank(message = "La confirmación de la nueva contraseña no puede estar vacía.")
    @JsonProperty("confirmar_nueva_contraseña")
    private String confirmarNuevaContraseña;
    */
}
