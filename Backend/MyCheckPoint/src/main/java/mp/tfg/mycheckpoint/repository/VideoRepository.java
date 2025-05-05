package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByJuego_Id(Long juegoId);
    Optional<Video> findByIdigdbVideoId(String idigdbVideoId); // Si necesitas buscar por este ID
}