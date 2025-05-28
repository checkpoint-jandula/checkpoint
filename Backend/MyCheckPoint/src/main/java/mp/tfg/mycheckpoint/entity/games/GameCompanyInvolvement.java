package mp.tfg.mycheckpoint.entity.games;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "game_company_involvements", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"involvement_igdb_id"}) // El ID de IGDB para esta relación debe ser único
})
public class GameCompanyInvolvement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    @Column(name = "involvement_igdb_id", unique = true, nullable = false)
    private Long involvementIgdbId; // El 'id' del objeto en la lista involved_companies

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_internal_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}) // Persistir/mergear compañía si es nueva/cambia
    @JoinColumn(name = "company_internal_id", nullable = false)
    private Company company;

    private boolean developer;
    private boolean porting;
    private boolean publisher;
    private boolean supporting;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameCompanyInvolvement that = (GameCompanyInvolvement) o;
        // Una relación de involucramiento es única por su propio ID de IGDB si lo tiene,
        // o por la combinación de juego y compañía si el ID de involucramiento no fuera fiable.
        // Aquí usamos el ID de involucramiento de IGDB como clave principal de negocio.
        return Objects.equals(involvementIgdbId, that.involvementIgdbId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(involvementIgdbId);
    }
}
