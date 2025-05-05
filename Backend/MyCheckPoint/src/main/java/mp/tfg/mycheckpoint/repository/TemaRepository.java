package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> { // Tema de IGDB
    Optional<Tema> findByIdigdbThemeId(Long idigdbThemeId);
    Optional<Tema> findByNombreIgnoreCase(String nombre);
}
