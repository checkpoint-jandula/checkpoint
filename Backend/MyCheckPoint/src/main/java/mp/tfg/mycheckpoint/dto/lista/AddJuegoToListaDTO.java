package mp.tfg.mycheckpoint.dto.lista;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddJuegoToListaDTO {

    // Según tu OpenAPI, el body de la petición para añadir un juego a una lista
    // solo necesita el ID del juego.
    @NotNull(message = "El ID del juego es obligatorio")
    private Long juegoId;

    // El ID de la lista se obtiene de la URL.
}