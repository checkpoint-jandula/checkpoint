package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "web") // Nombre tabla BBDD
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Web {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juego_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Game game;

    @Column(name = "id_igdb_website_id", unique = true)
    private Long idigdbWebsiteId;

    @Column(name = "categoria")
    private Integer categoria; // Tipo de web según IGDB

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "trusted")
    private Boolean trusted = false;
}