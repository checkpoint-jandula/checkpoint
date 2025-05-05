package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.enums.TemaEnum;
import mp.tfg.mycheckpoint.entity.enums.VisibilidadEnum;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode; // Para tipo específico
import org.hibernate.type.SqlTypes; // Para tipo específico
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;

@Entity
@Table(name = "perfilusuario", indexes = {
        // El índice en usuario_id ya está implícito al ser parte de la PK
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario"}) // Evitar recursión
@SQLDelete(sql = "UPDATE perfilusuario SET fechaEliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "fechaEliminacion IS NULL")
public class PerfilUsuario {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    // PerfilUsuario 1 <--> 1 Usuario (Dueño de la relación con la FK)
    @OneToOne(fetch = FetchType.LAZY, optional = false) // Es obligatorio que un perfil tenga usuario
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false)
    @MapsId // <--- **IMPORTANTE:** Esta anotación indica que la PK de esta entidad
    //      se mapea desde la PK de la entidad asociada (`Usuario`).
    //      Implícitamente toma el valor del campo 'id' de esta entidad.
    private Usuario usuario;

    @Enumerated(EnumType.STRING) // Mapear a String ('CLARO', 'OSCURO')
    @Column(name = "tema", nullable = false, columnDefinition = "checkpoint_tema_enum DEFAULT 'CLARO'")
    private TemaEnum tema = TemaEnum.CLARO; // Valor por defecto en Java también

    @Lob // Para campos grandes como BYTEA/BLOB
    @JdbcTypeCode(SqlTypes.VARBINARY) // O SqlTypes.BLOB, depende de la configuración/versión
    @Column(name = "fotoPerfil")
    private byte[] fotoPerfil;

    @Column(name = "notificaciones", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean notificaciones = true; // Usar Boolean objeto para permitir null si no fuera NOT NULL

    @Enumerated(EnumType.STRING)
    @Column(name = "visibilidadPerfil", nullable = false, columnDefinition = "checkpoint_visibilidad_enum DEFAULT 'PUBLICO'")
    private VisibilidadEnum visibilidadPerfil = VisibilidadEnum.PUBLICO;

    @Column(name = "fechaCreacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fechaModificacion", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaModificacion;

    @Column(name = "fechaEliminacion", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime fechaEliminacion;
}