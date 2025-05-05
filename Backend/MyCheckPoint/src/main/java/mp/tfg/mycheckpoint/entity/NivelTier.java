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

    // --- Métodos Helper ---

    // Helpers para TierListJuego (tabla de unión)
    // Un juego puede estar en una tierlist y asignado a un nivel.
    // La relación principal es TierList -> TierListJuego, pero este helper
    // es útil si quieres ver o gestionar los juegos *dentro de este nivel*
    // tierlistJuego.setNivelTier(this) es lo importante aquí.
    public void addTierListJuego(TierListJuego tierListJuego) {
        this.tierListJuegos.add(tierListJuego);
        tierListJuego.setNivelTier(this);
        // Es importante que al crear TierListJuego, la tierlist, el juego y el ID compuesto estén completos
        // tierListJuego.setId(...)
        // tierListJuego.setTierList(...)
        // tierListJuego.setJuego(...)
    }

    public void removeTierListJuego(TierListJuego tierListJuego) {
        this.tierListJuegos.remove(tierListJuego);
        tierListJuego.setNivelTier(null);
    }
}