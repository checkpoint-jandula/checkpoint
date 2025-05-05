package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.junction.TierListJuego;
import mp.tfg.mycheckpoint.entity.embedded.TierListJuegoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TierListJuegoRepository extends JpaRepository<TierListJuego, TierListJuegoId> { // ID es TierListJuegoId

    // Buscar todos los juegos de una TierList
    List<TierListJuego> findById_TierListId(Long tierListId);

    // Buscar todos los juegos asignados a un NivelTier específico
    List<TierListJuego> findByNivelTier_Id(Long nivelTierId);

    // Buscar la entrada de un juego específico en una TierList (por el ID compuesto)
    Optional<TierListJuego> findById_TierListIdAndId_JuegoId(Long tierListId, Long juegoId);
}
