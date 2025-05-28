package mp.tfg.mycheckpoint.repository.games;



import mp.tfg.mycheckpoint.entity.games.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByIgdbId(Long igdbId); // Para buscar si un juego ya existe por su ID de IGDB
}