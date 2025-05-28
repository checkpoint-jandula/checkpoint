package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens") // Nueva tabla
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {

    // Tiempo de expiración más corto para reseteo de contraseña (ej: 1 hora)
    public static final int EXPIRATION_MINUTES = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id", foreignKey = @ForeignKey(name = "FK_PASSWORD_RESET_TOKEN_USER"))
    private User user;

    @Column(nullable = false)
    private OffsetDateTime expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    public PasswordResetToken(User user) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        this.expiryDate = OffsetDateTime.now().plusMinutes(EXPIRATION_MINUTES);
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(this.expiryDate);
    }
}
