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
@Table(name = "themes")
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    @Column(unique = true, nullable = false)
    private Long igdbId;

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "themes")
    private Set<Game> games = new HashSet<>();

    public Theme(Long igdbId, String name) {
        this.igdbId = igdbId;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Theme theme = (Theme) o;
        return Objects.equals(igdbId, theme.igdbId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(igdbId);
    }
}
