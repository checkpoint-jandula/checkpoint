package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "plataforma") // Nombre tabla en BBDD
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_igdb", unique = true)
    private Integer idigdb; // Campo Java: idigdb, Columna BBDD: id_igdb

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "nombre_alternativo", length = 255)
    private String nombreAlternativo; // Campo Java: nombreAlternativo, Columna BBDD: nombre_alternativo

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl; // Campo Java: logoUrl, Columna BBDD: logo_url

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false)
    @UpdateTimestamp
    private OffsetDateTime fechaModificacion;

    @Column(name = "fecha_eliminacion")
    private OffsetDateTime fechaEliminacion;
}
