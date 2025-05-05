package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Web;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebRepository extends JpaRepository<Web, Long> {
    List<Web> findByJuego_Id(Long juegoId);
    Optional<Web> findByIdigdbWebsiteId(Long idigdbWebsiteId); // Si necesitas buscar por este ID
}
