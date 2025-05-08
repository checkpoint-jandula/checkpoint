package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "modo_juego") // Nombre tabla BBDD
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_igdb", unique = true, nullable = false)
    private Long idigdb;

    @Column(name = "nombre", unique = true, nullable = false, length = 255)
    private String nombre;
}