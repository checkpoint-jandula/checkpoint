package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "compania") // Nombre tabla BBDD
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_igdb", unique = true, nullable = false)
    private Long idigdb;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "developer")
    private Boolean developer = false;

    @Column(name = "publisher")
    private Boolean publisher = false;

    @Column(name = "porting")
    private Boolean porting = false;

    @Column(name = "supporting")
    private Boolean supporting = false;
}