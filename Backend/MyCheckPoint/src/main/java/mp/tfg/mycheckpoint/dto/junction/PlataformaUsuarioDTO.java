package mp.tfg.mycheckpoint.dto.junction;

import lombok.*;
// Opcionalmente, importar DTOs de resumen
// import mp.tfg.mycheckpoint.dto.usuario.UsuarioSummaryDTO;
// import mp.tfg.mycheckpoint.dto.plataforma.PlataformaDTO; // O PlataformaSummaryDTO

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlataformaUsuarioDTO {

    // Exponemos los IDs de las entidades relacionadas, que forman el EmbeddedId
    private Long usuarioId;
    private Long plataformaId;

    private OffsetDateTime fechaCreacion;

    // Opcionalmente, anidar DTOs de las entidades relacionadas
    // private UsuarioSummaryDTO usuario; // Útil si necesitas info del usuario
    // private PlataformaDTO plataforma; // Útil si necesitas info de la plataforma
}