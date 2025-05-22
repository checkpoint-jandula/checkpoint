package mp.tfg.mycheckpoint.dto.tierlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TierListItemMoveRequestDTO {

    @NotNull(message = "El ID de la sección destino no puede ser nulo.")
    @JsonProperty("target_section_internal_id")
    private Long targetSectionInternalId;

    @NotNull(message = "El nuevo orden no puede ser nulo.")
    @JsonProperty("new_order")
    private Integer newOrder;
}