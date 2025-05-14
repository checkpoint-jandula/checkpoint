package mp.tfg.mycheckpoint.entity.games;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Objects; // Para equals y hashCode

@Embeddable // Indica que esta clase puede ser embebida en otras entidades
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Artwork {

    @Column(name = "artwork_igdb_id") // Es buena idea prefijar para evitar colisiones de nombres en la tabla
    private Long igdbId;

    @Column(name = "artwork_url", length = 512) // Ajusta la longitud si es necesario
    private String url;

    // Implementar equals y hashCode es importante para colecciones de Embeddables,
    // especialmente si JPA necesita comparar elementos.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artwork artwork = (Artwork) o;
        return Objects.equals(igdbId, artwork.igdbId) &&
                Objects.equals(url, artwork.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(igdbId, url);
    }
}