package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Game;
import org.springframework.data.jpa.repository.EntityGraph; // Para EAGER fetching selectivo
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByIdigdb(Long idIgdb);
    Optional<Game> findBySlug(String slug);

    // Ejemplo para cargar relaciones EAGER selectivamente al buscar por ID
    @Override
    @EntityGraph(attributePaths = {"genres", "platforms"}) // Carga géneros y plataformas al buscar por ID
    Optional<Game> findById(Long id);
}