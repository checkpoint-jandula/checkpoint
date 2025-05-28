package mp.tfg.mycheckpoint.repository.games;


import mp.tfg.mycheckpoint.entity.games.GameCompanyInvolvement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface GameCompanyInvolvementRepository extends JpaRepository<GameCompanyInvolvement, Long> {
    Optional<GameCompanyInvolvement> findByInvolvementIgdbId(Long involvementIgdbId);
    // Podrías necesitar métodos para buscar por juego y compañía, etc.
    // Set<GameCompanyInvolvement> findByGame(Game game); // Para borrar los antiguos si no usas orphanRemoval así
}
