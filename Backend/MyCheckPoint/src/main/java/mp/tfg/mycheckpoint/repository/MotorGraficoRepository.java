package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.MotorGrafico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MotorGraficoRepository extends JpaRepository<MotorGrafico, Long> {
    Optional<MotorGrafico> findByIdigdb(Long idigdb);
    Optional<MotorGrafico> findByNombreIgnoreCase(String nombre);
}
