package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.PerspectivaJugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerspectivaJugadorRepository extends JpaRepository<PerspectivaJugador, Long> {
    Optional<PerspectivaJugador> findByIdigdb(Long idigdb);
    Optional<PerspectivaJugador> findByNombreIgnoreCase(String nombre);
}
