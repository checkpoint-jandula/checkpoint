package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.NivelTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NivelTierRepository extends JpaRepository<NivelTier, Long> {

    // Buscar todos los niveles de una TierList específica, ordenados por 'orden'
    List<NivelTier> findByTierList_IdOrderByOrdenAsc(Long tierListId);

    // Buscar un nivel por TierList y nombre (constraint unique)
    Optional<NivelTier> findByTierList_IdAndNombreIgnoreCase(Long tierListId, String nombre);

    // Buscar un nivel por TierList y orden (constraint unique)
    Optional<NivelTier> findByTierList_IdAndOrden(Long tierListId, Integer orden);
}
