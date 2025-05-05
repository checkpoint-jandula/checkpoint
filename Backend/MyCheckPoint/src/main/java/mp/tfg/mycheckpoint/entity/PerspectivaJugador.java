package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "perspectivajugador", indexes = {
        @Index(name = "idx_perspectivajugador_idigdb", columnList = "idigdb")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PerspectivaJugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "idigdb", unique = true, nullable = false)
    private Long idigdb; // ID de IGDB

    @Column(name = "nombre", length = 255, unique = true, nullable = false)
    private String nombre;

    // Relación inversa con Juego (opcional)
    @ManyToMany(mappedBy = "perspectivas", fetch = FetchType.LAZY)
    private Set<Juego> juegos = new HashSet<>();

    // --- Métodos Helper ---
    // Helpers para Juego (lado inverso ManyToMany)
    public void addJuego(Juego juego) {
        this.juegos.add(juego);
        juego.getPerspectivas().add(this); // <-- Actualiza el otro lado de la relación
    }

    public void removeJuego(Juego juego) {
        this.juegos.remove(juego);
        juego.getPerspectivas().remove(this); // <-- Actualiza el otro lado de la relación
    }
}
