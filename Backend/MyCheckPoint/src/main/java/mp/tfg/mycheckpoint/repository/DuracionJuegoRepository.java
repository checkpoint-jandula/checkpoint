package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.DuracionJuego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DuracionJuegoRepository extends JpaRepository<DuracionJuego, Long> { // ID es Long (id_duracion)

    // Buscar la duración asociada a un juego (es unique)
    Optional<DuracionJuego> findByJuego_Id(Long juegoId);
    Optional<DuracionJuego> findByJuego_Idigdb(Long juegoIdigdb);
}
