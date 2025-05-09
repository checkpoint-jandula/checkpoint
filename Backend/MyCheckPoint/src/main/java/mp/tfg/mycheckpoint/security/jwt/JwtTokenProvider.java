package mp.tfg.mycheckpoint.security.jwt;

import io.jsonwebtoken.*; // Asegúrate que este import está
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import mp.tfg.mycheckpoint.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername()) // Correcto para jjwt 0.11.x+
                .issuedAt(now)                      // Correcto para jjwt 0.11.x+
                .expiration(expiryDate)             // Correcto para jjwt 0.11.x+
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // Usando SignatureAlgorithm directamente
                .compact();
    }

    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username) // Correcto
                .issuedAt(now)     // Correcto
                .expiration(expiryDate) // Correcto
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // Usando SignatureAlgorithm directamente
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey()) // Correcto para jjwt 0.11.x+
                .build()
                .parseSignedClaims(token)    // Correcto para jjwt 0.11.x+
                .getPayload();               // Correcto para jjwt 0.11.x+

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey()) // Correcto
                    .build()
                    .parseSignedClaims(authToken); // Correcto
            return true;
        } catch (SignatureException ex) {
            logger.error("Firma del token JWT inválida: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Token JWT inválido: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Token JWT expirado: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Token JWT no soportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("Claims del token JWT vacías o argumento ilegal: {}", ex.getMessage());
        }
        return false;
    }
}