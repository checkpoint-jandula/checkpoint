package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.junction.JuegoPlataformaIGDB;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "plataformaigdb", indexes = { // Plataformas tal como vienen de IGDB
        @Index(name = "idx_plataformaigdb_idigdb", columnList = "idigdb_platform_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "juegosPlataforma")
public class PlataformaIGDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "idigdb_platform_id", unique = true, nullable = false)
    private Long idigdbPlatformId; // ID de IGDB

    @Column(name = "nombre", length = 255, nullable = false)
    private String nombre;

    @Column(name = "abreviatura", length = 50)
    private String abreviatura;

    @Column(name = "logoUrl", columnDefinition = "TEXT")
    private String logoUrl;

    // Relación inversa con la tabla de unión JuegoPlataformaIGDB (opcional)
    @OneToMany(mappedBy = "plataformaIgdb", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<JuegoPlataformaIGDB> juegosPlataforma = new HashSet<>();
}
