package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "juego_idioma_soporte") // Nombre tabla BBDD
@Data
@NoArgsConstructor
@AllArgsConstructor
// Usamos IdClass para clave primaria compuesta
@IdClass(GameLanguageSupportId.class)
public class GameLanguageSupport {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juego_id", referencedColumnName = "id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Game game;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idioma_id", referencedColumnName = "id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Language language;

    @Id
    @Column(name = "tipo_soporte", nullable = false)
    private Integer tipoSoporte; // 1=Interfaz, 2=Audio, 3=Subtítulos
}