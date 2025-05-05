package mp.tfg.mycheckpoint.dto.plataforma;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL; // Importar si necesitas validar que logoUrl sea una URL

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlataformaCreateDTO {

    @NotNull(message = "El ID externo (idigdb) es obligatorio")
    private Integer idigdb; // O Long, dependiendo del tipo en tu entidad

    @NotBlank(message = "El nombre de la plataforma es obligatorio")
    @Size(max = 255, message = "El nombre de la plataforma no puede exceder los 255 caracteres")
    private String nombre;

    @Size(max = 255, message = "El nombre alternativo no puede exceder los 255 caracteres")
    private String nombreAlternativo; // Puede ser null

    @URL(message = "La URL del logo no es válida") // Añadir si quieres validar formato URL
    private String logoUrl; // Puede ser null

    // Los campos de fecha y el ID interno se generan en el backend.
}