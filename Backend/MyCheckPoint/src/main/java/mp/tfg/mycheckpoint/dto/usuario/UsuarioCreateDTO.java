package mp.tfg.mycheckpoint.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioCreateDTO {

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre de usuario debe tener entre 3 y 100 caracteres")
    private String nombreUsuario;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 255, message = "El email no puede exceder los 255 caracteres")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password; // Usar "password" en el DTO es más estándar que "contraseña"

    // No se incluyen IDs ni fechas de creación/registro en el DTO de creación
}