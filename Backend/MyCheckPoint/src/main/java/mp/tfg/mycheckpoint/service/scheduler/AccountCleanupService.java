package mp.tfg.mycheckpoint.service.scheduler;

import mp.tfg.mycheckpoint.entity.User;
import mp.tfg.mycheckpoint.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AccountCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(AccountCleanupService.class);
    private final UserRepository userRepository;
    // Podrías necesitar inyectar otros repositorios si la eliminación de usuario implica
    // limpiar datos relacionados que no se manejan por CASCADE en la BBDD.

    @Autowired
    public AccountCleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    @Scheduled(cron = "0 0 2 * * ?") // Ajusta la expresión Cron según tus necesidades
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
                // Aquí es donde manejarías la lógica de eliminación de datos relacionados
                // ANTES de eliminar la entidad User, si no está configurado con CascadeType.REMOVE
                // o restricciones ON DELETE CASCADE en la base de datos.
                // Ejemplo: gameListRepository.deleteByUser(user);
                //          userPreferencesRepository.deleteByUser(user);

                userRepository.delete(user); // Eliminación definitiva
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