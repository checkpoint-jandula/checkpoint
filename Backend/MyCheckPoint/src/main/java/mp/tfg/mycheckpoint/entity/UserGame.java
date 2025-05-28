package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.UserGamePersonalPlatform;
import mp.tfg.mycheckpoint.dto.enums.UserGameStatus;
import mp.tfg.mycheckpoint.entity.games.Game;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_games", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_internal_id", "game_internal_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    private Long internalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_internal_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_internal_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserGameStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "personal_platform")
    private UserGamePersonalPlatform personalPlatform;

    @Column(name = "has_possession")
    private Boolean hasPossession; // Posesión

    @Column(name = "score")
    private Float score; // Puntuación (ej. 0.0 a 10.0 o 0-5)

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "private_comment", columnDefinition = "TEXT")
    private String privateComment;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "story_duration_hours")
    private Float storyDurationHours; // Duración de la historia en horas

    @Column(name = "story_secondary_duration_hours")
    private Float storySecondaryDurationHours; // Duración historia + secundarias en horas

    @Column(name = "completionist_duration_hours")
    private Float completionistDurationHours; // Duración para completarlo al 100% en horas

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt; // Para borrado suave si es necesario en el futuro

    // equals y hashCode basados en user y game para la lógica de negocio,
    // aunque internalId es el PK.
    // Para colecciones en memoria y lógica de si ya existe la entrada para un usuario y juego.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGame userGame = (UserGame) o;
        // Consideramos que una entrada es "la misma" si es para el mismo usuario y juego.
        // No usar internalId aquí si estamos comprobando antes de persistir.
        return Objects.equals(user != null ? user.getId() : null, userGame.user != null ? userGame.user.getId() : null) &&
                Objects.equals(game != null ? game.getInternalId() : null, userGame.game != null ? userGame.game.getInternalId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user != null ? user.getId() : null, game != null ? game.getInternalId() : null);
    }
}
