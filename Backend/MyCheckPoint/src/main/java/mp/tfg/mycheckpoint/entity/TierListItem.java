package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "tier_list_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tier_section_internal_id", "user_game_internal_id"}, name = "uk_tierlistitem_section_usergame")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    private Long internalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_section_internal_id", nullable = false)
    private TierSection tierSection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_game_internal_id", nullable = false)
    private UserGame userGame;

    @Column(name = "item_order", nullable = false)
    private int itemOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TierListItem that = (TierListItem) o;
        return Objects.equals(internalId, that.internalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(internalId);
    }
}