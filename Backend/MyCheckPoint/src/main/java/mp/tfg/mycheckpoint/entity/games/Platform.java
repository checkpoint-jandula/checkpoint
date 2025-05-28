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
@Table(name = "platforms")
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    @Column(unique = true, nullable = false)
    private Long igdbId;

    @Column(name = "alternative_name")
    private String alternativeName;

    @Column(nullable = false)
    private String name;

    @Embedded // Embeber el objeto PlatformLogo
    private PlatformLogo platformLogo;

    @ManyToMany(mappedBy = "platforms")
    private Set<Game> games = new HashSet<>();

    public Platform(Long igdbId, String name, String alternativeName, PlatformLogo platformLogo) {
        this.igdbId = igdbId;
        this.name = name;
        this.alternativeName = alternativeName;
        this.platformLogo = platformLogo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Platform platform = (Platform) o;
        return Objects.equals(igdbId, platform.igdbId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(igdbId);
    }
}
