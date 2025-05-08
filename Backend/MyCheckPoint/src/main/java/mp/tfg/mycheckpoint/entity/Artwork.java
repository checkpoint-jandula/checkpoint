package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "artwork") // Nombre tabla BBDD
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Llave foránea a Juego (Relación ManyToOne desde Artwork a Game)
    @ManyToOne(fetch = FetchType.LAZY) // Un artwork pertenece a un juego
    @JoinColumn(name = "juego_id", nullable = false) // FK column in artwork table
    @ToString.Exclude // Evitar recursión en logs/debug
    @EqualsAndHashCode.Exclude
    private Game game;

    @Column(name = "id_igdb_image_id", unique = true, length = 255)
    private String idigdbImageId;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;
}