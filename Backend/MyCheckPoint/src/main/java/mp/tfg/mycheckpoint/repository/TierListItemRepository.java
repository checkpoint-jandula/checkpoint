package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.TierList;
import mp.tfg.mycheckpoint.entity.TierListItem;
import mp.tfg.mycheckpoint.entity.TierSection;
import mp.tfg.mycheckpoint.entity.UserGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TierListItemRepository extends JpaRepository<TierListItem, Long> {

    List<TierListItem> findByTierSectionOrderByItemOrderAsc(TierSection tierSection);

    Optional<TierListItem> findByTierSectionAndUserGame(TierSection tierSection, UserGame userGame);

    @Query("SELECT tli FROM TierListItem tli WHERE tli.internalId = :tierListItemId AND tli.tierSection.tierList.publicId = :tierListPublicId")
    Optional<TierListItem> findByInternalIdAndTierListPublicId(@Param("tierListItemId") Long tierListItemId, @Param("tierListPublicId") UUID tierListPublicId);

    @Query("SELECT CASE WHEN COUNT(tli) > 0 THEN true ELSE false END " +
            "FROM TierListItem tli " +
            "WHERE tli.tierSection.tierList = :tierList AND tli.userGame = :userGame")
    boolean existsByTierListAndUserGame(@Param("tierList") TierList tierList, @Param("userGame") UserGame userGame);

    @Query("SELECT tli FROM TierListItem tli WHERE tli.tierSection.tierList = :tierList AND tli.userGame = :userGame")
    Optional<TierListItem> findByTierListAndUserGame(@Param("tierList") TierList tierList, @Param("userGame") UserGame userGame);

    @Modifying
    @Query("DELETE FROM TierListItem tli WHERE tli.userGame = :userGame AND tli.tierSection.tierList = :tierList")
    void deleteAllByUserGameAndTierList(@Param("userGame") UserGame userGame, @Param("tierList") TierList tierList);

    // Para encontrar el itemOrder más alto en una sección
    @Query("SELECT MAX(tli.itemOrder) FROM TierListItem tli WHERE tli.tierSection = :tierSection")
    Optional<Integer> findMaxItemOrderByTierSection(@Param("tierSection") TierSection tierSection);

    // Para reordenar items cuando uno se elimina o se mueve
    @Modifying
    @Query("UPDATE TierListItem tli SET tli.itemOrder = tli.itemOrder - 1 WHERE tli.tierSection = :tierSection AND tli.itemOrder > :itemOrder")
    void decrementOrderOfSubsequentItems(@Param("tierSection") TierSection tierSection, @Param("itemOrder") int itemOrder);

    @Modifying
    @Query("UPDATE TierListItem tli SET tli.itemOrder = tli.itemOrder + 1 WHERE tli.tierSection = :tierSection AND tli.itemOrder >= :itemOrder")
    void incrementOrderOfSubsequentItemsForInsert(@Param("tierSection") TierSection tierSection, @Param("itemOrder") int itemOrder);
}