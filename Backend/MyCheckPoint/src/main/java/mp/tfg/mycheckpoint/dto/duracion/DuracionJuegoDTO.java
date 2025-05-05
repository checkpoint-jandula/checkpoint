package mp.tfg.mycheckpoint.dto.duracion;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DuracionJuegoDTO {

    // Mapeamos los campos de la entidad DuracionJuego
    private Long idDuracion; // Corresponde a 'id_duracion' en la entidad
    private Long juegoId; // Corresponde a 'juego.id' en la entidad (FK al juego)
    private Double mediaHistoria; // Usamos Double ya que AVG en JPQL devuelve Double
    private Double mediaHistoriaSecundarias;
    private Double mediaCompletista;
    private Integer numeroUsuarios;
    private OffsetDateTime fechaModificacion;

    // Según el OpenAPI, solo se devuelven estos campos para este DTO.
    // Si en el futuro necesitaras devolver el juego asociado, crearías un DTO de detalle.
}