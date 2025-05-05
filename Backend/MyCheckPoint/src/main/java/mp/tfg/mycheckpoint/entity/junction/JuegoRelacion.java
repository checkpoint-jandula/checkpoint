package mp.tfg.mycheckpoint.entity.junction;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.Juego;
import mp.tfg.mycheckpoint.entity.embedded.JuegoRelacionId;
// import mp.tfg.mycheckpoint.entity.enums.GameTypeEnum; // Ya está en el ID

@Entity
@Table(name = "juegorelacion", indexes = {
        @Index(name = "idx_juegorelacion_juegodestinoid", columnList = "juego_destino_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"juegoOrigen", "juegoDestino"})
public class JuegoRelacion { // Para DLCs, Expansiones, Similares

    @EmbeddedId
    @EqualsAndHashCode.Include
    private JuegoRelacionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("juegoOrigenId")
    @JoinColumn(name = "juego_origen_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Juego juegoOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("juegoDestinoId")
    @JoinColumn(name = "juego_destino_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Juego juegoDestino;

    // El 'tipo_relacion' ya forma parte del ID embebido
    // @Enumerated(EnumType.STRING)
    // @Column(name = "tipo_relacion", nullable = false)
    // private GameTypeEnum tipoRelacion;
}