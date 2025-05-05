package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.JuegoUsuario;
import mp.tfg.mycheckpoint.entity.enums.EstadoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JuegoUsuarioRepository extends JpaRepository<JuegoUsuario, Long> {

    // Buscar una entrada específica por usuario y juego (clave única lógica)
    Optional<JuegoUsuario> findByUsuario_IdAndJuego_Id(Long usuarioId, Long juegoId);
    Optional<JuegoUsuario> findByUsuario_PublicIdAndJuego_Id(UUID usuarioPublicId, Long juegoId);
    Optional<JuegoUsuario> findByUsuario_PublicIdAndJuego_Idigdb(UUID usuarioPublicId, Long juegoIdigdb);


    // Buscar todos los juegos de un usuario específico (con paginación)
    Page<JuegoUsuario> findByUsuario_Id(Long usuarioId, Pageable pageable);
    Page<JuegoUsuario> findByUsuario_PublicId(UUID usuarioPublicId, Pageable pageable);
    Page<JuegoUsuario> findByUsuario_NombreUsuario(String nombreUsuario, Pageable pageable);

    // Buscar todos los juegos de un usuario con un estado específico (con paginación)
    Page<JuegoUsuario> findByUsuario_PublicIdAndEstado(UUID usuarioPublicId, EstadoEnum estado, Pageable pageable);

    // Buscar todos los usuarios que tienen un juego específico (con paginación)
    Page<JuegoUsuario> findByJuego_Id(Long juegoId, Pageable pageable);
    Page<JuegoUsuario> findByJuego_Idigdb(Long juegoIdigdb, Pageable pageable);

    // Buscar todos los juegos de un usuario en una plataforma específica
    Page<JuegoUsuario> findByUsuario_PublicIdAndPlataforma_Id(UUID usuarioPublicId, Long plataformaId, Pageable pageable);

    // Contar juegos por estado para un usuario
    long countByUsuario_PublicIdAndEstado(UUID usuarioPublicId, EstadoEnum estado);

    // Calcular puntuación media de un juego (Ejemplo con @Query)
    @Query("SELECT AVG(ju.puntuacion) FROM JuegoUsuario ju WHERE ju.juego.id = :juegoId AND ju.puntuacion IS NOT NULL")
    Optional<Float> findAveragePuntuacionByJuego_Id(@Param("juegoId") Long juegoId);

    // Calcular duración media (Ejemplo con @Query)
    @Query("SELECT AVG(ju.duracionHistoria) FROM JuegoUsuario ju WHERE ju.juego.id = :juegoId AND ju.duracionHistoria IS NOT NULL")
    Optional<Float> findAverageDuracionHistoriaByJuego_Id(@Param("juegoId") Long juegoId);
    // ... (similares para otras duraciones) ...
}