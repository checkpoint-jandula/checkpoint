package mp.tfg.mycheckpoint.dto.usuario;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioSummaryDTO {

    private UUID publicId;
    private String nombreUsuario;
    // Podrías añadir otros campos públicos como la URL de la foto de perfil pequeña
    // private String profilePictureUrl;
}