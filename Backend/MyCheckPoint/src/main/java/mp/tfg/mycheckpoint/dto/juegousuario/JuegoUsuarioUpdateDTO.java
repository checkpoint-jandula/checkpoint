package mp.tfg.mycheckpoint.dto.juegousuario;

import lombok.*;
import mp.tfg.mycheckpoint.entity.enums.EstadoEnum;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JuegoUsuarioUpdateDTO {

    private EstadoEnum estado; // O String (puedes permitir null si quieres actualizar solo algunos campos)
    private Boolean posesion; // Opcional
    private Float puntuacion; // Opcional
    private String comentario; // Opcional
    private String comentarioPrivado; // Opcional
    private LocalDate fechaInicio; // Opcional
    private LocalDate fechaFin; // Opcional
    private Float duracionHistoria; // Opcional
    private Float duracionHistoriaSecundarias; // Opcional
    private Float duracionCompletista; // Opcional
    private Long plataformaId; // ID de nuestra Plataforma (opcional, permite cambiarla o poner null)

    // No se incluyen IDs de usuario ni juego, se obtienen de la URL o contexto.
    // Los campos null en este DTO pueden interpretarse como "no cambiar este campo".
}