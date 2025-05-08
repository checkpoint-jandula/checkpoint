package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set; // Para buscar varios IDs

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByIdigdb(Long idIgdb);
    Set<Genre> findByIdigdbIn(Set<Long> idIgdbSet); // Para buscar varios géneros por ID de IGDB
}