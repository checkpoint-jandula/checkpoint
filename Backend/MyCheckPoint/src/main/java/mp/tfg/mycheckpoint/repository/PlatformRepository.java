package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, Long> {
    Optional<Platform> findByIdigdb(Integer idIgdb); // Buscar por ID de IGDB
    Optional<Platform> findByNombreIgnoreCase(String nombre); // Buscar por nombre (ignorando may/min)
}