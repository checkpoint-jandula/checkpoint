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
public class PlatformLogo {

    @Column(name = "logo_igdb_id")
    private Long igdbId;

    @Column(name = "logo_url", length = 512)
    private String url;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlatformLogo that = (PlatformLogo) o;
        return Objects.equals(igdbId, that.igdbId) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(igdbId, url);
    }
}
