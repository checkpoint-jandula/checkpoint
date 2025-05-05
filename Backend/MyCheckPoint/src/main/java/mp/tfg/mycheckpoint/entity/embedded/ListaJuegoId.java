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
public class ListaJuegoId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "lista_id")
    private Long listaId;

    @Column(name = "juego_id")
    private Long juegoId;
}