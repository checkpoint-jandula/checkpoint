package mp.tfg.mycheckpoint.entity.games;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Embeddable // Indica que esta clase será parte de otra entidad
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cover {

    @Column(name = "cover_igdb_id") // Para evitar colisión con Game.igdbId en la tabla game
    private Long igdbId;

    @Column(name = "cover_url")
    private String url;
}