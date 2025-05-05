package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository // Indica a Spring que esta interfaz es un componente de repositorio
public interface UsuarioRepository extends JpaRepository<Usuario, Long> { // Entidad Usuario, ID es Long

    // Spring Data JPA genera la consulta basada en el nombre del metodo

    // Buscar usuario por su nombre de usuario (debe ser único)
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    // Buscar usuario por su email (debe ser único)
    Optional<Usuario> findByEmail(String email);

    // Buscar usuario por su identificador público UUID (debe ser único)
    Optional<Usuario> findByPublicId(UUID publicId);

    // Comprobar si existe un usuario con ese nombre de usuario
    boolean existsByNombreUsuario(String nombreUsuario);

    // Comprobar si existe un usuario con ese email
    boolean existsByEmail(String email);

    // Puedes añadir más métodos según necesites, por ejemplo, para buscar usuarios activos:
    // List<Usuario> findByFechaEliminacionIsNull(); // Si usas Soft Delete y necesitas consultarlos explícitamente
}