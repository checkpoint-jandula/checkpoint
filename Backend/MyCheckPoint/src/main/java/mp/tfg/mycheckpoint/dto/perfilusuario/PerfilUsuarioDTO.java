package mp.tfg.mycheckpoint.dto.perfilusuario;

import lombok.*;
import mp.tfg.mycheckpoint.entity.enums.TemaEnum; // Importar los ENUMs utilizados en la entidad
import mp.tfg.mycheckpoint.entity.enums.VisibilidadEnum;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilUsuarioDTO {

    private Long id;
    private Long usuarioId; // ID del usuario asociado
    private TemaEnum tema; // O String si mapeas el Enum a String en el DTO
    private String fotoPerfil; // La OpenAPI sugiere string Base64 o URL
    private Boolean notificaciones;
    private VisibilidadEnum visibilidadPerfil; // O String si mapeas el Enum a String en el DTO
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    private OffsetDateTime fechaEliminacion; // Para soft delete, puede ser null

    // No incluimos el objeto Usuario completo aquí para evitar ciclos y mantener el DTO ligero.
}