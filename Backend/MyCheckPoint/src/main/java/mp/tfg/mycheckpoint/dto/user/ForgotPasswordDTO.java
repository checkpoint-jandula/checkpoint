package mp.tfg.mycheckpoint.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para la solicitud de restablecimiento de contraseña. Requiere el correo electrónico asociado a la cuenta.")
@Data
@NoArgsConstructor
public class ForgotPasswordDTO {
    @Schema(description = "Correo electrónico del usuario que solicita el restablecimiento de contraseña. Debe ser un formato de email válido.",
            example = "usuario@example.com", requiredMode = Schema.RequiredMode.REQUIRED, format = "email")
    @NotBlank(message = "El email no puede estar vacío.")
    @Email(message = "El formato del email no es válido.")
    @JsonProperty("email")
    private String email;
}
