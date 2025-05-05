package mp.tfg.mycheckpoint.dto.igdb;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemaDTO {

    private Long id; // Opcional
    private Long idigdbThemeId; // ID de IGDB para el tema
    private String nombre;

}