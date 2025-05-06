package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.enums.TemaEnum;
import mp.tfg.mycheckpoint.entity.enums.VisibilidadEnum;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;

@Entity
@Table(name = "perfilusuario", indexes = {
        // El índice en usuario_id ya está implícito al ser PK
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario"})
@SQLDelete(sql = "UPDATE perfilusuario SET fechaEliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "fechaEliminacion IS NULL")
public class PerfilUsuario {

    // El ID de PerfilUsuario es el mismo que el ID de Usuario
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id; // Mantiene el valor del ID del Usuario

    // PerfilUsuario 1 <--> 1 Usuario (Dueño de la relación con la FK que es también PK)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false,unique = true) // Esta es la FK en la tabla
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tema", nullable = false, columnDefinition = "checkpoint_tema_enum DEFAULT 'CLARO'")
    private TemaEnum tema = TemaEnum.CLARO;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "fotoPerfil")
    private byte[] fotoPerfil;

    @Column(name = "notificaciones", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean notificaciones = true;

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