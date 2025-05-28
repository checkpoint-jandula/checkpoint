package mp.tfg.mycheckpoint.repository.games;



import mp.tfg.mycheckpoint.entity.games.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByIgdbId(Long igdbId);
}