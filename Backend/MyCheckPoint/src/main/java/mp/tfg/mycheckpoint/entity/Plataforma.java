package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.junction.PlataformaUsuario;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "plataforma", indexes = {
        @Index(name = "idx_plataforma_idigdb", columnList = "idigdb"),
        @Index(name = "idx_plataforma_nombre", columnList = "nombre")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"plataformasUsuario", "juegosUsuario"}) // Evitar recursión
@SQLDelete(sql = "UPDATE plataforma SET fechaEliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "fechaEliminacion IS NULL")
public class Plataforma { // Representa NUESTRA entidad Plataforma (Steam, PS5, etc.)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "idigdb", unique = true, nullable = false) // ID externo (¿de IGDB?) ¿O ID interno nuestro? Basado en SQL, parece ID externo
    private Integer idigdb; // O Long si puede ser grande

    @Column(name = "nombre", length = 255, nullable = false)
    private String nombre;

    @Column(name = "nombreAlternativo", length = 255)
    private String nombreAlternativo;

    @Column(name = "logoUrl", columnDefinition = "TEXT")
    private String logoUrl;

    @Column(name = "fechaCreacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fechaModificacion", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaModificacion;

    @Column(name = "fechaEliminacion", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime fechaEliminacion;

    // --- Relaciones ---

    // Plataforma 1 <--> * PlataformaUsuario (Tabla de unión)
    @OneToMany(mappedBy = "plataforma", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<PlataformaUsuario> plataformasUsuario = new HashSet<>();

    // Plataforma 1 <--> * JuegoUsuario (Un juego de usuario puede tener una plataforma)
    // Esto representa la plataforma en la que el usuario JUEGA o POSEE el juego
    @OneToMany(mappedBy = "plataforma", fetch = FetchType.LAZY) // No cascade all, no borrar juegos de usuario si se borra plataforma
    private Set<JuegoUsuario> juegosUsuario = new HashSet<>();

}