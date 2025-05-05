package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SourceType;

import java.time.OffsetDateTime;

@Entity
@Table(name = "duracionjuego", indexes = {
        @Index(name = "idx_duracionjuego_juegoid", columnList = "juego_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "juego")
public class DuracionJuego { // Datos agregados

    // Usamos el nombre de columna del SQL para el ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_duracion")
    @EqualsAndHashCode.Include
    private Long idDuracion;

    // DuracionJuego 1 <--> 1 Juego (Dueño de la relación, tiene la FK)
    // 'optional = false' porque una entrada de duración siempre pertenece a un juego
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "juego_id", referencedColumnName = "id", unique = true, nullable = false)
    private Juego juego;

    @Column(name = "mediaHistoria") // REAL CHECK >= 0
    private Float mediaHistoria;

    @Column(name = "mediaHistoriaSecundarias") // REAL CHECK >= 0
    private Float mediaHistoriaSecundarias;

    @Column(name = "mediaCompletista") // REAL CHECK >= 0
    private Float mediaCompletista;

    @Column(name = "numeroUsuarios", nullable = false, columnDefinition = "INT DEFAULT 0") // CHECK >= 0
    private Integer numeroUsuarios = 0;

    @Column(name = "fechaModificacion", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp(source = SourceType.DB) // Actualizado por trigger al recalcular
    private OffsetDateTime fechaModificacion;
}
