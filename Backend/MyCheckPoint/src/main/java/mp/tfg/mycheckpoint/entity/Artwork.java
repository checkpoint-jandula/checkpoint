package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "artwork", indexes = {
        @Index(name = "idx_artwork_juegoid", columnList = "juego_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "juego")
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Artwork N <--> 1 Juego
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;

    @Column(name = "idigdb_image_id", length = 255, unique = true) // Puede ser null si no viene de IGDB
    private String idigdbImageId; // e.g., 'co28d4'

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;
}
