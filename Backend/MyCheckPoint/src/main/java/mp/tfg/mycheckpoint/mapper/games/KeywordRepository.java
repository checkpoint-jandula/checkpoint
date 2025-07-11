package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.entity.games.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Optional<Keyword> findByIgdbId(Long igdbId);
}
