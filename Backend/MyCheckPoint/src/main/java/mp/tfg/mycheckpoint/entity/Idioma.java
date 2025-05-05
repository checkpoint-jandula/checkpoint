package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.junction.JuegoIdiomaSoporte;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "idioma", indexes = {
        @Index(name = "idx_idioma_idigdb", columnList = "idigdb_language_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "juegosSoporte")
public class Idioma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "idigdb_language_id", unique = true) // Puede ser null si no mapeado
    private Long idigdbLanguageId;

    @Column(name = "nombre", length = 100, unique = true, nullable = false)
    private String nombre; // "Español", "Inglés"

    @Column(name = "locale", length = 10, unique = true)
    private String locale; // "es", "en_US"

    // Relación inversa con la tabla de unión JuegoIdiomaSoporte (opcional)
    @OneToMany(mappedBy = "idioma", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<JuegoIdiomaSoporte> juegosSoporte = new HashSet<>();
}