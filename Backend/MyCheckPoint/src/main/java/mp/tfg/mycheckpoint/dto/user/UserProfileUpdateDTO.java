package mp.tfg.mycheckpoint.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull; // Para campos que pueden ser null pero deben estar en el JSON si se actualizan
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.TemaEnum;
import mp.tfg.mycheckpoint.dto.enums.VisibilidadEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDTO {

    // Los campos que no están aquí no se podrán actualizar mediante este DTO
    // (ej. email, nombre_usuario, contraseña)

    // Nota: OpenAPI define UsuarioUpdate con nombreUsuario y email.
    // Decidimos aquí que este DTO específico para /me/perfil solo actualiza estos campos.
    // Si quisieras actualizar nombre_usuario o email, considera las implicaciones (unicidad, verificación de email).
    // Por simplicidad, este DTO solo actualiza preferencias de perfil.

    @JsonProperty("nombre_usuario")
    @Size(min = 3, max = 100, message = "El nombre de usuario debe tener entre 3 y 100 caracteres si se proporciona.")
    private String nombreUsuario; // Nuevo campo para actualizar nombre de usuario


    @JsonProperty("tema")
    private TemaEnum tema; // El usuario puede enviar null si no quiere cambiarlo, o un nuevo valor

    @JsonProperty("foto_perfil")
    private String fotoPerfil; // Ruta a la nueva imagen

    @JsonProperty("notificaciones")
    @NotNull(message = "El campo de notificaciones no puede ser nulo si se desea actualizar") // Opcional si quieres que siempre se envíe
    private Boolean notificaciones;

    @JsonProperty("visibilidad_perfil")
    private VisibilidadEnum visibilidadPerfil;
}