package mp.tfg.mycheckpoint.entity.junction;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.Juego;
import mp.tfg.mycheckpoint.entity.Lista;
import mp.tfg.mycheckpoint.entity.embedded.ListaJuegoId;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
// import org.hibernate.annotations.UpdateTimestamp; // Si se añade fechaModificacion
// import org.hibernate.annotations.SQLDelete; // Si se necesita soft delete
// import org.hibernate.annotations.Where; // Si se necesita soft delete

import java.time.OffsetDateTime;

@Entity
@Table(name = "listajuego", indexes = {
        @Index(name = "idx_listajuego_juegoid", columnList = "juego_id")
        // PK compuesta (lista_id, juego_id) ya indexada
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"lista", "juego"})
// @SQLDelete(sql = "UPDATE listajuego SET fechaEliminacion = CURRENT_TIMESTAMP WHERE lista_id = ? AND juego_id = ?")
// @Where(clause = "fechaEliminacion IS NULL")
public class ListaJuego {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private ListaJuegoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("listaId") // Mapea la parte 'listaId' del EmbeddedId
    @JoinColumn(name = "lista_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Lista lista; // FK a Lista

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("juegoId") // Mapea la parte 'juegoId' del EmbeddedId
    @JoinColumn(name = "juego_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Juego juego; // FK a Juego

    @Column(name = "fechaCreacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaCreacion;

    // Descomentar si se añaden estas columnas a la tabla y se necesitan
    // @Column(name = "fechaModificacion", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    // @UpdateTimestamp(source = SourceType.DB) // Asumiendo trigger
    // private OffsetDateTime fechaModificacion;

    // @Column(name = "fechaEliminacion", columnDefinition = "TIMESTAMPTZ")
    // private OffsetDateTime fechaEliminacion;
}