package mp.tfg.mycheckpoint.dto.igdb;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebDTO {

    private Long id; // Opcional
    private Long idigdbWebsiteId; // Puede ser útil exponerlo
    private Integer categoria; // Categoría de la web según IGDB (puede ser un Enum si mapeas los valores)
    private String url;
    private Boolean trusted;

}