package mp.tfg.mycheckpoint.repository.games;


import mp.tfg.mycheckpoint.entity.games.GameEngine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameEngineRepository extends JpaRepository<GameEngine, Long> {
    Optional<GameEngine> findByIgdbId(Long igdbId);
}
