package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "video") // Nombre tabla BBDD
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juego_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Game game;

    @Column(name = "id_igdb_video_id", unique = true, length = 255)
    private String idigdbVideoId;

    @Column(name = "nombre", length = 255)
    private String nombre;

    @Column(name = "video_id", nullable = false, length = 255) // Youtube ID, etc.
    private String videoId;
}