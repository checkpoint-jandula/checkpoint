package mp.tfg.mycheckpoint.dto.junction;

import lombok.*;
// Puedes importar DTOs de resumen si quieres anidar información de las entidades relacionadas
// import mp.tfg.mycheckpoint.dto.lista.ListaDTO;
// import mp.tfg.mycheckpoint.dto.juego.JuegoSummaryDTO;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListaJuegoDTO {

    // Exponemos los IDs de las entidades relacionadas, que forman el EmbeddedId
    private Long listaId;
    private Long juegoId;

    private OffsetDateTime fechaCreacion;
    // private OffsetDateTime fechaModificacion; // Si se añade en la entidad
    // private OffsetDateTime fechaEliminacion; // Si se añade en la entidad (soft delete)

    // Opcionalmente, anidar DTOs de las entidades relacionadas
    // private ListaDTO lista; // Raramente útil aquí
    // private JuegoSummaryDTO juego; // Útil si necesitas info del juego en este DTO
}