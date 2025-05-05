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
    @Id // <--- Este campo es ahora la PK
    @EqualsAndHashCode.Include
    // private Long id; // Puedes mantener este campo para representar la PK... PERO...
    // ...cuando usas @MapsId con una FK a otra entidad, el campo @Id
    // a menudo se omite y el valor del ID se obtiene a través de la relación mapeada por @MapsId.
    // O si la PK es un tipo primitivo (como Long aquí), puedes declararlo,
    // pero @MapsId le dice a JPA que su valor proviene de la FK.
    // Vamos a seguir la forma más común con @MapsId, donde el campo @Id existe
    // y MapStruct lo mapeará si es necesario, pero su valor viene de la FK.
    private Long id; // Mantiene el valor del ID del Usuario

    // PerfilUsuario 1 <--> 1 Usuario (Dueño de la relación con la FK que es también PK)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false) // Esta es la FK en la tabla
    @MapsId // <--- **IMPORTANTE:** Esta anotación hace que la columna "usuario_id" sea la PK de esta entidad
    //      y que el valor de 'id' de esta entidad se obtenga de la PK de la entidad 'usuario'.
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