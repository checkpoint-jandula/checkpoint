package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.junction.ListaJuego;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lista", indexes = {
        @Index(name = "idx_lista_usuarioid", columnList = "usuario_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario", "listaJuegos"})
@SQLDelete(sql = "UPDATE lista SET fechaEliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "fechaEliminacion IS NULL")
public class Lista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "nombre", length = 255, nullable = false)
    private String nombre;

    // Lista N <--> 1 Usuario
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fechaCreacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fechaModificacion", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaModificacion;

    @Column(name = "fechaEliminacion", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime fechaEliminacion;

    // Relación con la tabla de unión ListaJuego
    // Lista 1 <--> * ListaJuego
    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ListaJuego> listaJuegos = new HashSet<>();

    // Método Helper (opcional)
    public void addListaJuego(ListaJuego listaJuego) {
        this.listaJuegos.add(listaJuego);
        listaJuego.setLista(this);
    }

    public void removeListaJuego(ListaJuego listaJuego) {
        this.listaJuegos.remove(listaJuego);
        listaJuego.setLista(null);
    }
}