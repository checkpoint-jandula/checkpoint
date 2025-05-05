package mp.tfg.mycheckpoint.dto.tierlist;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierListDTO {

    private Long id;
    private String nombre;
    private Long usuarioId; // ID del usuario propietario
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    private OffsetDateTime fechaEliminacion;

    // La OpenAPI sugiere opcionalmente info de niveles o contador de juegos.
    // Si quieres incluir el número de niveles o juegos en el DTO básico:
    // private Integer numNiveles;
    // private Integer numJuegos;

    // No incluimos los objetos NivelTier o TierListJuego aquí.
}