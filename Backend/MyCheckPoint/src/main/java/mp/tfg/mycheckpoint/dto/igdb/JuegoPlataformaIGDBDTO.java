package mp.tfg.mycheckpoint.dto.igdb;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JuegoPlataformaIGDBDTO { // Asumo que este es el nombre que le diste

    // No incluimos el ID compuesto de la entidad de unión aquí.
    // En su lugar, incluimos los DTOs de las entidades relacionadas y los atributos de la unión.

    private PlataformaIgdbDTO plataformaIgdb; // Detalles de la plataforma de IGDB asociada
    private LocalDate fechaLanzamiento; // El atributo adicional de la unión

    // No incluimos el JuegoDTO aquí para evitar recursión, ya que este DTO estará DENTRO de un JuegoDTO.
    // Si necesitaras el ID del juego, podrías añadir:
    // private Long juegoId;

}