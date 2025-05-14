package mp.tfg.mycheckpoint.entity.games;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Screenshot {

    @Column(name = "screenshot_igdb_id")
    private Long igdbId;

    @Column(name = "screenshot_url", length = 512) // Ajusta la longitud si es necesario
    private String url;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Screenshot that = (Screenshot) o;
        return Objects.equals(igdbId, that.igdbId) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(igdbId, url);
    }
}
