package mp.tfg.mycheckpoint.entity.games;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "game_engines")
public class GameEngine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId; // PK de nuestra BDD

    @Column(unique = true, nullable = false)
    private Long igdbId; // El ID que viene del JSON

    @Column(nullable = false) // Asumimos que el nombre no puede ser nulo
    private String name;

    @ManyToMany(mappedBy = "gameEngines") // 'gameEngines' es el campo en la entidad Game
    private Set<Game> games = new HashSet<>();

    // Constructor útil
    public GameEngine(Long igdbId, String name) {
        this.igdbId = igdbId;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameEngine that = (GameEngine) o;
        return Objects.equals(igdbId, that.igdbId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(igdbId);
    }
}