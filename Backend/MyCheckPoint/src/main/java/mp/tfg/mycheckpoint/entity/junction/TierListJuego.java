package mp.tfg.mycheckpoint.entity.junction;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.Juego;
import mp.tfg.mycheckpoint.entity.NivelTier;
import mp.tfg.mycheckpoint.entity.TierList;
import mp.tfg.mycheckpoint.entity.embedded.TierListJuegoId;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
// La PK real de la tabla es (tierlist_id, juego_id, nivelTier_id) según el DDL implícito o explícito de FKs,
// pero tiene un UNIQUE (tierlist_id, juego_id). Usamos el @EmbeddedId que creamos para esta clave única lógica.
// La anotación @Table refleja el constraint UNIQUE de la BD.
@Table(name = "tierlistjuego",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"tierlist_id", "juego_id"}) },
        indexes = {
                @Index(name = "idx_tierlistjuego_juegoid", columnList = "juego_id"),
                @Index(name = "idx_tierlistjuego_niveltierid", columnList = "nivelTier_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"tierList", "juego", "nivelTier"})
public class TierListJuego {

    @EmbeddedId // ID lógico basado en (tierlist_id, juego_id) por el UNIQUE constraint
    @EqualsAndHashCode.Include
    private TierListJuegoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tierListId") // Mapea la parte 'tierListId' del EmbeddedId
    @JoinColumn(name = "tierlist_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private TierList tierList; // FK a TierList

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("juegoId") // Mapea la parte 'juegoId' del EmbeddedId
    @JoinColumn(name = "juego_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Juego juego; // FK a Juego

    // El nivel es un atributo importante de la relación, no parte del ID lógico principal
    // pero sí de la PK física implícita. Es obligatorio que un juego en la lista tenga nivel.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nivelTier_id", referencedColumnName = "id", nullable = false) // FK a NivelTier
    private NivelTier nivelTier;

    @Column(name = "fechaCreacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fechaModificacion", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp(source = SourceType.DB) // El trigger en BD se encarga de esto
    private OffsetDateTime fechaModificacion;

    // fechaEliminacion si se necesita
}