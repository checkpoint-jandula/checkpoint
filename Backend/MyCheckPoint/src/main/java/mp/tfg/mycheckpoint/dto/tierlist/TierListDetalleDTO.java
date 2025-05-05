package mp.tfg.mycheckpoint.dto.tierlist;

import lombok.*;
import mp.tfg.mycheckpoint.dto.juego.JuegoSummaryDTO; // Para incluir resúmenes de juegos
import mp.tfg.mycheckpoint.dto.niveltier.NivelTierDTO; // Para incluir detalles de niveles

import java.time.OffsetDateTime;
import java.util.List; // Usamos List porque NivelTier tiene un orden
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Puede extender de TierListDTO para heredar los campos básicos, o listarlos todos.
public class TierListDetalleDTO extends TierListDTO { // Extender para heredar campos básicos

    // La OpenAPI indica que este DTO incluye una lista de niveles.
    // Cada nivel a su vez contiene el NivelTierDTO y una lista de juegos.
    // Creamos una clase interna o un DTO separado para representar la estructura { nivel: NivelTierDTO, juegos: [JuegoSummaryDTO] }
    private List<NivelConJuegosDTO> niveles; // Usamos List para mantener el orden de los niveles

    // Clase interna para representar la estructura de un nivel con sus juegos
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NivelConJuegosDTO {
        private NivelTierDTO nivel;
        private Set<JuegoSummaryDTO> juegos; // Usamos Set para los juegos dentro de un nivel (no hay orden explícito en la tabla de unión para esto)
    }

    // Si no heredas de TierListDTO, tendrías que listar todos los campos de TierListDTO aquí:
    /*
    private Long id;
    private String nombre;
    private Long usuarioId;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    private OffsetDateTime fechaEliminacion;
    // ... otros campos de TierListDTO
    */
}