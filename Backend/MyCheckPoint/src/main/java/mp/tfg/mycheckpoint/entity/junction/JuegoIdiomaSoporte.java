package mp.tfg.mycheckpoint.entity.junction;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.Idioma;
import mp.tfg.mycheckpoint.entity.Juego;
import mp.tfg.mycheckpoint.entity.embedded.JuegoIdiomaSoporteId;

@Entity
@Table(name = "juegoidiomasoporte", indexes = {
        @Index(name = "idx_juegoidiomasoporte_idiomaid", columnList = "idioma_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"juego", "idioma"})
public class JuegoIdiomaSoporte {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private JuegoIdiomaSoporteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("juegoId")
    @JoinColumn(name = "juego_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Juego juego;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idiomaId")
    @JoinColumn(name = "idioma_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Idioma idioma;

    // El 'tipoSoporte' ya está incluido como parte del @EmbeddedId
    // @Column(name = "tipoSoporte", nullable = false)
    // private Integer tipoSoporte; // 1=Interfaz, 2=Audio, 3=Subtítulos
}