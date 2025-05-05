package mp.tfg.mycheckpoint.dto.lista;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListaDTO {

    private Long id;
    private String nombre;
    private Long usuarioId; // ID del usuario propietario
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    private OffsetDateTime fechaEliminacion;

    // Tu OpenAPI sugiere opcionalmente un campo 'num_juegos' en el esquema Lista.
    // Si quieres incluirlo, se calcularía en el servicio o repositorio y se añadiría aquí.
    private Integer numJuegos;

}