package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Juego;
import mp.tfg.mycheckpoint.entity.enums.GameTypeEnum;
import mp.tfg.mycheckpoint.entity.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
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

    // Buscar juegos por tipo (JUEGO, DLC, EXPANSION) con paginación
    Page<Juego> findByGameType(GameTypeEnum gameType, Pageable pageable);

    // Buscar juegos por status (RELEASED, ALPHA, etc.) con paginación
    Page<Juego> findByStatus(StatusEnum status, Pageable pageable);

    // Buscar juegos lanzados después de una fecha específica
    Page<Juego> findByFechaLanzamientoAfter(LocalDate fecha, Pageable pageable);

    // Ejemplos de consultas personalizadas usando @Query (si las derivadas no son suficientes)

    // Buscar juegos por nombre de género (requiere JOIN)
    @Query("SELECT j FROM Juego j JOIN j.generos g WHERE g.nombre = :nombreGenero")
    Page<Juego> findByNombreGenero(@Param("nombreGenero") String nombreGenero, Pageable pageable);

    // Buscar juegos por nombre de plataforma IGDB (requiere JOIN con la tabla de unión)
    @Query("SELECT j FROM Juego j JOIN j.plataformasIgdb jp WHERE jp.plataformaIgdb.nombre = :nombrePlataforma")
    Page<Juego> findByNombrePlataformaIgdb(@Param("nombrePlataforma") String nombrePlataforma, Pageable pageable);

    // Puedes añadir métodos para buscar por cualquier otra relación ManyToMany o OneToMany
    // Por ejemplo, buscar juegos asociados a una compañía desarrolladora específica
    @Query("SELECT j FROM Juego j JOIN j.companias c WHERE c.nombre = :nombreCompania AND c.developer = true")
    Page<Juego> findByCompaniaDesarrolladora(@Param("nombreCompania") String nombreCompania, Pageable pageable);

    // Contar el número total de juegos (ya viene con JpaRepository, .count())

    // Podrías añadir consultas para buscar juegos por múltiples criterios
    // (usando Specification, Criteria API o @Query más compleja)
}