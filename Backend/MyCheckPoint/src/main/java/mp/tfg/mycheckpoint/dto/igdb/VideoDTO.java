package mp.tfg.mycheckpoint.dto.igdb;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoDTO {

    private Long id; // Opcional
    private String idigdbVideoId; // Puede ser útil exponerlo
    private String nombre;
    private String videoId; // El ID en la plataforma externa (ej. YouTube)

}