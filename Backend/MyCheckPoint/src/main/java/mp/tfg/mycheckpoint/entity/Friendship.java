package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.FriendshipStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "friendships", uniqueConstraints = {
        // Asegura que no haya múltiples solicitudes/amistades activas entre los mismos dos usuarios en la misma dirección
        @UniqueConstraint(columnNames = {"requester_user_internal_id", "receiver_user_internal_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    private Long internalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_internal_id", nullable = false)
    private User requester; // Usuario que envía la solicitud

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_internal_id", nullable = false)
    private User receiver; // Usuario que recibe la solicitud

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FriendshipStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // equals y hashCode para asegurar la unicidad de la relación en colecciones si es necesario,
    // basado en los participantes. La constraint de BBDD ya lo maneja a nivel de persistencia.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        return Objects.equals(requester != null ? requester.getId() : null, that.requester != null ? that.requester.getId() : null) &&
                Objects.equals(receiver != null ? receiver.getId() : null, that.receiver != null ? that.receiver.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                requester != null ? requester.getId() : null,
                receiver != null ? receiver.getId() : null
        );
    }
}
