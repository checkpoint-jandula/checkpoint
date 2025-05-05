package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Franquicia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FranquiciaRepository extends JpaRepository<Franquicia, Long> {
    Optional<Franquicia> findByIdigdbFranchiseId(Long idigdbFranchiseId);
    Optional<Franquicia> findByNombreIgnoreCase(String nombre);
}
