package mp.tfg.mycheckpoint.dto.lista;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListaCreateDTO {

    @NotBlank(message = "El nombre de la lista no puede estar vacío")
    @Size(max = 255, message = "El nombre de la lista no puede exceder los 255 caracteres")
    private String nombre;

    // El usuario_id se obtiene del contexto de seguridad o de la URL, no del body de la petición.
}