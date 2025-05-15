package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.PasswordResetToken;
import mp.tfg.mycheckpoint.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUserAndUsedFalseAndExpiryDateAfter(User user, OffsetDateTime now);

    void deleteByUser(User user); // Para eliminar tokens antiguos de un usuario
}
