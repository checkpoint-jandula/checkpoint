package mp.tfg.mycheckpoint.repository.games;



import mp.tfg.mycheckpoint.entity.games.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {
    Optional<Theme> findByIgdbId(Long igdbId);
}
