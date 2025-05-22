package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.dto.enums.TierListType;
import mp.tfg.mycheckpoint.entity.GameList;
import mp.tfg.mycheckpoint.entity.TierList;
import mp.tfg.mycheckpoint.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TierListRepository extends JpaRepository<TierList, Long> {

    Optional<TierList> findByPublicId(UUID publicId);

    List<TierList> findByOwnerAndTypeOrderByUpdatedAtDesc(User owner, TierListType type);

    Optional<TierList> findBySourceGameListAndType(GameList sourceGameList, TierListType type);

    @Query("SELECT tl FROM TierList tl LEFT JOIN FETCH tl.sections s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.userGame ug LEFT JOIN FETCH ug.game g LEFT JOIN FETCH g.cover WHERE tl.publicId = :publicId AND tl.owner = :owner")
    Optional<TierList> findByPublicIdAndOwnerWithDetails(@Param("publicId") UUID publicId, @Param("owner") User owner);

    @Query("SELECT tl FROM TierList tl LEFT JOIN FETCH tl.sections s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.userGame ug LEFT JOIN FETCH ug.game g LEFT JOIN FETCH g.cover WHERE tl.publicId = :publicId AND tl.isPublic = true")
    Optional<TierList> findByPublicIdAndIsPublicTrueWithDetails(@Param("publicId") UUID publicId);

    @Query("SELECT tl FROM TierList tl WHERE tl.owner = :owner AND tl.type = :type ORDER BY tl.updatedAt DESC")
    List<TierList> findAllByOwnerAndType(@Param("owner") User owner, @Param("type") TierListType type);

    @Query("SELECT tl FROM TierList tl WHERE tl.isPublic = true ORDER BY tl.updatedAt DESC")
    List<TierList> findAllByIsPublicTrue();
}