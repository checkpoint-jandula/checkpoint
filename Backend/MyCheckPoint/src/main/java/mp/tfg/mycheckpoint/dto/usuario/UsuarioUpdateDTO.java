package mp.tfg.mycheckpoint.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioUpdateDTO {

    @Size(min = 3, max = 100, message = "El nombre de usuario debe tener entre 3 y 100 caracteres")
    private String nombreUsuario; // Hacerlo opcional en update si no siempre se cambia

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 255, message = "El email no puede exceder los 255 caracteres")
    private String email; // Hacerlo opcional en update

    // Si permites cambio de contraseña en este endpoint, incluir campos
    private String currentPassword;
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    private String newPassword;

    // No se incluyen IDs ni fechas en el DTO de actualización
    // Los campos null en el DTO podrían significar "no cambiar"
}