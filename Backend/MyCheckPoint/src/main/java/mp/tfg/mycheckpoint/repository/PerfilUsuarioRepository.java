package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerfilUsuarioRepository extends JpaRepository<PerfilUsuario, Long> { // Entidad PerfilUsuario, ID es Long

    // Buscar perfil por el ID del usuario asociado (la FK es unique)
    Optional<PerfilUsuario> findByUsuario_Id(Long usuarioId);

    // Buscar perfil por el publicId del usuario asociado
    Optional<PerfilUsuario> findByUsuario_PublicId(UUID publicId);

    // Buscar perfil por el nombre de usuario del usuario asociado
    Optional<PerfilUsuario> findByUsuario_NombreUsuario(String nombreUsuario);
}