package mp.tfg.mycheckpoint.dto.igdb;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneroDTO {

    private Long id; // Opcional si solo expones idigdb
    private Long idigdb;
    private String nombre;

}