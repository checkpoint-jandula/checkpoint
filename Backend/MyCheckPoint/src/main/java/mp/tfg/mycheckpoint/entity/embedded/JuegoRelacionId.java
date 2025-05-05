package mp.tfg.mycheckpoint.entity.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import mp.tfg.mycheckpoint.entity.enums.GameTypeEnum;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class JuegoRelacionId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "juego_origen_id")
    private Long juegoOrigenId;

    @Column(name = "juego_destino_id")
    private Long juegoDestinoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_relacion", nullable = false)
    private GameTypeEnum tipoRelacion; // DLC, EXPANSION (o SIMILAR si se añade)
}