package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.enums.EstadoEnum;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "juegousuario",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"usuario_id", "juego_id"}) }, // Constraint de BD
        indexes = {
                @Index(name = "idx_juegousuario_usuarioid", columnList = "usuario_id"),
                @Index(name = "idx_juegousuario_juegoid", columnList = "juego_id"),
                @Index(name = "idx_juegousuario_plataformaid", columnList = "plataforma_id"),
                @Index(name = "idx_juegousuario_estado", columnList = "estado"),
                @Index(name = "idx_juegousuario_puntuacion", columnList = "puntuacion")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario", "juego", "plataforma"}) // Evitar recursión
@SQLDelete(sql = "UPDATE juegousuario SET fechaEliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "fechaEliminacion IS NULL")
public class JuegoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // JuegoUsuario N <--> 1 Usuario
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // JuegoUsuario N <--> 1 Juego
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;

    // JuegoUsuario N <--> 1 Plataforma (Nuestra Plataforma, donde lo juega/posee)
    @ManyToOne(fetch = FetchType.LAZY) // Puede ser null
    @JoinColumn(name = "plataforma_id", referencedColumnName = "id") // FK a nuestra tabla Plataforma
    private Plataforma plataforma;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "checkpoint_estado_juego_usuario_enum DEFAULT 'DESEADO'")
    private EstadoEnum estado = EstadoEnum.DESEADO;

    @Column(name = "posesion", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean posesion = false;

    @Column(name = "puntuacion") // REAL CHECK (puntuacion >= 0 AND puntuacion <= 10)
    private Float puntuacion; // Puntuación del usuario (0-10)

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "comentarioPrivado", columnDefinition = "TEXT")
    private String comentarioPrivado;

    @Column(name = "fechaInicio")
    private LocalDate fechaInicio;

    @Column(name = "fechaFin")
    private LocalDate fechaFin;

    @Column(name = "duracionHistoria") // REAL CHECK (duracionHistoria >= 0)
    private Float duracionHistoria; // Horas

    @Column(name = "duracionHistoriaSecundarias") // REAL CHECK >= 0
    private Float duracionHistoriaSecundarias;

    @Column(name = "duracionCompletista") // REAL CHECK >= 0
    private Float duracionCompletista;

    @Column(name = "importado", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean importado = false;

    @Column(name = "fechaCreacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fechaModificacion", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp(source = SourceType.DB)
    private OffsetDateTime fechaModificacion;

    @Column(name = "fechaEliminacion", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime fechaEliminacion;
}