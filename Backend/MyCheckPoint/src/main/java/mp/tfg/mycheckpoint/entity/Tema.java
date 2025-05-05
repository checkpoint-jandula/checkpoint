package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tema", indexes = { // Tema de IGDB (Acción, Fantasía...)
        @Index(name = "idx_tema_idigdb", columnList = "idigdb_theme_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "idigdb_theme_id", unique = true, nullable = false)
    private Long idigdbThemeId; // ID de IGDB

    @Column(name = "nombre", length = 255, unique = true, nullable = false)
    private String nombre;

    // Relación inversa con Juego (opcional)
    @ManyToMany(mappedBy = "temas", fetch = FetchType.LAZY)
    private Set<Juego> juegos = new HashSet<>();

    // --- Métodos Helper ---
    // Helpers para Juego (lado inverso ManyToMany)
    public void addJuego(Juego juego) {
        this.juegos.add(juego);
        juego.getTemas().add(this); // <-- Actualiza el otro lado de la relación
    }

    public void removeJuego(Juego juego) {
        this.juegos.remove(juego);
        juego.getTemas().remove(this); // <-- Actualiza el otro lado de la relación
    }
}
