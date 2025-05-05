package mp.tfg.mycheckpoint.dto.tierlist;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddJuegoToTierListDTO {

    @NotNull(message = "El ID del juego es obligatorio")
    private Long juego_id; // Coincide con el nombre en tu OpenAPI

    @NotNull(message = "El ID del nivel de Tier List es obligatorio")
    private Long nivelTier_id; // Coincide con el nombre en tu OpenAPI

    // El ID de la TierList se obtiene de la URL.
}