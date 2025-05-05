package mp.tfg.mycheckpoint.dto.tierlist;

import lombok.*;
import mp.tfg.mycheckpoint.dto.juego.JuegoSummaryDTO; // O JuegoDTO
import mp.tfg.mycheckpoint.dto.niveltier.NivelTierDTO; // O NivelTierSummaryDTO
// import mp.tfg.mycheckpoint.dto.tierlist.TierListDTO; // Raramente útil aquí

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NivelTierJuegoDTO { // Potencialmente el DTO para la entidad de unión con detalles anidados

    // No incluimos el ID compuesto aquí

    // Incluimos DTOs de las entidades relacionadas
    private NivelTierDTO nivelTier; // Detalles del nivel al que pertenece
    private JuegoSummaryDTO juego; // Detalles del juego asociado
    // private TierListDTO tierList; // Si se necesitara el contexto de la tier list padre

    // Incluimos los atributos propios de la tabla de unión
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    // fechaEliminacion

    // Si este DTO fuera usado en algún lugar donde no se necesita el ID del nivel o juego en la URL,
    // podrías incluirlos también:
    // private Long nivelTierId;
    // private Long juegoId;

}