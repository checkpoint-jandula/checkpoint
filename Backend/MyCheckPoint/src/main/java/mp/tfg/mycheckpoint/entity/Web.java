package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "web", indexes = {
        @Index(name = "idx_web_juegoid", columnList = "juego_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "juego")
public class Web {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Web N <--> 1 Juego
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;

    @Column(name = "idigdb_website_id", unique = true) // Puede ser null
    private Long idigdbWebsiteId;

    @Column(name = "categoria") // Mapear categoría de IGDB (official=1, wikia=2, wikipedia=3, etc.)
    private Integer categoria;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "trusted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean trusted = false;
}
