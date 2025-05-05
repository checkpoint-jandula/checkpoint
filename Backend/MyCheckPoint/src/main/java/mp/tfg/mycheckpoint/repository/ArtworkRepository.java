package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    List<Artwork> findByJuego_Id(Long juegoId);
    Optional<Artwork> findByIdigdbImageId(String idigdbImageId); // Si necesitas buscar por este ID
}
