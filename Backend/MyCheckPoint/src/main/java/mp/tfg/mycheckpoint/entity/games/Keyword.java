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
@Table(name = "keywords")
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId; // PK de nuestra BDD

    @Column(unique = true, nullable = false)
    private Long igdbId; // El ID que viene del JSON

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "keywords") // 'keywords' es el campo en la entidad Game
    private Set<Game> games = new HashSet<>();

    // Constructor útil
    public Keyword(Long igdbId, String name) {
        this.igdbId = igdbId;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Keyword keyword = (Keyword) o;
        return Objects.equals(igdbId, keyword.igdbId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(igdbId);
    }
}
