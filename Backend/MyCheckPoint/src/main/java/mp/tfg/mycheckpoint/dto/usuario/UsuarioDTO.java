package mp.tfg.mycheckpoint.dto.usuario;

import lombok.*;
import mp.tfg.mycheckpoint.dto.perfilusuario.PerfilUsuarioDTO; // Si PerfilUsuarioDTO incluye detalles del perfil
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Útil si quieres construir DTOs fácilmente en tests o mappers
public class UsuarioDTO {

    private Long id; // Opcional incluir ID interno si se necesita en la respuesta
    private UUID publicId;
    private String nombreUsuario;
    private String email;
    // La contraseña NUNCA se incluye en DTOs de respuesta
    private OffsetDateTime fechaRegistro;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    private OffsetDateTime fechaEliminacion; // Será null si no se ha borrado (soft delete)

    // Puedes incluir resúmenes o DTOs de otras entidades relacionadas si la API lo requiere
    // private PerfilUsuarioDTO perfilUsuario; // Ejemplo: si se devuelve el perfil junto al usuario
    // private Set<JuegoUsuarioDetalleDTO> juegosUsuario; // Ejemplo: si se devuelven sus juegos
    // private Set<ListaDTO> listas; // Ejemplo: si se devuelven sus listas

}