package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Buena práctica añadir @Repository aunque no es estrictamente necesario para JpaRepository

import java.util.Optional;
import java.util.UUID;

@Repository // Indica que es un bean de repositorio y permite la traducción de excepciones de persistencia
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNombreUsuario(String nombreUsuario);

    Optional<User> findByPublicId(UUID publicId);

    boolean existsByEmail(String email); // Útil para validaciones

    boolean existsByNombreUsuario(String nombreUsuario); // Útil para validaciones
}