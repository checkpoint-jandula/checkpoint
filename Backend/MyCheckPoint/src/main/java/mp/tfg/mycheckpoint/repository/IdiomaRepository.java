package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Idioma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdiomaRepository extends JpaRepository<Idioma, Long> {
    Optional<Idioma> findByIdigdbLanguageId(Long idigdbLanguageId);
    Optional<Idioma> findByNombreIgnoreCase(String nombre);
    Optional<Idioma> findByLocaleIgnoreCase(String locale);
}
