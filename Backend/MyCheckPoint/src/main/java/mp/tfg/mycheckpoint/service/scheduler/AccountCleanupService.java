package mp.tfg.mycheckpoint.service.scheduler;

import mp.tfg.mycheckpoint.entity.Friendship;
import mp.tfg.mycheckpoint.entity.GameList;
import mp.tfg.mycheckpoint.entity.User;
import mp.tfg.mycheckpoint.entity.UserGame;
import mp.tfg.mycheckpoint.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AccountCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(AccountCleanupService.class);
    private final UserRepository userRepository;
    private final UserGameRepository userGameRepository;
    private final GameListRepository gameListRepository;
    private final FriendshipRepository friendshipRepository;
    private final VerificationTokenRepository verificationTokenRepository; // Para tokens de verificación
    private final PasswordResetTokenRepository passwordResetTokenRepository; // Para tokens de reseteo

    @Autowired
    public AccountCleanupService(UserRepository userRepository,
                                 UserGameRepository userGameRepository,
                                 GameListRepository gameListRepository,
                                 FriendshipRepository friendshipRepository,
                                 VerificationTokenRepository verificationTokenRepository,
                                 PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.userGameRepository = userGameRepository;
        this.gameListRepository = gameListRepository;
        this.friendshipRepository = friendshipRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }


    /**
     * Tarea programada para eliminar definitivamente cuentas de usuario
     * cuya fecha de eliminación programada ha llegado o pasado.
     * Se ejecuta, por ejemplo, todos los días a las 02:00 AM.
     * Formato Cron: segundo minuto hora día-del-mes mes día-de-la-semana
     * "0 0 2 * * ?" = todos los días a las 02:00:00 AM
     * Para pruebas, puedes usar una expresión más frecuente como "0 * * * * ?" (cada minuto)
     * ¡Pero cámbiala para producción!
     */
    @Scheduled(cron = "0 * * * * ?") // Ajusta la expresión Cron según tus necesidades
    @Transactional // Importante para que la operación de borrado sea atómica
    public void performScheduledAccountDeletions() {
        OffsetDateTime now = OffsetDateTime.now();
        logger.info("Iniciando tarea de limpieza de cuentas programadas para eliminación antes de o igual a: {}", now);

        // Busca usuarios cuya fechaEliminacion no es null y es anterior o igual a 'ahora'
        List<User> accountsToDelete = userRepository.findUsersScheduledForDeletion(now);

        if (accountsToDelete.isEmpty()) {
            logger.info("No se encontraron cuentas para eliminación definitiva en esta ejecución.");
            return;
        }

        logger.info("Se encontraron {} cuentas para eliminación definitiva.", accountsToDelete.size());
        for (User user : accountsToDelete) {
            logger.warn("Eliminando definitivamente la cuenta del usuario con email: {} (ID: {}, PublicID: {}) programada para el {}",
                    user.getEmail(), user.getId(), user.getPublicId(), user.getFechaEliminacion());
            try {
                // 1. Eliminar UserGame entries
                //    Necesitarás un método en UserGameRepository para borrar por User
                //    o buscar todas las entradas y borrarlas una por una o en lote.
                List<UserGame> userGames = userGameRepository.findByUser(user);
                if (!userGames.isEmpty()) {
                    userGameRepository.deleteAll(userGames); // o userGameRepository.deleteAllInBatch(userGames)
                    logger.info("Eliminadas {} entradas de UserGame para el usuario {}.", userGames.size(), user.getEmail());
                }

                // 2. Eliminar GameList entries
                //    Necesitarás un método en GameListRepository para borrar por User (owner)
                List<GameList> gameLists = gameListRepository.findByOwnerOrderByUpdatedAtDesc(user); // Asumiendo que tienes este método
                if (!gameLists.isEmpty()) {
                    // Antes de borrar la lista, podrías querer desasociar los UserGame de la tabla de unión game_list_user_games.
                    // Si la relación ManyToMany en GameList usa CascadeType.REMOVE o orphanRemoval en userGames,
                    // esto podría no ser necesario O podría causar problemas si UserGame se borra antes.
                    // Dado que game_list_user_games es una tabla de unión, la eliminación de GameList debería eliminar las referencias.
                    // Sin embargo, es más seguro limpiar las asociaciones primero si hay dudas.
                    // Por ahora, asumimos que borrar GameList es suficiente para la tabla de unión.
                    // O, si las GameList tienen UserGames, y UserGames se borraron primero,
                    // la colección gameList.getUserGames() estaría vacía o las referencias rotas.
                    // Es mejor borrar las GameList que le pertenecen.
                    gameListRepository.deleteAll(gameLists);
                    logger.info("Eliminadas {} listas de GameList para el usuario {}.", gameLists.size(), user.getEmail());
                }

                // 3. Eliminar Friendship entries

                // Opción B: Usando el método deleteAllInvolvingUser (más directo si se implementa en el repo)
                 friendshipRepository.deleteAllInvolvingUser(user);
                 logger.info("Eliminadas todas las amistades que involucran al usuario {}.", user.getEmail());

                // 4. Eliminar Tokens de Verificación y Reseteo
                verificationTokenRepository.findByUser(user).ifPresent(token -> {
                    verificationTokenRepository.delete(token);
                    logger.info("Eliminado token de verificación para el usuario {}.", user.getEmail());
                });
                passwordResetTokenRepository.findByUser(user).ifPresent(token -> {
                    passwordResetTokenRepository.delete(token);
                    logger.info("Eliminado token de reseteo de contraseña para el usuario {}.", user.getEmail());
                });

                // 5. Finalmente, eliminar el User
                userRepository.delete(user);
                logger.info("Cuenta del usuario {} eliminada exitosamente de la base de datos.", user.getEmail());
            } catch (Exception e) {
                // Es crucial loguear el error y decidir si la transacción debe hacer rollback
                // para este usuario específico o si se debe continuar con los demás.
                // @Transactional por defecto hará rollback para RuntimeExceptions.
                logger.error("Error al eliminar definitivamente la cuenta del usuario {}: {}. Se procederá con otros si los hay.", user.getEmail(), e.getMessage(), e);
                // Considera enviar una alerta a los administradores si una eliminación falla consistentemente.
            }
        }
        logger.info("Tarea de limpieza de cuentas completada.");
    }
}