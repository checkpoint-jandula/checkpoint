package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tema") // Nombre tabla BBDD
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_igdb_theme_id", unique = true, nullable = false) // Nombre columna BBDD
    private Long idigdbThemeId; // Nombre campo Java

    @Column(name = "nombre", unique = true, nullable = false, length = 255)
    private String nombre;
}