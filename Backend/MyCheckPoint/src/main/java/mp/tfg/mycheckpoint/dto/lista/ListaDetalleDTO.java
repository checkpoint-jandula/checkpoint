package mp.tfg.mycheckpoint.dto.lista;

import lombok.*;
import mp.tfg.mycheckpoint.dto.juego.JuegoSummaryDTO; // Para incluir resúmenes de juegos

import java.time.OffsetDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Puede extender de ListaDTO para heredar los campos básicos, o listarlos todos.
public class ListaDetalleDTO extends ListaDTO { // Extender para heredar campos básicos

    // La OpenAPI indica que este DTO incluye una lista de juegos.
    private Set<JuegoSummaryDTO> juegos; // Lista de juegos en la lista (usando el DTO resumen de Juego)

    // Si no heredas de ListaDTO, tendrías que listar todos los campos de ListaDTO aquí:
    /*
    private Long id;
    private String nombre;
    private Long usuarioId;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    private OffsetDateTime fechaEliminacion;
    private Integer numJuegos; // Si está incluido en ListaDTO
    */
}