package mp.tfg.mycheckpoint.entity.junction;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.Juego;
import mp.tfg.mycheckpoint.entity.PlataformaIGDB; // Necesitas crear esta entidad también
import mp.tfg.mycheckpoint.entity.embedded.JuegoPlataformaIGDBId;

import java.time.LocalDate;

@Entity
@Table(name = "juegoplataformaigdb", indexes = {
        @Index(name = "idx_juegoplataformaigdb_plataformaigdbid", columnList = "plataforma_igdb_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"juego", "plataformaIgdb"})
public class JuegoPlataformaIGDB { // Relación Juego <-> Plataforma de IGDB con fecha específica

    @EmbeddedId
    @EqualsAndHashCode.Include
    private JuegoPlataformaIGDBId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("juegoId")
    @JoinColumn(name = "juego_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Juego juego;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("plataformaIgdbId")
    @JoinColumn(name = "plataforma_igdb_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private PlataformaIGDB plataformaIgdb; // Referencia a la entidad PlataformaIGDB

    @Column(name = "fechaLanzamiento") // Fecha específica para esta plataforma
    private LocalDate fechaLanzamiento;
}