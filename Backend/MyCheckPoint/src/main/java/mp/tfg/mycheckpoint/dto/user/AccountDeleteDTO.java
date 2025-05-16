package mp.tfg.mycheckpoint.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountDeleteDTO {

    @NotBlank(message = "La contraseña actual es requerida para eliminar la cuenta.")
    @JsonProperty("contraseña_actual")
    private String contraseñaActual;
}