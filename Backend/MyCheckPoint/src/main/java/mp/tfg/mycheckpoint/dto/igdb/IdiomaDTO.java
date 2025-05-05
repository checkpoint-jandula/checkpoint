package mp.tfg.mycheckpoint.dto.igdb;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdiomaDTO {

    private Long id; // Opcional
    private Long idigdbLanguageId; // Puede ser útil exponerlo
    private String nombre;
    private String locale; // es, en_US, etc.

    // Nota: El tipo de soporte (Interfaz, Audio, Subtítulos) no está en la entidad Idioma,
    // sino en la entidad de unión JuegoIdiomaSoporte. Si quieres exponer esa información
    // asociada a un juego, necesitarás el DTO de la entidad de unión.
}