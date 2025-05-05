package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Lista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListaRepository extends JpaRepository<Lista, Long> {

    // Buscar listas por el ID del usuario propietario
    Page<Lista> findByUsuario_Id(Long usuarioId, Pageable pageable);
    Page<Lista> findByUsuario_PublicId(UUID usuarioPublicId, Pageable pageable);
    List<Lista> findByUsuario_PublicId(UUID usuarioPublicId); // Sin paginación

    // Buscar una lista específica por ID y ID público del usuario (para seguridad/validación)
    Optional<Lista> findByIdAndUsuario_PublicId(Long listaId, UUID usuarioPublicId);

    // Buscar una lista por nombre y usuario (para evitar duplicados)
    Optional<Lista> findByNombreIgnoreCaseAndUsuario_PublicId(String nombre, UUID usuarioPublicId);
}
