package mp.tfg.mycheckpoint.dto.juego;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JuegoSummaryDTO {

    private Long id;
    private Long idigdb;
    private String nombre;
    private String slug;
    private String coverUrl;
    private LocalDate fechaLanzamiento;
    // Puedes añadir otros campos de resumen si son relevantes (ej: totalRating)
    private Float totalRating;

}