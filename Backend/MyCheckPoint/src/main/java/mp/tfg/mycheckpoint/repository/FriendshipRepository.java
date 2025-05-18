package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.dto.enums.FriendshipStatus;
import mp.tfg.mycheckpoint.entity.Friendship;
import mp.tfg.mycheckpoint.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Encuentra una amistad/solicitud específica entre dos usuarios, independientemente de quién fue el solicitante
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :user1 AND f.receiver = :user2) OR " +
            "(f.requester = :user2 AND f.receiver = :user1)")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    // Encuentra una solicitud/amistad con un estado específico
    @Query("SELECT f FROM Friendship f WHERE " +
            "((f.requester = :user1 AND f.receiver = :user2) OR (f.requester = :user2 AND f.receiver = :user1)) " +
            "AND f.status = :status")
    Optional<Friendship> findFriendshipBetweenUsersWithStatus(
            @Param("user1") User user1,
            @Param("user2") User user2,
            @Param("status") FriendshipStatus status
    );

    // Solicitudes pendientes recibidas por un usuario
    List<Friendship> findByReceiverAndStatus(User receiver, FriendshipStatus status);

    // Solicitudes pendientes enviadas por un usuario
    List<Friendship> findByRequesterAndStatus(User requester, FriendshipStatus status);

    // Amistades aceptadas para un usuario (donde es solicitante o receptor)
    @Query("SELECT f FROM Friendship f JOIN FETCH f.requester JOIN FETCH f.receiver " +
            "WHERE (f.requester = :user OR f.receiver = :user) AND f.status = :status")
    List<Friendship> findAcceptedFriendshipsForUser(@Param("user") User user, @Param("status") FriendshipStatus status);
}