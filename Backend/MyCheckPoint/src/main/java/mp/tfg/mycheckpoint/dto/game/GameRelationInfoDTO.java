package mp.tfg.mycheckpoint.dto.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import mp.tfg.mycheckpoint.dto.enums.JuegoRelacionTipoEnum;

@Data
public class GameRelationInfoDTO {
    // Usaremos GameSummaryDTO (que ahora es completo) para representar el juego relacionado
    @JsonProperty("juego")
    private GameSummaryDTO relatedGame; // El juego relacionado (DLC, Similar, etc.)

    @JsonProperty("tipo_relacion")
    private JuegoRelacionTipoEnum relationType;
}