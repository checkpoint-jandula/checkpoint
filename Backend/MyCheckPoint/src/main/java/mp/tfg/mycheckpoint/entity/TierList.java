package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.dto.enums.TierListType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tier_lists") // Nombre de la nueva tabla para TierLists
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    private Long internalId;

    @Column(name = "public_id", unique = true, nullable = false, updatable = false, columnDefinition = "UUID")
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_internal_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TierListType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_game_list_internal_id", nullable = true)
    private GameList sourceGameList; // Null si es PROFILE_GLOBAL

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    @OneToMany(mappedBy = "tierList", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sectionOrder ASC") // Asegura que las secciones se recuperen en orden
    @Builder.Default
    private List<TierSection> sections = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onPrePersist() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
    }

    // Métodos helper para manejar la relación bidireccional con TierSection
    public void addSection(TierSection section) {
        sections.add(section);
        section.setTierList(this);
    }

    public void removeSection(TierSection section) {
        sections.remove(section);
        section.setTierList(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TierList tierList = (TierList) o;
        // Si los IDs internos no son nulos, se usan para la igualdad.
        // Si son nulos (entidades aún no persistidas), se usa publicId si está disponible.
        if (internalId != null && tierList.internalId != null) {
            return Objects.equals(internalId, tierList.internalId);
        }
        if (publicId != null && tierList.publicId != null) {
            return Objects.equals(publicId, tierList.publicId);
        }
        return this == o; // Fallback a igualdad de instancia si no hay identificadores
    }

    @Override
    public int hashCode() {
        // Usa internalId si no es nulo, de lo contrario usa publicId.
        return Objects.hash(internalId == null ? publicId : internalId);
    }
}