package mp.tfg.mycheckpoint.dto.igdb;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompaniaDTO {

    private Long id; // Opcional
    private Long idigdb;
    private String nombre;
    private Boolean developer;
    private Boolean publisher;
    private Boolean porting;
    private Boolean supporting;

}