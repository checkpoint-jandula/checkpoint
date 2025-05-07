package mp.tfg.mycheckpoint.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty; // Importar
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.TemaEnum;
import mp.tfg.mycheckpoint.dto.enums.VisibilidadEnum;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @JsonProperty("public_id")
    private UUID publicId;

    @JsonProperty("nombre_usuario")
    private String nombreUsuario;

    @JsonProperty("email")
    private String email;

    @JsonProperty("fecha_registro")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime fechaRegistro;

    @JsonProperty("tema")
    private TemaEnum tema;

    @JsonProperty("foto_perfil")
    private String fotoPerfil;

    @JsonProperty("notificaciones")
    private Boolean notificaciones;

    @JsonProperty("visibilidad_perfil")
    private VisibilidadEnum visibilidadPerfil;
}