package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.PlataformaIGDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlataformaIGDBRepository extends JpaRepository<PlataformaIGDB, Long> {
    Optional<PlataformaIGDB> findByIdigdbPlatformId(Long idigdbPlatformId);
    Optional<PlataformaIGDB> findByNombreIgnoreCase(String nombre);
}
