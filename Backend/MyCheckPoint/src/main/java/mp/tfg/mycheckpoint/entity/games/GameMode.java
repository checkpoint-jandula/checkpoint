package mp.tfg.mycheckpoint.entity.games;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "game_modes")
public class GameMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId; // PK de nuestra BDD

    @Column(unique = true, nullable = false)
    private Long igdbId; // El ID que viene del JSON

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "gameModes")
    private Set<Game> games = new HashSet<>();

    public GameMode(Long igdbId, String name) {
        this.igdbId = igdbId;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameMode gameMode = (GameMode) o;
        return Objects.equals(igdbId, gameMode.igdbId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(igdbId);
    }
}