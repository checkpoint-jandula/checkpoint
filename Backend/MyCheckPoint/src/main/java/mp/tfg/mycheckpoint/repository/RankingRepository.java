package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> { // ID es Long (id_ranking)

    // Buscar el ranking asociado a un juego (es unique)
    Optional<Ranking> findByJuego_Id(Long juegoId);
    Optional<Ranking> findByJuego_Idigdb(Long juegoIdigdb);
}
