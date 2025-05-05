package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "compania", indexes = {
        @Index(name = "idx_compania_idigdb", columnList = "idigdb")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Compania {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "idigdb", unique = true, nullable = false) // Asumiendo que idigdb es la clave única lógica
    private Long idigdb; // ID de IGDB

    @Column(name = "nombre", length = 255, nullable = false)
    private String nombre;

    @Column(name = "developer", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean developer = false;

    @Column(name = "publisher", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean publisher = false;

    @Column(name = "porting", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean porting = false;

    @Column(name = "supporting", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean supporting = false;

    // Relación inversa con Juego (opcional)
    // @ManyToMany(mappedBy = "companias", fetch = FetchType.LAZY)
    // private Set<Juego> juegos = new HashSet<>();
}