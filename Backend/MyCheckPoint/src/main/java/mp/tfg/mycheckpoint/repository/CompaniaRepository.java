package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Compania;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompaniaRepository extends JpaRepository<Compania, Long> {
    Optional<Compania> findByIdigdb(Long idigdb);
    Optional<Compania> findByNombreIgnoreCase(String nombre);
}
