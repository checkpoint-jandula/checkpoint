package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.TierList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TierListRepository extends JpaRepository<TierList, Long> {

    // Buscar TierLists por el ID del usuario propietario
    Page<TierList> findByUsuario_Id(Long usuarioId, Pageable pageable);
    Page<TierList> findByUsuario_PublicId(UUID usuarioPublicId, Pageable pageable);
    List<TierList> findByUsuario_PublicId(UUID usuarioPublicId); // Sin paginación

    // Buscar una TierList específica por ID y ID público del usuario (para seguridad/validación)
    Optional<TierList> findByIdAndUsuario_PublicId(Long tierListId, UUID usuarioPublicId);

    // Buscar una TierList por nombre y usuario (para evitar duplicados)
    Optional<TierList> findByNombreIgnoreCaseAndUsuario_PublicId(String nombre, UUID usuarioPublicId);
}