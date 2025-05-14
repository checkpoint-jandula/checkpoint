package mp.tfg.mycheckpoint.repository.games;



import mp.tfg.mycheckpoint.entity.games.GameMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameModeRepository extends JpaRepository<GameMode, Long> {
    Optional<GameMode> findByIgdbId(Long igdbId);
}