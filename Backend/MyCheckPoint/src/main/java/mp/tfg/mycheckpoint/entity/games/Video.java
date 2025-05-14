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
public class Video {

    @Column(name = "video_igdb_id")
    private Long igdbId;

    @Column(name = "video_name")
    private String name;

    @Column(name = "video_platform_id") // Para almacenar el "video_id" del JSON (ej. ID de YouTube)
    private String videoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return Objects.equals(igdbId, video.igdbId) &&
                Objects.equals(name, video.name) &&
                Objects.equals(videoId, video.videoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(igdbId, name, videoId);
    }
}
