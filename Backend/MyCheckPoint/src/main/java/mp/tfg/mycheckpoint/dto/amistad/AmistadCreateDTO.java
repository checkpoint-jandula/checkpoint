package mp.tfg.mycheckpoint.dto.amistad;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmistadCreateDTO {

    // Aunque los IDs vienen de la URL en tu API, definimos el DTO
    // por si la implementación cambia o se usa internamente.
    // Además, coincide con el esquema AmistadCreate de tu OpenAPI.

    @NotNull(message = "El ID del usuario principal es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El ID del amigo es obligatorio")
    private Long amigoId;

}