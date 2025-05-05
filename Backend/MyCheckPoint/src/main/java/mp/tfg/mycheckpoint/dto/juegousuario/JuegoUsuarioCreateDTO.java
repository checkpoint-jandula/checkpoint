package mp.tfg.mycheckpoint.dto.juegousuario;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import mp.tfg.mycheckpoint.entity.enums.EstadoEnum;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JuegoUsuarioCreateDTO {

    @NotNull(message = "El ID del juego es obligatorio")
    private Long juegoId; // ID del juego a añadir

    @NotNull(message = "El estado del juego es obligatorio")
    private EstadoEnum estado; // O String

    private Boolean posesion; // Opcional en la petición, si tiene valor por defecto en BD
    private Float puntuacion; // Opcional
    private String comentario; // Opcional
    private String comentarioPrivado; // Opcional
    private LocalDate fechaInicio; // Opcional
    private LocalDate fechaFin; // Opcional
    private Float duracionHistoria; // Opcional
    private Float duracionHistoriaSecundarias; // Opcional
    private Float duracionCompletista; // Opcional
    private Long plataformaId; // ID de nuestra Plataforma (opcional)

    // El usuario_id se obtiene del contexto de seguridad o de la URL, no del body de la petición

}