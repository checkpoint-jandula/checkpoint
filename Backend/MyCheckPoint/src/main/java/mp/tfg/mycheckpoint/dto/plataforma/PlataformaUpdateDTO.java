package mp.tfg.mycheckpoint.dto.plataforma;

import jakarta.validation.constraints.NotNull; // Si idigdb es obligatorio en update
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlataformaUpdateDTO {

    // Según tu OpenAPI, estos campos pueden ser actualizados.
    // Hacerlos no @NotNull si no es obligatorio actualizar todos en un solo PUT.
    private Integer idigdb; // O Long
    @Size(max = 255, message = "El nombre de la plataforma no puede exceder los 255 caracteres")
    private String nombre;
    @Size(max = 255, message = "El nombre alternativo no puede exceder los 255 caracteres")
    private String nombreAlternativo;
    @URL(message = "La URL del logo no es válida")
    private String logoUrl;

    // No se incluye el ID de la plataforma a actualizar, se obtiene de la URL.
    // Los campos null en este DTO pueden indicar "no cambiar este campo",
    // o podrías requerir que todos los campos actualizables estén presentes y usar null explícitamente.
}