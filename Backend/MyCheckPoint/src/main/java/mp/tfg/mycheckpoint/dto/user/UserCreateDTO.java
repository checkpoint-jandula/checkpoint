package mp.tfg.mycheckpoint.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// Ya no se importan los enums aquí porque no se usan en este DTO
// import mp.tfg.mycheckpoint.dto.enums.TemaEnum;
// import mp.tfg.mycheckpoint.dto.enums.VisibilidadEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {

    @JsonProperty("nombre_usuario")
    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre de usuario debe tener entre 3 y 100 caracteres")
    private String nombreUsuario;

    @JsonProperty("email")
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El formato del email no es válido")
    @Size(max = 255, message = "El email no puede exceder los 255 caracteres")
    private String email;

    @JsonProperty("contraseña")
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String contraseña;

    // --- CAMPOS ELIMINADOS ---
    // private TemaEnum tema;
    // private Boolean notificaciones;
    // private VisibilidadEnum visibilidadPerfil;
    // --- FIN CAMPOS ELIMINADOS ---
}