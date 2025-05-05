package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SourceType;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ranking", indexes = {
        @Index(name = "idx_ranking_juegoid", columnList = "juego_id"),
        @Index(name = "idx_ranking_puntuacionmedia", columnList = "puntuacionMedia")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "juego")
public class Ranking { // Datos agregados

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ranking")
    @EqualsAndHashCode.Include
    private Long idRanking;

    // Ranking 1 <--> 1 Juego (Dueño de la relación)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "juego_id", referencedColumnName = "id", unique = true, nullable = false)
    private Juego juego;

    @Column(name = "puntuacionMedia") // REAL CHECK (>= 0 AND <= 10)
    private Float puntuacionMedia; // Basado en JuegoUsuario.puntuacion

    @Column(name = "numeroVotos", nullable = false, columnDefinition = "INT DEFAULT 0") // CHECK >= 0
    private Integer numeroVotos = 0;

    @Column(name = "fechaModificacion", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp(source = SourceType.DB) // Actualizado por trigger al recalcular
    private OffsetDateTime fechaModificacion;
}
