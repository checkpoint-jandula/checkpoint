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
@Table(name = "genres")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId; // PK de nuestra BDD

    @Column(unique = true, nullable = false)
    private Long igdbId; // El ID que viene del JSON

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "genres")
    private Set<Game> games = new HashSet<>();

    // Constructor para facilitar la creación desde el DTO
    public Genre(Long igdbId, String name) {
        this.igdbId = igdbId;
        this.name = name;
    }

    // Es importante implementar equals y hashCode si usas Set y quieres
    // que la lógica de "ya existe" funcione correctamente al buscar por igdbId.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        return Objects.equals(igdbId, genre.igdbId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(igdbId);
    }
}