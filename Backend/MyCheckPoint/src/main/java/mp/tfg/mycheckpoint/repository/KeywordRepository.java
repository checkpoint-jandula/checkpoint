package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Optional<Keyword> findByIdigdb(Long idigdb);
    Optional<Keyword> findByNombreIgnoreCase(String nombre);
}
