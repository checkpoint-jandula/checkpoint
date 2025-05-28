package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.User;
import mp.tfg.mycheckpoint.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(User user);

    // Podrías necesitar eliminar tokens expirados o por usuario
    void deleteAllByExpiryDateBefore(OffsetDateTime now);
    void deleteByUserAndUsed(User user, boolean used);
}
