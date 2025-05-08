package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;
import mp.tfg.mycheckpoint.dto.enums.JuegoRelacionTipoEnum; // Asegúrate de crear este Enum

@Entity
@Table(name = "juego_relacion") // Nombre tabla BBDD
@Data
@NoArgsConstructor
@AllArgsConstructor
// Usamos IdClass para la clave primaria compuesta
@IdClass(GameRelationId.class)
public class GameRelation {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juego_origen_id", referencedColumnName = "id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Game originGame; // Juego base

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juego_destino_id", referencedColumnName = "id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Game relatedGame; // Juego relacionado (DLC, Similar, etc.)

    @Id
    @Enumerated(EnumType.STRING) // Asumiendo que la columna en BBDD es VARCHAR (basado en Opción B)
    @Column(name = "tipo_relacion", nullable = false, length = 50) // Asegúrate de que el tipo en BBDD sea VARCHAR
    private JuegoRelacionTipoEnum relationType;
}