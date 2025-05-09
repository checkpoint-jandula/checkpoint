package mp.tfg.mycheckpoint.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonProperty("identificador")
    @NotBlank(message = "El identificador (email o nombre de usuario) no puede estar vacío")
    private String identificador; // Puede ser el email o el nombre_usuario

    @JsonProperty("contraseña")
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String contraseña;
}