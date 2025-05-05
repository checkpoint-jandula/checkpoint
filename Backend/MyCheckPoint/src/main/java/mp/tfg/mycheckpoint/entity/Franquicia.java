package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "franquicia", indexes = {
        @Index(name = "idx_franquicia_idigdb", columnList = "idigdb_franchise_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Franquicia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "idigdb_franchise_id", unique = true, nullable = false)
    private Long idigdbFranchiseId; // ID de IGDB

    @Column(name = "nombre", length = 255, unique = true, nullable = false)
    private String nombre;

    // Relación inversa con Juego (opcional)
    @ManyToMany(mappedBy = "franquicias", fetch = FetchType.LAZY)
    private Set<Juego> juegos = new HashSet<>();

    // --- Métodos Helper ---
    // Helpers para Juego (lado inverso ManyToMany)
    public void addJuego(Juego juego) {
        this.juegos.add(juego);
        juego.getFranquicias().add(this); // <-- Actualiza el otro lado de la relación
    }

    public void removeJuego(Juego juego) {
        this.juegos.remove(juego);
        juego.getFranquicias().remove(this); // <-- Actualiza el otro lado de la relación
    }
}
