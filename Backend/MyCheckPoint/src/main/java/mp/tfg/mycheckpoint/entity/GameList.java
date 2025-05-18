package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "game_lists")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    private Long internalId;

    // Identificador público si quieres que las listas se puedan compartir/acceder por URL amigable
    @Column(name = "public_id", unique = true, nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_internal_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false; // Por defecto, las listas son privadas

    // La relación clave: Una lista contiene múltiples entradas de la biblioteca del usuario.
    // Usamos UserGame porque contiene la instancia específica del juego PARA ESE USUARIO.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "game_list_user_games",
            joinColumns = @JoinColumn(name = "game_list_internal_id"),
            inverseJoinColumns = @JoinColumn(name = "user_game_internal_id")
    )
    @Builder.Default // Asegura que la colección se inicialice si se usa el builder
    private Set<UserGame> userGames = new HashSet<>();

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameList gameList = (GameList) o;
        // Si internalId es null (entidad nueva, aún no persistida), no puede ser igual a otra
        // a menos que ambas sean la misma instancia en memoria.
        // Para entidades persistidas, internalId es la clave.
        if (internalId == null) {
            return publicId != null && Objects.equals(publicId, gameList.publicId); // Comparar por publicId si internalId es null
        }
        return Objects.equals(internalId, gameList.internalId);
    }

    @Override
    public int hashCode() {
        // Si internalId es null, usa publicId.
        return Objects.hash(internalId == null ? publicId : internalId);
    }
}
