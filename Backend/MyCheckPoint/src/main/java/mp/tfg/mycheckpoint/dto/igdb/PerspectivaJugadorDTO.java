package mp.tfg.mycheckpoint.dto.igdb;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerspectivaJugadorDTO {

    private Long id; // Opcional
    private Long idigdb;
    private String nombre;

}