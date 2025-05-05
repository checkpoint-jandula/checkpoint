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
public class JuegoPlataformaIGDBId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "juego_id")
    private Long juegoId;

    @Column(name = "plataforma_igdb_id")
    private Long plataformaIgdbId;
}