package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.TierList;
import mp.tfg.mycheckpoint.entity.TierSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TierSectionRepository extends JpaRepository<TierSection, Long> {

    List<TierSection> findByTierListOrderBySectionOrderAsc(TierList tierList);

    Optional<TierSection> findByTierListAndIsDefaultUnclassifiedTrue(TierList tierList);

    @Query("SELECT ts FROM TierSection ts WHERE ts.internalId = :sectionId AND ts.tierList.publicId = :tierListPublicId")
    Optional<TierSection> findByInternalIdAndTierListPublicId(@Param("sectionId") Long sectionId, @Param("tierListPublicId") UUID tierListPublicId);

    @Query("SELECT ts FROM TierSection ts LEFT JOIN FETCH ts.items WHERE ts.tierList = :tierList AND ts.isDefaultUnclassified = true")
    Optional<TierSection> findUnclassifiedSectionWithItemsByTierList(@Param("tierList") TierList tierList);
}