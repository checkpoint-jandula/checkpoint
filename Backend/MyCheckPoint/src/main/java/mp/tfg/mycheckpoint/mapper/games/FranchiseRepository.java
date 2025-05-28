package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.entity.games.Franchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FranchiseRepository extends JpaRepository<Franchise, Long> {
    Optional<Franchise> findByIgdbId(Long igdbId);
}