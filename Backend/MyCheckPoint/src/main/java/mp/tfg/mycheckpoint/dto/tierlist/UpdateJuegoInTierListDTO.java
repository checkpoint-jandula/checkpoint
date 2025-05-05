package mp.tfg.mycheckpoint.dto.tierlist;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateJuegoInTierListDTO {

    @NotNull(message = "El ID del nuevo nivel de Tier List es obligatorio")
    private Long nivelTier_id; // Coincide con el nombre en tu OpenAPI

    // El ID de la TierList y el ID del juego se obtienen de la URL.
    // Solo se permite actualizar el nivel al que está asociado el juego.
}