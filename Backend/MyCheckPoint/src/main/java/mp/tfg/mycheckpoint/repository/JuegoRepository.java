package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Juego;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JuegoRepository extends JpaRepository<Juego, Long> {

    // Buscar juego por su ID de IGDB (único)
    Optional<Juego> findByIdigdb(Long idigdb);

    // Buscar juego por su slug (único)
    Optional<Juego> findBySlug(String slug);

    // Buscar juegos por nombre (ignorando mayúsculas/minúsculas) con paginación
    Page<Juego> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    // Comprobar si existe un juego con ese ID de IGDB
    boolean existsByIdigdb(Long idigdb);

    // Comprobar si existe un juego con ese slug
    boolean existsBySlug(String slug);

    // Podrías añadir métodos para buscar por género, plataforma IGDB, etc. usando JOIN
    // Ejemplo (requiere ajustar entidad Juego si no hay relación bidireccional):
    // Page<Juego> findByGeneros_Nombre(String nombreGenero, Pageable pageable);
    // O con @Query:
    // @Query("SELECT j FROM Juego j JOIN j.generos g WHERE g.nombre = :nombreGenero")
    // Page<Juego> findByNombreGenero(@Param("nombreGenero") String nombreGenero, Pageable pageable);
}