package mp.tfg.mycheckpoint.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para la solicitud de inicio de sesión. Requiere un identificador (email o nombre de usuario) y una contraseña.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    // Permitiremos login con email o nombre_usuario
    // En el servicio, intentaremos buscar por uno y luego por el otro.
    // Podrías tener dos campos separados si prefieres que el usuario elija,
    // pero un solo campo "usernameOrEmail" es común.
    // O simplemente un campo "username" y otro "email" y que el usuario rellene uno.
    // Por simplicidad ahora, usaremos un campo "identificador" que puede ser email o nombre_usuario
    @Schema(description = "Identificador del usuario, puede ser su email o su nombre de usuario.",
            example = "usuario123", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("identificador")
    @NotBlank(message = "El identificador (email o nombre de usuario) no puede estar vacío")
    private String identificador; // Puede ser el email o el nombre_usuario

    @Schema(description = "Contraseña del usuario.",
            example = "P@$$wOrd", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
    @JsonProperty("contraseña")
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String contraseña;
}