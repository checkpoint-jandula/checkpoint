package mp.tfg.mycheckpoint.dto.igdb;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkDTO {

    private Long id; // Opcional
    private String idigdbImageId; // Puede ser útil exponerlo
    private String url;

}