package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.entity.junction.Amistad;
import mp.tfg.mycheckpoint.entity.junction.PlataformaUsuario;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete; // Para soft delete
import org.hibernate.annotations.Where; // Para soft delete

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "usuario", indexes = {
        @Index(name = "idx_usuario_email", columnList = "email"),
        @Index(name = "idx_usuario_nombreusuario", columnList = "nombreUsuario"),
        @Index(name = "idx_usuario_fechaeliminacion", columnList = "fechaEliminacion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Útil para crear instancias, especialmente en tests
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Comparar solo por ID
@ToString(exclude = {"perfilUsuario", "juegosUsuario", "listas", "tierLists", "plataformasUsuario", "amistades", "amigosDe"}) // Evitar recursión
// Soft Delete: Al llamar a delete, se ejecuta el UPDATE en lugar de DELETE
@SQLDelete(sql = "UPDATE usuario SET fechaEliminacion = CURRENT_TIMESTAMP WHERE id = ?")
// Al buscar, filtra automáticamente los eliminados
@Where(clause = "fechaEliminacion IS NULL")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Incluir ID en equals/hashCode
    private Long id;

    @GeneratedValue(strategy = GenerationType.UUID) // JPA generará el UUID si no se proporciona
    @Column(name = "publicId", unique = true, nullable = false, updatable = false, columnDefinition = "UUID DEFAULT gen_random_uuid()")
    @EqualsAndHashCode.Include // Incluir publicId también puede ser útil
    private UUID publicId;

    @Column(name = "nombreUsuario", length = 100, unique = true, nullable = false)
    private String nombreUsuario;

    @Column(name = "email", length = 255, unique = true, nullable = false)
    private String email;

    @Column(name = "contraseña", length = 255, nullable = false)
    private String contrasena; // Ojo: nombre de variable en camelCase

    @Column(name = "fechaRegistro", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp(source = SourceType.DB) // Gestionado por DB
    private OffsetDateTime fechaRegistro;

    @Column(name = "fechaCreacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp(source = SourceType.DB) // Gestionado por DB
    private OffsetDateTime fechaCreacion;

    @Column(name = "fechaModificacion", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp(source = SourceType.DB) // Gestionado por Trigger de DB
    private OffsetDateTime fechaModificacion;

    @Column(name = "fechaEliminacion", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime fechaEliminacion; // Para Soft Delete

    // --- Relaciones ---

    // Usuario 1 <--> 1 PerfilUsuario (Usuario es el inverso, PerfilUsuario tiene la FK)
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PerfilUsuario perfilUsuario;

    // Usuario 1 <--> * JuegoUsuario
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<JuegoUsuario> juegosUsuario = new HashSet<>();

    // Usuario 1 <--> * Lista
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Lista> listas = new HashSet<>();

    // Usuario 1 <--> * TierList
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TierList> tierLists = new HashSet<>();

    // Usuario 1 <--> * PlataformaUsuario (Tabla de unión)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<PlataformaUsuario> plataformasUsuario = new HashSet<>();

    // Usuario * <--> * Usuario (Amistad - Lado que inicia la amistad)
    // Usamos la entidad Amistad para gestionar la relación N:M
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Amistad> amistades = new HashSet<>(); // Amistades iniciadas por este usuario

    // Usuario * <--> * Usuario (Amistad - Lado que recibe la amistad)
    @OneToMany(mappedBy = "amigo", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Amistad> amigosDe = new HashSet<>(); // Amistades donde este usuario es el amigo

    // --- Métodos Helper (Opcional pero útil) ---

    public void addJuegoUsuario(JuegoUsuario juegoUsuario) {
        this.juegosUsuario.add(juegoUsuario);
        juegoUsuario.setUsuario(this);
    }

    public void removeJuegoUsuario(JuegoUsuario juegoUsuario) {
        this.juegosUsuario.remove(juegoUsuario);
        juegoUsuario.setUsuario(null);
    }

    // ... (helpers similares para Listas, TierLists, Plataformas, Amistades)

    // --- Ciclo de vida JPA (Opcional) ---
    @PrePersist
    protected void onCreate() {
        // Podríamos generar el publicId aquí si no usamos @GeneratedValue
        // if (publicId == null) {
        //     publicId = UUID.randomUUID();
        // }
        // Las fechas las maneja la BD o @CreationTimestamp/@UpdateTimestamp
    }
}