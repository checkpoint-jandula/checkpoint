package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.dto.enums.VisibilidadEnum;
import mp.tfg.mycheckpoint.entity.User;
import mp.tfg.mycheckpoint.entity.UserGame;
import mp.tfg.mycheckpoint.entity.games.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGameRepository extends JpaRepository<UserGame, Long> {

    Optional<UserGame> findByUserAndGame(User user, Game game);

    // Para obtener todos los juegos de la biblioteca de un usuario
    List<UserGame> findByUser(User user);

    // Para buscar por IGDB ID (necesitarás un join o una query más compleja si solo tienes igdbId)
    // O, más simple, buscar Game por igdbId y luego usar ese Game en findByUserAndGame
    Optional<UserGame> findByUserAndGame_IgdbId(User user, Long igdbId);

    // Para verificar si existe una entrada
    boolean existsByUserAndGame_IgdbId(User user, Long igdbId);

    void deleteByUserAndGame_IgdbId(User user, Long igdbId);

    @Query("SELECT ug FROM UserGame ug JOIN FETCH ug.user u " +
            "WHERE ug.game = :game " +
            "AND ug.comment IS NOT NULL AND ug.comment <> '' " +
            "AND u.visibilidadPerfil = :visibilidad " +
            "AND u.fechaEliminacion IS NULL " +
            "ORDER BY ug.updatedAt DESC")
    List<UserGame> findPublicCommentsForGame(@Param("game") Game game, @Param("visibilidad") VisibilidadEnum visibilidad);
}
