package mp.tfg.mycheckpoint.dto.igdb;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JuegoIdiomaSoporteDTO {

    // No incluimos el ID compuesto de la entidad de unión aquí.
    // Incluimos el DTO del Idioma relacionado y el atributo de la unión.

    private IdiomaDTO idioma; // Detalles del idioma asociado
    private Integer tipoSoporte; // El atributo adicional de la unión (1=Interfaz, 2=Audio, 3=Subtítulos)
    // Podrías usar un Enum aquí si creas uno para TipoSoporteEnum.

    // No incluimos el JuegoDTO aquí.
    // Si necesitaras el ID del juego, podrías añadir:
    // private Long juegoId;
}