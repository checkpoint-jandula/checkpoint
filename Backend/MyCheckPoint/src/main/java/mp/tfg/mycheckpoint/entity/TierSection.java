package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tier_sections")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    private Long internalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_list_internal_id", nullable = false)
    private TierList tierList;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "section_order", nullable = false)
    private int sectionOrder; // 0 para "Juegos por Clasificar", 1+ para personalizables

    @Column(name = "is_default_unclassified", nullable = false)
    private boolean isDefaultUnclassified = false;

    @OneToMany(mappedBy = "tierSection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("itemOrder ASC")
    @Builder.Default
    @Fetch(FetchMode.SUBSELECT) // <--- AÑADE ESTA LÍNEA
    private List<TierListItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Métodos helper
    public void addItem(TierListItem item) {
        items.add(item);
        item.setTierSection(this);
    }

    public void removeItem(TierListItem item) {
        items.remove(item);
        item.setTierSection(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TierSection that = (TierSection) o;
        return Objects.equals(internalId, that.internalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(internalId);
    }
}