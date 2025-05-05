package mp.tfg.mycheckpoint.entity.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TierListJuegoId implements Serializable {
    // Aunque la tabla TierListJuego tiene UNIQUE (tierlist_id, juego_id),
    // la PK implícita o explícita podría incluir nivelTier_id si se quisiera.
    // Siguiendo la estructura y el SQL, parece que la combinación (tierlist_id, juego_id)
    // es la clave lógica para identificar la relación.
    // Si la PK fuera (tierlist_id, juego_id, nivelTier_id), se añadiría aquí.
    // Vamos a asumir que (tierlist_id, juego_id) es suficiente como ID embebido
    // y nivelTier_id es un atributo de la entidad TierListJuego.
    // Sin embargo, el SQL tiene UNIQUE(tierlist_id, juego_id), lo cual sugiere que esta
    // combinación identifica la relación. Usaremos esta como ID.

    private static final long serialVersionUID = 1L;

    @Column(name = "tierlist_id")
    private Long tierListId;

    @Column(name = "juego_id")
    private Long juegoId;
}