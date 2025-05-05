package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.junction.PlataformaUsuario;
import mp.tfg.mycheckpoint.entity.embedded.PlataformaUsuarioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlataformaUsuarioRepository extends JpaRepository<PlataformaUsuario, PlataformaUsuarioId> { // ID es PlataformaUsuarioId

    // Buscar todas las plataformas de un usuario
    List<PlataformaUsuario> findById_UsuarioId(Long usuarioId);
    // O por publicId del usuario (más útil)
    List<PlataformaUsuario> findByUsuario_PublicId(UUID usuarioPublicId);

    // Buscar todos los usuarios de una plataforma
    List<PlataformaUsuario> findById_PlataformaId(Long plataformaId);
}
