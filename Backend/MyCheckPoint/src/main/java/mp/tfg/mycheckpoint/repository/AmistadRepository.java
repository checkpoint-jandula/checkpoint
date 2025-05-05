package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.junction.Amistad;
import mp.tfg.mycheckpoint.entity.embedded.AmistadId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmistadRepository extends JpaRepository<Amistad, AmistadId> { // ID es AmistadId

    // Buscar todas las relaciones donde un usuario es el "dueño" (usuario_id)
    List<Amistad> findById_UsuarioId(Long usuarioId);

    // Buscar todas las relaciones donde un usuario es el "amigo" (amigo_id)
    List<Amistad> findById_AmigoId(Long amigoId);

    // Buscar una amistad específica (en cualquier dirección)
    // Nota: La restricción UNIQUE simétrica de BD asegura que solo una existe
    @Query("SELECT a FROM Amistad a WHERE (a.id.usuarioId = :user1Id AND a.id.amigoId = :user2Id) OR (a.id.usuarioId = :user2Id AND a.id.amigoId = :user1Id)")
    Optional<Amistad> findAmistadEntreUsuarios(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    // Contar amigos de un usuario
    @Query("SELECT COUNT(a) FROM Amistad a WHERE a.id.usuarioId = :usuarioId OR a.id.amigoId = :usuarioId")
    long countAmigosByUsuarioId(@Param("usuarioId") Long usuarioId);
}
