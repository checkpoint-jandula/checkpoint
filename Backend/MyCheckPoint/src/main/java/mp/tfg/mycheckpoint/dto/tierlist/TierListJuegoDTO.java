package mp.tfg.mycheckpoint.dto.tierlist;

import lombok.*;
import mp.tfg.mycheckpoint.dto.juego.JuegoSummaryDTO; // O JuegoDTO si necesitas más detalles
import mp.tfg.mycheckpoint.dto.niveltier.NivelTierDTO; // O NivelTierSummaryDTO si lo creas
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierListJuegoDTO {

    // En un DTO para la entidad de unión, expones los IDs de las entidades relacionadas
    // y los atributos propios de la tabla de unión.
    // También puedes anidar DTOs de las entidades relacionadas para más detalle.

    private Long tierlistId; // ID de la TierList padre
    private Long juegoId; // ID del Juego asociado
    private Long nivelTierId; // ID del NivelTier asociado

    // Puedes anidar DTOs de resumen o detalle de las entidades relacionadas
    // private TierListDTO tierList; // Raramente útil en este DTO
    // private JuegoSummaryDTO juego; // Útil si necesitas info del juego
    // private NivelTierDTO nivelTier; // Útil si necesitas info del nivel

    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    // fechaEliminacion si aplica soft delete a la unión

}