package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNombreUsuario(String nombreUsuario);

    Optional<User> findByPublicId(UUID publicId);

    boolean existsByEmailAndFechaEliminacionIsNull(String email);

    boolean existsByNombreUsuarioAndFechaEliminacionIsNull(String nombreUsuario);

    @Query("SELECT u FROM User u WHERE u.fechaEliminacion IS NOT NULL AND u.fechaEliminacion <= :currentTime")
    List<User> findUsersScheduledForDeletion(@Param("currentTime") OffsetDateTime currentTime);

    // MODIFICACIÓN: Método para buscar usuarios por nombre de usuario (coincidencia parcial, ignorando mayúsculas/minúsculas)
    // Solo usuarios activos y con perfil público o para los que se permita la búsqueda.
    // Por ahora, buscaremos en todos los usuarios no eliminados.
    // Se excluye al propio usuario de los resultados.
    @Query("SELECT u FROM User u WHERE LOWER(u.nombreUsuario) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND u.fechaEliminacion IS NULL " +
            "AND u.id <> :currentUserId") // Excluir al usuario actual de los resultados de su propia búsqueda
    List<User> searchByNombreUsuarioContainingIgnoreCaseAndNotSelf(
            @Param("query") String query,
            @Param("currentUserId") Long currentUserId
    );
}