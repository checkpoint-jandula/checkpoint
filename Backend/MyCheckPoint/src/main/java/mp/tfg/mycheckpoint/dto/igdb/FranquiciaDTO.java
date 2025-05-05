package mp.tfg.mycheckpoint.dto.igdb;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FranquiciaDTO {

    private Long id; // Opcional
    private Long idigdbFranchiseId; // ID de IGDB para la franquicia
    private String nombre;

}