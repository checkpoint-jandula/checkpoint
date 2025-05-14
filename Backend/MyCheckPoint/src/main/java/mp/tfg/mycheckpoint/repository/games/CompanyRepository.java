package mp.tfg.mycheckpoint.repository.games;


import mp.tfg.mycheckpoint.entity.games.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByIgdbId(Long igdbId);
}
