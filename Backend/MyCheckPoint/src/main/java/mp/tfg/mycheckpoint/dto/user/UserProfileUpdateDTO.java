package mp.tfg.mycheckpoint.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull; // Para campos que pueden ser null pero deben estar en el JSON si se actualizan
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.TemaEnum;
import mp.tfg.mycheckpoint.dto.enums.VisibilidadEnum;

@Schema(description = "DTO para actualizar el perfil de un usuario. Solo los campos presentes y no nulos (o que cumplan @NotNull) serán considerados para la actualización. " +
        "Para campos como 'foto_perfil', enviar un nuevo valor actualiza, enviar null o no enviar el campo lo deja sin cambios.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDTO {

    @Schema(description = "Nuevo nombre de usuario para la cuenta. Si se proporciona, debe tener entre 3 y 100 caracteres. Si no se envía o es nulo, no se actualiza.",
            example = "usuarioActualizado", minLength = 3, maxLength = 100, nullable = true)
    @JsonProperty("nombre_usuario")
    @Size(min = 3, max = 100, message = "El nombre de usuario debe tener entre 3 y 100 caracteres si se proporciona.")
    private String nombreUsuario;

    @Schema(description = "Nuevo tema de la interfaz preferido por el usuario. Si no se envía o es nulo, no se actualiza.",
            example = "OSCURO", allowableValues = {"CLARO", "OSCURO"}, nullable = true)
    @JsonProperty("tema")
    private TemaEnum tema;

    @Schema(description = "Nueva URL de la foto de perfil del usuario. Este campo normalmente se actualiza a través del endpoint de subida de imágenes. " +
            "Si se proporciona aquí, debería ser una URL válida a una imagen ya alojada. Si no se envía o es nulo, no se actualiza.",
            example = "/profile-pictures/nuevo_id_usuario.png", nullable = true)
    @JsonProperty("foto_perfil")
    private String fotoPerfil;

    @Schema(description = "Preferencia para recibir notificaciones. Si se envía, no puede ser nulo.", example = "false", nullable = true)
    @JsonProperty("notificaciones")
    @NotNull(message = "El campo de notificaciones no puede ser nulo si se desea actualizar") // Opcional si quieres que siempre se envíe
    private Boolean notificaciones;

    @Schema(description = "Nuevo nivel de visibilidad del perfil del usuario. Si no se envía o es nulo, no se actualiza.",
            example = "SOLO_AMIGOS", allowableValues = {"PUBLICO", "PRIVADO", "SOLO_AMIGOS"}, nullable = true)
    @JsonProperty("visibilidad_perfil")
    private VisibilidadEnum visibilidadPerfil;
}