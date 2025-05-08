package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.GameRelation;
import mp.tfg.mycheckpoint.entity.GameRelationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface GameRelationRepository extends JpaRepository<GameRelation, GameRelationId> {

    // Método para buscar relaciones salientes por ID del juego origen
    // Necesitamos cargar el 'relatedGame' ávidamente (EAGER) aquí para usarlo en el mapper
    @Query("SELECT gr FROM GameRelation gr JOIN FETCH gr.relatedGame WHERE gr.originGame.id = :originGameId")
    Set<GameRelation> findByOriginGameId(@Param("originGameId") Long originGameId);

    // Podrías necesitar uno similar para relaciones entrantes si las necesitas
    // @Query("SELECT gr FROM GameRelation gr JOIN FETCH gr.originGame WHERE gr.relatedGame.id = :relatedGameId")
    // Set<GameRelation> findByRelatedGameId(@Param("relatedGameId") Long relatedGameId);
}