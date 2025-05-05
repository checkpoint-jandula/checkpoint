package mp.tfg.mycheckpoint.entity.junction;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.Usuario;
import mp.tfg.mycheckpoint.entity.embedded.AmistadId;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
// import org.hibernate.annotations.SQLDelete; // Para soft delete si se desea
// import org.hibernate.annotations.Where; // Para soft delete si se desea

import java.time.OffsetDateTime;

@Entity
@Table(name = "amistad", indexes = {
        // Índices ya cubiertos por FKs y PK compuesta, pero podemos añadir explícitos si es necesario
        @Index(name = "idx_amistad_usuarioid", columnList = "usuario_id"),
        @Index(name = "idx_amistad_amigoid", columnList = "amigo_id")
        // El índice UNIQUE simétrico está en el SQL, JPA no lo representa directamente
        // CREATE UNIQUE INDEX idx_amistad_symmetric_unique ON Amistad (LEAST(usuario_id, amigo_id), GREATEST(usuario_id, amigo_id));
        // Se puede implementar lógica de servicio para asegurar esto si es necesario.
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Basado en ID
@ToString(exclude = {"usuario", "amigo"}) // Evitar recursión
// @SQLDelete(sql = "UPDATE amistad SET fechaEliminacion = CURRENT_TIMESTAMP WHERE usuario_id = ? AND amigo_id = ?") // Soft delete (ajustar PK)
// @Where(clause = "fechaEliminacion IS NULL")
public class Amistad {

    @EmbeddedId // Usa la clase Embeddable como ID compuesto
    @EqualsAndHashCode.Include
    private AmistadId id;

    // Mapeo de las partes del ID compuesto a las entidades relacionadas
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId") // Mapea la parte 'usuarioId' del EmbeddedId
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Usuario usuario; // Usuario que inicia/posee la relación (FK a Usuario)

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("amigoId") // Mapea la parte 'amigoId' del EmbeddedId
    @JoinColumn(name = "amigo_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Usuario amigo; // Usuario que es el amigo (FK a Usuario)

    @Column(name = "fechaCreacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fechaModificacion", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp(source = SourceType.DB) // Trigger de BD lo maneja
    private OffsetDateTime fechaModificacion;

    // Descomentar si se añade fechaEliminacion en la BD y se quiere mapear
    // @Column(name = "fechaEliminacion", columnDefinition = "TIMESTAMPTZ")
    // private OffsetDateTime fechaEliminacion;

    // Podría añadirse un estado ('pendiente', 'aceptada') aquí si hubiera solicitudes
    // @Column(name = "estado", length = 20, nullable = false)
    // private String estado = "pendiente";
}