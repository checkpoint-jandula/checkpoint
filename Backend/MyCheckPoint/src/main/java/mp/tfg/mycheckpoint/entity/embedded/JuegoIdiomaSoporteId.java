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
public class JuegoIdiomaSoporteId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "juego_id")
    private Long juegoId;

    @Column(name = "idioma_id")
    private Long idiomaId;

    @Column(name = "tipoSoporte") // tipoSoporte INT NOT NULL
    private Integer tipoSoporte; // 1=Interfaz, 2=Audio, 3=Subtítulos
}