package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.GameList;
import mp.tfg.mycheckpoint.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameListRepository extends JpaRepository<GameList, Long> {

    Optional<GameList> findByPublicId(UUID publicId);

    Optional<GameList> findByInternalIdAndOwner(Long internalId, User owner);
    Optional<GameList> findByPublicIdAndOwner(UUID publicId, User owner);


    List<GameList> findByOwnerOrderByUpdatedAtDesc(User owner);

    List<GameList> findByIsPublicTrueOrderByUpdatedAtDesc(); // Para listas públicas de todos

    // Para buscar una lista pública por su ID público
    Optional<GameList> findByPublicIdAndIsPublicTrue(UUID publicId);


    // Query para obtener una lista con sus juegos (UserGame) cargados para un propietario específico
    // Esto es útil para evitar problemas de N+1 si se accede a userGames fuera de una transacción.
    // JpaRepository ya hace mucho de esto con FetchType.LAZY bien gestionado en el servicio.
    // Podrías considerar un EntityGraph si el rendimiento se vuelve un problema.
    @Query("SELECT gl FROM GameList gl LEFT JOIN FETCH gl.userGames WHERE gl.internalId = :listId AND gl.owner = :owner")
    Optional<GameList> findByIdAndOwnerWithGames(Long listId, User owner);

    @Query("SELECT gl FROM GameList gl LEFT JOIN FETCH gl.userGames WHERE gl.publicId = :publicId AND gl.owner = :owner")
    Optional<GameList> findByPublicIdAndOwnerWithGames(UUID publicId, User owner);


    @Query("SELECT gl FROM GameList gl LEFT JOIN FETCH gl.userGames WHERE gl.publicId = :publicId AND gl.isPublic = true")
    Optional<GameList> findByPublicIdAndIsPublicTrueWithGames(UUID publicId);
}
