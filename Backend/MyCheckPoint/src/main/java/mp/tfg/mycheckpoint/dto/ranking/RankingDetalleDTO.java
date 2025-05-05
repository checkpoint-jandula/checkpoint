package mp.tfg.mycheckpoint.dto.ranking;

import lombok.*;
import mp.tfg.mycheckpoint.dto.juego.JuegoSummaryDTO; // Importar el DTO resumen de Juego

// Puede extender de RankingDTO para heredar los campos básicos, o listarlos todos.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingDetalleDTO extends RankingDTO { // Extender para heredar campos básicos

    // La OpenAPI indica que este DTO añade detalles del juego asociado.
    private JuegoSummaryDTO juego; // Detalles resumidos del juego

    // Si no heredas de RankingDTO, tendrías que listar todos los campos de RankingDTO aquí:
    /*
    private Long juegoId;
    private Float puntuacionMedia;
    private Integer numeroVotos;
    private OffsetDateTime fechaModificacion;
    */
}