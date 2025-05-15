package mp.tfg.mycheckpoint.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResetPasswordDTO {

    @NotBlank(message = "El token de reseteo no puede estar vacío.")
    @JsonProperty("token")
    private String token;

    @NotBlank(message = "La nueva contraseña no puede estar vacía.")
    @Size(min = 8, max = 100, message = "La nueva contraseña debe tener entre 8 y 100 caracteres.")
    @JsonProperty("nueva_contraseña")
    private String nuevaContraseña;

    // Opcional: Confirmación de la nueva contraseña
    /*
    @NotBlank(message = "La confirmación de la nueva contraseña no puede estar vacía.")
    @JsonProperty("confirmar_nueva_contraseña")
    private String confirmarNuevaContraseña;
    */
}
