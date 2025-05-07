package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.TemaEnum;
import mp.tfg.mycheckpoint.dto.enums.VisibilidadEnum;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
// import org.hibernate.annotations.UuidGenerator; // No es necesario si la BBDD lo genera y @Column(name="...") está bien

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuario") // Tu BBDD.txt ahora usa 'Usuario' en mayúscula, verifica esto. Si es 'usuario', cambia aquí.
// Basado en tu BBDD.txt reciente, los nombres de tabla son en CamelCase: Usuario, Plataforma, Juego.
// Los nombres de columna son en snake_case.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // En BBDD: id (PK)

    @Column(name = "public_id", unique = true, nullable = false, updatable = false, columnDefinition = "UUID")
    @UuidGenerator(style = UuidGenerator.Style.RANDOM) // RANDOM es el default, otras opciones: TIME, AUTO
    private UUID publicId; // Campo Java: publicId, Columna BBDD: public_id

    @Column(name = "nombre_usuario", unique = true, nullable = false, length = 100)
    private String nombreUsuario; // Campo Java: nombreUsuario, Columna BBDD: nombre_usuario

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email; // Campo Java: email, Columna BBDD: email

    @Column(name = "contraseña", nullable = false, length = 255)
    private String contraseña; // Campo Java: contraseña, Columna BBDD: contraseña

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime fechaRegistro; // Campo Java: fechaRegistro, Columna BBDD: fecha_registro

    @Enumerated(EnumType.STRING)
    @Column(name = "tema", nullable = false, columnDefinition = "checkpoint_tema_enum DEFAULT 'CLARO'")
    private TemaEnum tema = TemaEnum.CLARO; // Campo Java: tema, Columna BBDD: tema

    @Column(name = "foto_perfil", columnDefinition = "TEXT")
    private String fotoPerfil; // Campo Java: fotoPerfil, Columna BBDD: foto_perfil

    @Column(name = "notificaciones", nullable = false)
    private Boolean notificaciones = true; // Campo Java: notificaciones, Columna BBDD: notificaciones

    @Enumerated(EnumType.STRING)
    @Column(name = "visibilidad_perfil", nullable = false, columnDefinition = "checkpoint_visibilidad_enum DEFAULT 'PUBLICO'")
    private VisibilidadEnum visibilidadPerfil = VisibilidadEnum.PUBLICO; // Campo Java: visibilidadPerfil, Columna BBDD: visibilidad_perfil

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime fechaCreacion; // Campo Java: fechaCreacion, Columna BBDD: fecha_creacion

    @Column(name = "fecha_modificacion", nullable = false)
    @UpdateTimestamp
    private OffsetDateTime fechaModificacion; // Campo Java: fechaModificacion, Columna BBDD: fecha_modificacion

    @Column(name = "fecha_eliminacion")
    private OffsetDateTime fechaEliminacion; // Campo Java: fechaEliminacion, Columna BBDD: fecha_eliminacion
}