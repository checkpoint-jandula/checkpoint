package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.junction.TierListJuego;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tierlist", indexes = {
        @Index(name = "idx_tierlist_usuarioid", columnList = "usuario_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario", "nivelesTier", "tierListJuegos"})
@SQLDelete(sql = "UPDATE tierlist SET fechaEliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "fechaEliminacion IS NULL")
public class TierList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "nombre", length = 255, nullable = false)
    private String nombre;

    // TierList N <--> 1 Usuario
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fechaCreacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fechaModificacion", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaModificacion;

    @Column(name = "fechaEliminacion", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime fechaEliminacion;

    // TierList 1 <--> * NivelTier
    @OneToMany(mappedBy = "tierList", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("orden ASC") // Ordenar los niveles por su campo 'orden'
    private List<NivelTier> nivelesTier = new ArrayList<>(); // Usamos List para mantener el orden

    // TierList 1 <--> * TierListJuego (Relación con la tabla de unión)
    @OneToMany(mappedBy = "tierList", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TierListJuego> tierListJuegos = new HashSet<>();

    // Métodos Helper (Ejemplo para niveles)
    public void addNivelTier(NivelTier nivelTier) {
        this.nivelesTier.add(nivelTier);
        nivelTier.setTierList(this);
    }

    public void removeNivelTier(NivelTier nivelTier) {
        this.nivelesTier.remove(nivelTier);
        nivelTier.setTierList(null);
    }
    // Helpers para TierListJuego si son necesarios
}