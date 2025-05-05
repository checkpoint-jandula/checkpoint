package mp.tfg.mycheckpoint.entity.junction;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.Plataforma; // Nuestra entidad Plataforma
import mp.tfg.mycheckpoint.entity.Usuario;
import mp.tfg.mycheckpoint.entity.embedded.PlataformaUsuarioId;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import java.time.OffsetDateTime;

@Entity
@Table(name = "plataformausuario", indexes = {
        @Index(name = "idx_plataformausuario_plataformaid", columnList = "plataforma_id")
        // La PK compuesta ya indexa (usuario_id, plataforma_id)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario", "plataforma"})
public class PlataformaUsuario { // Plataformas que posee/usa el usuario

    @EmbeddedId
    @EqualsAndHashCode.Include
    private PlataformaUsuarioId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId") // Mapea a la parte 'usuarioId' del EmbeddedId
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Usuario usuario; // FK a Usuario

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("plataformaId") // Mapea a la parte 'plataformaId' del EmbeddedId
    @JoinColumn(name = "plataforma_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private Plataforma plataforma; // FK a nuestra entidad Plataforma

    @Column(name = "fechaCreacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaCreacion;

    // El campo 'juegosUsuario_id' del UML fue correctamente omitido en el SQL y aquí.
}