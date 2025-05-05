package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "video", indexes = {
        @Index(name = "idx_video_juegoid", columnList = "juego_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "juego")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Video N <--> 1 Juego
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;

    @Column(name = "idigdb_video_id", length = 255, unique = true) // Puede ser null
    private String idigdbVideoId;

    @Column(name = "nombre", length = 255)
    private String nombre;

    @Column(name = "videoId", length = 255, nullable = false) // ID en la plataforma (Youtube, etc.)
    private String videoId;
}
