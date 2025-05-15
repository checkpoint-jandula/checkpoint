package mp.tfg.mycheckpoint.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens") // Nombre de la tabla en la BDD
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {

    // Tiempo de expiración del token en minutos (ej: 24 horas)
    private static final int EXPIRATION_MINUTES = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    // Relación OneToOne con la entidad User.
    // FetchType.EAGER para cargar el usuario junto con el token.
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id", foreignKey = @ForeignKey(name = "FK_VERIFICATION_TOKEN_USER"))
    private User user;

    @Column(nullable = false)
    private OffsetDateTime expiryDate;

    @Column(nullable = false)
    private boolean used = false; // Para marcar si el token ya fue utilizado

    /**
     * Constructor para crear un nuevo token de verificación para un usuario.
     * @param user El usuario para el cual se genera el token.
     */
    public VerificationToken(User user) {
        this.user = user;
        this.token = UUID.randomUUID().toString(); // Genera un token aleatorio
        this.expiryDate = OffsetDateTime.now().plusMinutes(EXPIRATION_MINUTES);
    }

    /**
     * Verifica si el token ha expirado.
     * @return true si el token ha expirado, false en caso contrario.
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(this.expiryDate);
    }
}