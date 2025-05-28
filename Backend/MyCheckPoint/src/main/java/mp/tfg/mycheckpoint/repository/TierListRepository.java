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

    Optional<TierList> findBySourceGameListAndType(GameList sourceGameList, TierListType type);

    @Query("SELECT DISTINCT tl FROM TierList tl LEFT JOIN FETCH tl.sections s WHERE tl.publicId = :publicId AND tl.owner = :owner ORDER BY s.sectionOrder ASC")
    Optional<TierList> findByPublicIdAndOwnerWithSections(@Param("publicId") UUID publicId, @Param("owner") User owner);

    @Query("SELECT DISTINCT tl FROM TierList tl LEFT JOIN FETCH tl.sections s WHERE tl.publicId = :publicId AND tl.isPublic = true ORDER BY s.sectionOrder ASC")
    Optional<TierList> findByPublicIdAndIsPublicTrueWithSections(@Param("publicId") UUID publicId);

    @Query("SELECT DISTINCT tl FROM TierList tl LEFT JOIN FETCH tl.sections s WHERE tl.owner = :owner ORDER BY tl.updatedAt DESC, s.sectionOrder ASC")
    List<TierList> findAllByOwnerAndTypeWithSections(@Param("owner") User owner);

    @Query("SELECT DISTINCT tl FROM TierList tl LEFT JOIN FETCH tl.sections s WHERE tl.isPublic = true ORDER BY tl.updatedAt DESC, s.sectionOrder ASC")
    List<TierList> findAllByIsPublicTrueAndFetchSections(); // Este es el método que añadiste/modificaste
}