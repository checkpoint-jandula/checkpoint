package mp.tfg.mycheckpoint.dto.perfilusuario;

import lombok.*;
import mp.tfg.mycheckpoint.entity.enums.TemaEnum; // Importar los ENUMs utilizados en la entidad
import mp.tfg.mycheckpoint.entity.enums.VisibilidadEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilUsuarioUpdateDTO {

    private TemaEnum tema; // O String
    private Boolean notificaciones; // Puede ser null en la petición
    private VisibilidadEnum visibilidadPerfil; // O String

    // La foto de perfil se actualiza en un endpoint separado y no está aquí.
    // Los campos null pueden significar "no cambiar".
}