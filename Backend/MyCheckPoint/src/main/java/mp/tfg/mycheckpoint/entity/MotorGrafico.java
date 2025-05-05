package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "motorgrafico", indexes = {
        @Index(name = "idx_motorgrafico_idigdb", columnList = "idigdb")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MotorGrafico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "idigdb", unique = true, nullable = false)
    private Long idigdb; // ID de IGDB

    @Column(name = "nombre", length = 255, unique = true, nullable = false)
    private String nombre;

    // Relación inversa con Juego (opcional)
    // @ManyToMany(mappedBy = "motores", fetch = FetchType.LAZY)
    // private Set<Juego> juegos = new HashSet<>();
}
