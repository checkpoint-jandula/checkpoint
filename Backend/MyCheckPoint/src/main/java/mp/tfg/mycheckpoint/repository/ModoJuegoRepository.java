package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.ModoJuego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModoJuegoRepository extends JpaRepository<ModoJuego, Long> {
    Optional<ModoJuego> findByIdigdb(Long idigdb);
    Optional<ModoJuego> findByNombreIgnoreCase(String nombre);
}
