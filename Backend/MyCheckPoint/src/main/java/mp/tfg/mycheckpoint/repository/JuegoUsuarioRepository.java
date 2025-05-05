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
    // Se puede buscar por ID interno del usuario y juego
    Optional<JuegoUsuario> findByUsuario_IdAndJuego_Id(Long usuarioId, Long juegoId);
    // O por publicId del usuario y ID interno del juego
    Optional<JuegoUsuario> findByUsuario_PublicIdAndJuego_Id(UUID usuarioPublicId, Long juegoId);
    // O por publicId del usuario y ID de IGDB del juego (si es más conveniente)
    Optional<JuegoUsuario> findByUsuario_PublicIdAndJuego_Idigdb(UUID usuarioPublicId, Long juegoIdigdb);


    // Buscar todos los juegos de un usuario específico (con paginación)
    Page<JuegoUsuario> findByUsuario_Id(Long usuarioId, Pageable pageable);
    Page<JuegoUsuario> findByUsuario_PublicId(UUID usuarioPublicId, Pageable pageable);
    // Si quisieras buscar por nombre de usuario:
    // Page<JuegoUsuario> findByUsuario_NombreUsuario(String nombreUsuario, Pageable pageable);

    // Buscar todos los juegos de un usuario con un estado específico (con paginación)
    Page<JuegoUsuario> findByUsuario_PublicIdAndEstado(UUID usuarioPublicId, EstadoEnum estado, Pageable pageable);

    // Buscar todos los usuarios que tienen un juego específico (con paginación)
    Page<JuegoUsuario> findByJuego_Id(Long juegoId, Pageable pageable);
    Page<JuegoUsuario> findByJuego_Idigdb(Long juegoIdigdb, Pageable pageable);

    // Buscar todos los juegos de un usuario en una plataforma específica (nuestra Plataforma)
    Page<JuegoUsuario> findByUsuario_PublicIdAndPlataforma_Id(UUID usuarioPublicId, Long plataformaId, Pageable pageable);
    // O por el IDigdb de nuestra Plataforma
    Page<JuegoUsuario> findByUsuario_PublicIdAndPlataforma_Idigdb(UUID usuarioPublicId, Integer plataformaIdigdb, Pageable pageable);


    // Contar juegos por estado para un usuario
    long countByUsuario_PublicIdAndEstado(UUID usuarioPublicId, EstadoEnum estado);

    // Consultas para calcular datos agregados (ejemplos con @Query)
    // Estos datos podrían usarse para actualizar las entidades DuracionJuego y Ranking

    // Calcular puntuación media de un juego (Ignora puntuaciones NULL)
    @Query("SELECT AVG(ju.puntuacion) FROM JuegoUsuario ju WHERE ju.juego.id = :juegoId AND ju.puntuacion IS NOT NULL")
    Optional<Double> findAveragePuntuacionByJuego_Id(@Param("juegoId") Long juegoId);
    // Nota: AVG en JPQL devuelve Double por defecto

    // Contar número de usuarios que han puntuado un juego
    @Query("SELECT COUNT(ju.puntuacion) FROM JuegoUsuario ju WHERE ju.juego.id = :juegoId AND ju.puntuacion IS NOT NULL")
    long countUsersWithPuntuacionForJuego(@Param("juegoId") Long juegoId);


    // Calcular duración media de historia para un juego (Ignora duraciones NULL)
    @Query("SELECT AVG(ju.duracionHistoria) FROM JuegoUsuario ju WHERE ju.juego.id = :juegoId AND ju.duracionHistoria IS NOT NULL")
    Optional<Double> findAverageDuracionHistoriaByJuego_Id(@Param("juegoId") Long juegoId);
    // ... similar para duracionHistoriaSecundarias y duracionCompletista ...

    // Contar número de usuarios que han registrado duración de historia para un juego
    @Query("SELECT COUNT(ju.duracionHistoria) FROM JuegoUsuario ju WHERE ju.juego.id = :juegoId AND ju.duracionHistoria IS NOT NULL")
    long countUsersWithDuracionHistoriaForJuego(@Param("juegoId") Long juegoId);
    // ... similar para otras duraciones ...

    // Puedes añadir métodos para buscar juegos de un usuario por otros criterios de JuegoUsuario
    // Ej: Juegos de un usuario que ha marcado como 'POSESION = true'
    // Page<JuegoUsuario> findByUsuario_PublicIdAndPosesion(UUID usuarioPublicId, Boolean posesion, Pageable pageable);
}