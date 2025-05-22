package mp.tfg.mycheckpoint.dto.tierlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TierListItemAddRequestDTO {

    @NotNull(message = "El ID de UserGame no puede ser nulo.")
    @JsonProperty("user_game_id")
    private Long userGameId;

    @JsonProperty("order")
    private Integer order;
}