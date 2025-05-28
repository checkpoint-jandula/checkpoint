package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Lombok para constructor sin argumentos
public class GameStatusDto {

    @JsonProperty("id")
    private Integer id; // El ID numérico del estado del juego
}
