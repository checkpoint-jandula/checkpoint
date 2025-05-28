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
public class Website {

    @Column(name = "website_igdb_id")
    private Long igdbId;

    @Column(name = "website_url", length = 1024) // Las URLs pueden ser largas
    private String url;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Website website = (Website) o;
        return Objects.equals(igdbId, website.igdbId) && Objects.equals(url, website.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(igdbId, url);
    }
}
