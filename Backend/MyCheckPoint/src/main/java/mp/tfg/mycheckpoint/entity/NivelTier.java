package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.junction.TierListJuego;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "niveltier",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tierlist_id", "nombre"}),
                @UniqueConstraint(columnNames = {"tierlist_id", "orden"})
        },
        indexes = {
                @Index(name = "idx_niveltier_tierlistid", columnList = "tierlist_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"tierList", "tierListJuegos"})
// No se suele necesitar soft delete para niveles individuales, se borran con la TierList
public class NivelTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // NivelTier N <--> 1 TierList
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tierlist_id", nullable = false)
    private TierList tierList;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "orden", nullable = false) // CHECK (orden >= 0)
    private Integer orden; // 0 es el superior

    @Column(name = "color", length = 7) // #RRGGBB
    private String color;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    // NivelTier 1 <--> * TierListJuego (Juegos asignados a este nivel)
    @OneToMany(mappedBy = "nivelTier", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TierListJuego> tierListJuegos = new HashSet<>();
}