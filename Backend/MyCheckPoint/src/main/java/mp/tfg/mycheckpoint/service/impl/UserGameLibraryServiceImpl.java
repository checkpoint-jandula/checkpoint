package mp.tfg.mycheckpoint.service.impl;

import mp.tfg.mycheckpoint.dto.comment.PublicGameCommentDTO; // IMPORTACIÓN ACTUALIZADA
import mp.tfg.mycheckpoint.dto.enums.VisibilidadEnum;
import mp.tfg.mycheckpoint.dto.games.GameDto;
import mp.tfg.mycheckpoint.dto.usergame.GameDetailDTO;
import mp.tfg.mycheckpoint.dto.usergame.UserGameDataDTO;
import mp.tfg.mycheckpoint.dto.usergame.UserGameResponseDTO;
import mp.tfg.mycheckpoint.entity.User;
import mp.tfg.mycheckpoint.entity.UserGame;
import mp.tfg.mycheckpoint.entity.games.Game;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.mapper.games.GameMapper;
import mp.tfg.mycheckpoint.mapper.UserGameMapper;
import mp.tfg.mycheckpoint.repository.UserRepository;
import mp.tfg.mycheckpoint.repository.UserGameRepository;
import mp.tfg.mycheckpoint.repository.games.GameRepository;
import mp.tfg.mycheckpoint.service.UserGameLibraryService;
import mp.tfg.mycheckpoint.service.games.GameService;
import mp.tfg.mycheckpoint.service.games.IgdbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio {@link UserGameLibraryService} para gestionar
 * la biblioteca de juegos personal de cada usuario.
 * <p>
 * Este servicio maneja la adición, actualización, recuperación y eliminación
 * de juegos en la biblioteca de un usuario. También proporciona una vista detallada
 * de un juego, que combina información general del juego con los datos específicos
 * del usuario y comentarios públicos.
 * </p>
 * <p>
 * Interactúa con varios repositorios y otros servicios (como {@link GameService}
 * e {@link IgdbService}) para asegurar que la información del juego esté completa y
 * actualizada en la base de datos local antes de asociarla con un usuario.
 * </p>
 */
@Service
public class UserGameLibraryServiceImpl implements UserGameLibraryService {

    private static final Logger logger = LoggerFactory.getLogger(UserGameLibraryServiceImpl.class);

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;
    private final UserGameMapper userGameMapper;
    private final GameMapper gameGeneralMapper; // Mapper para la entidad Game y GameDto
    private final GameService gameService; // Servicio para la lógica de guardado/actualización de Games
    private final IgdbService igdbService; // Servicio para interactuar con la API de IGDB

    /**
     * Constructor para UserGameLibraryServiceImpl.
     * Inyecta todas las dependencias necesarias para la gestión de la biblioteca de juegos del usuario.
     *
     * @param userRepository Repositorio para acceder a los datos de los usuarios.
     * @param gameRepository Repositorio para acceder a los datos de los juegos.
     * @param userGameRepository Repositorio para acceder a las entradas de la biblioteca de juegos de los usuarios.
     * @param userGameMapper Mapper para convertir entre entidades UserGame y sus DTOs.
     * @param gameGeneralMapper Mapper para convertir entre entidades Game y GameDto.
     * @param gameService Servicio para la lógica de negocio relacionada con las entidades Game.
     * @param igdbService Servicio para obtener información de juegos desde la API de IGDB.
     */
    @Autowired
    public UserGameLibraryServiceImpl(UserRepository userRepository,
                                      GameRepository gameRepository,
                                      UserGameRepository userGameRepository,
                                      UserGameMapper userGameMapper,
                                      GameMapper gameGeneralMapper,
                                      GameService gameService,
                                      IgdbService igdbService) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
        this.userGameMapper = userGameMapper;
        this.gameGeneralMapper = gameGeneralMapper;
        this.gameService = gameService;
        this.igdbService = igdbService;
    }

    /**
     * Obtiene una entidad {@link User} por su dirección de correo electrónico.
     * Lanza una {@link ResourceNotFoundException} si el usuario no se encuentra.
     *
     * @param email El email del usuario a buscar.
     * @return La entidad {@link User} encontrada.
     * @throws ResourceNotFoundException Si no se encuentra ningún usuario con el email proporcionado.
     */
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /**
     * Asegura que un juego con el {@code igdbId} especificado exista en la base de datos local.
     * <p>
     * Si el juego ya existe localmente:
     * <ul>
     * <li>Si está marcado como parcial ({@code isFullDetails = false}), intenta obtener
     * los detalles completos de IGDB y actualizar la entidad local.</li>
     * <li>Si ya tiene detalles completos, devuelve la entidad existente.</li>
     * </ul>
     * Si el juego no existe localmente, lo obtiene de IGDB, lo guarda en la base de datos
     * local (marcado como con detalles completos) y lo devuelve.
     * </p>
     *
     * @param igdbId El ID de IGDB del juego a asegurar.
     * @return La entidad {@link Game} persistida y con detalles completos (o lo más completos posible).
     * @throws ResourceNotFoundException Si el juego no se encuentra en IGDB al intentar obtenerlo
     * por primera vez o al intentar completar detalles.
     * @throws RuntimeException Si ocurre un error durante el guardado o actualización del juego.
     */
    private Game ensureGameExists(Long igdbId) {
        Optional<Game> gameOpt = gameRepository.findByIgdbId(igdbId);

        if (gameOpt.isPresent()) {
            Game existingGameEntity = gameOpt.get();
            if (!existingGameEntity.isFullDetails()) {
                logger.info("Juego con IGDB ID {} existe localmente pero es parcial. Obteniendo detalles completos de IGDB para actualizar.", igdbId);
                GameDto gameDtoFromIgdb = igdbService.findGameByIgdbId(igdbId).block(); // Bloqueante, considerar manejo reactivo si es cuello de botella

                if (gameDtoFromIgdb == null) {
                    logger.error("No se pudo encontrar el juego con IGDB ID {} en IGDB para completar detalles, aunque existía parcialmente.", igdbId);
                    throw new ResourceNotFoundException("Juego no encontrado en IGDB con ID: " + igdbId + " para completar detalles.");
                }
                gameDtoFromIgdb.setFullDetails(true); // Marcar que el DTO tiene todos los detalles

                // gameService.saveGames se encarga de la lógica de actualización o creación
                List<Game> updatedGames = gameService.saveGames(Collections.singletonList(gameDtoFromIgdb));
                if (updatedGames.isEmpty() || updatedGames.get(0) == null) {
                    logger.error("Error al actualizar el juego (IGDB ID {}) con detalles completos usando gameService.saveGames.", igdbId);
                    throw new RuntimeException("Falló la actualización del juego desde IGDB con ID: " + igdbId + " después de obtener detalles completos.");
                }
                Game fullyUpdatedGameEntity = updatedGames.get(0);
                logger.info("Juego con IGDB ID {} actualizado con detalles completos. Entidad.isFullDetails ahora es: {}", igdbId, fullyUpdatedGameEntity.isFullDetails());
                return fullyUpdatedGameEntity;
            } else {
                logger.debug("Juego con IGDB ID {} existe localmente y ya tiene detalles completos.", igdbId);
                return existingGameEntity;
            }
        } else {
            logger.info("Juego con IGDB ID {} no encontrado en BD local. Obteniendo de IGDB y guardando.", igdbId);
            GameDto gameDtoFromIgdb = igdbService.findGameByIgdbId(igdbId).block(); // Bloqueante
            if (gameDtoFromIgdb == null) {
                throw new ResourceNotFoundException("Juego no encontrado en IGDB con ID: " + igdbId);
            }
            gameDtoFromIgdb.setFullDetails(true); // Nuevo juego de IGDB, se asume que se obtienen todos los detalles

            List<Game> savedGames = gameService.saveGames(Collections.singletonList(gameDtoFromIgdb));
            if (savedGames.isEmpty() || savedGames.get(0) == null) {
                logger.error("Error al guardar nuevo juego de IGDB con ID {}.", igdbId);
                throw new RuntimeException("Falló el guardado del nuevo juego desde IGDB con ID: " + igdbId);
            }
            Game newSavedGameEntity = savedGames.get(0);
            logger.info("Nuevo juego con IGDB ID {} guardado. Entidad.isFullDetails: {}", igdbId, newSavedGameEntity.isFullDetails());
            return newSavedGameEntity;
        }
    }

    /**
     * Añade un juego a la biblioteca de un usuario o actualiza una entrada existente si ya posee el juego.
     * Se asegura de que la entidad {@link Game} exista y tenga detalles completos en la base de datos local
     * antes de crear o actualizar la entrada {@link UserGame}.
     *
     * @param userEmail El email del usuario.
     * @param igdbId El ID de IGDB del juego.
     * @param userGameDataDTO DTO con los datos específicos del usuario para este juego
     * (estado, puntuación, plataforma, comentarios, etc.).
     * @return Un {@link UserGameResponseDTO} que representa la entrada de la biblioteca creada o actualizada.
     * @throws ResourceNotFoundException Si el usuario no se encuentra, o si el juego no se puede
     * asegurar en la base de datos local (ej. no encontrado en IGDB).
     */
    @Override
    @Transactional
    public UserGameResponseDTO addOrUpdateGameInLibrary(String userEmail, Long igdbId, UserGameDataDTO userGameDataDTO) {
        User user = getUserByEmail(userEmail);
        Game game = ensureGameExists(igdbId); // Asegura que el juego exista en la BD local y esté completo

        UserGame userGame = userGameRepository.findByUserAndGame(user, game)
                .orElseGet(() -> UserGame.builder().user(user).game(game).build()); // Crea una nueva entrada si no existe

        userGameMapper.updateFromDto(userGameDataDTO, userGame); // Aplica los datos del DTO a la entidad UserGame
        UserGame savedUserGame = userGameRepository.save(userGame);
        logger.info("Usuario {} {} el juego con IGDB ID {} (ID interno UserGame: {}) en su biblioteca.",
                userEmail, (userGame.getInternalId() == null || savedUserGame.getInternalId().equals(userGame.getInternalId())) ? "actualizó" : "añadió", // Lógica mejorada para el log
                igdbId, savedUserGame.getInternalId());
        return userGameMapper.toResponseDto(savedUserGame);
    }

    /**
     * Obtiene la lista completa de juegos en la biblioteca del usuario especificado.
     *
     * @param userEmail El email del usuario.
     * @return Una lista de {@link UserGameResponseDTO}, cada uno representando un juego
     * en la biblioteca del usuario con sus datos personalizados. La lista puede estar vacía.
     * @throws ResourceNotFoundException Si el usuario no se encuentra.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserGameResponseDTO> getUserGameLibrary(String userEmail) {
        User user = getUserByEmail(userEmail);
        return userGameRepository.findByUser(user).stream()
                .map(userGameMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una entrada específica de la biblioteca de un usuario, identificada por el ID de IGDB del juego.
     *
     * @param userEmail El email del usuario.
     * @param igdbId El ID de IGDB del juego a buscar en la biblioteca del usuario.
     * @return Un {@link UserGameResponseDTO} con los datos del juego en la biblioteca del usuario.
     * @throws ResourceNotFoundException Si el usuario no se encuentra, o si el juego con el
     * {@code igdbId} especificado no se encuentra en su biblioteca.
     */
    @Override
    @Transactional(readOnly = true)
    public UserGameResponseDTO getUserGameFromLibrary(String userEmail, Long igdbId) {
        User user = getUserByEmail(userEmail);
        return userGameRepository.findByUserAndGame_IgdbId(user, igdbId)
                .map(userGameMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Juego con IGDB ID " + igdbId + " no encontrado en la biblioteca del usuario " + userEmail));
    }

    /**
     * Obtiene una vista detallada de un juego, combinando su información general,
     * los datos específicos del usuario (si está autenticado y el juego está en su biblioteca),
     * y una lista de comentarios públicos realizados por otros usuarios sobre ese juego.
     * <p>
     * Si el juego existe localmente y tiene detalles completos, se usa esa información.
     * Si existe localmente pero es parcial, se obtienen los detalles completos de IGDB
     * solo para la respuesta (sin actualizar la entidad local en este flujo).
     * Si no existe localmente, se obtiene de IGDB para la respuesta.
     * </p>
     *
     * @param igdbId El ID de IGDB del juego para el cual se solicitan los detalles.
     * @param userEmail El email del usuario actualmente autenticado (puede ser {@code null} si el acceso es anónimo).
     * Si se proporciona y el usuario existe, se intentarán cargar los datos de {@link UserGame}.
     * @return Un {@link GameDetailDTO} que contiene la información agregada.
     * @throws ResourceNotFoundException Si el juego no se puede encontrar (ni localmente ni en IGDB),
     * o si se proporciona {@code userEmail} pero el usuario no existe.
     */
    @Override
    @Transactional(readOnly = true)
    public GameDetailDTO getGameDetailsForUser(Long igdbId, String userEmail) {
        Optional<Game> gameEntityOptional = gameRepository.findByIgdbId(igdbId);
        GameDto gameInfoDto;
        Game gameEntityForUserAndComments = gameEntityOptional.orElse(null);

        if (gameEntityOptional.isPresent()) {
            Game localGameEntity = gameEntityOptional.get();
            logger.debug("Juego con IGDB ID {} encontrado localmente. FullDetails: {}", igdbId, localGameEntity.isFullDetails());
            if (localGameEntity.isFullDetails()) {
                logger.debug("Usando datos completos locales para gameInfo del juego IGDB ID {}.", igdbId);
                Game fullyLoadedLocalGame = gameService.getGameByIgdbIdOriginal(igdbId);
                if (fullyLoadedLocalGame == null) {
                    throw new ResourceNotFoundException("Error al cargar la entidad local completa para el juego IGDB ID: " + igdbId);
                }
                gameInfoDto = gameGeneralMapper.toDto(fullyLoadedLocalGame);
            } else {
                logger.info("Juego IGDB ID {} existe localmente pero es parcial. Obteniendo detalles de IGDB solo para la vista.", igdbId);
                gameInfoDto = igdbService.findGameByIgdbId(igdbId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Juego no encontrado en IGDB con ID: " + igdbId + " al intentar obtener detalles completos para la vista.")))
                        .block(); // Bloqueante
            }
        } else {
            logger.info("Juego con IGDB ID {} no encontrado localmente. Obteniendo de IGDB para la vista.", igdbId);
            gameInfoDto = igdbService.findGameByIgdbId(igdbId)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Juego no encontrado en IGDB con ID: " + igdbId)))
                    .block(); // Bloqueante
        }

        if (gameInfoDto == null) {
            throw new ResourceNotFoundException("No se pudo obtener información del juego con IGDB ID: " + igdbId);
        }

        UserGameResponseDTO userGameDataDto = null;
        if (userEmail != null && gameEntityForUserAndComments != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario actual no encontrado con email: " + userEmail)); // Lanza excepción si el usuario autenticado no se encuentra

            userGameRepository.findByUserAndGame(user, gameEntityForUserAndComments)
                    .ifPresent(userGame -> {
                        // userGameDataDto = userGameMapper.toResponseDto(userGame); // Esta línea estaba comentada en el original
                    });
            // Corrección: Asignar el resultado del mapeo a userGameDataDto
            userGameDataDto = userGameRepository.findByUserAndGame(user, gameEntityForUserAndComments)
                    .map(userGameMapper::toResponseDto)
                    .orElse(null);
        } else if (userEmail != null) { // userEmail provisto pero gameEntityForUserAndComments es null
            logger.debug("Juego con IGDB ID {} no existe en BD local, no se pueden obtener datos de UserGame para el usuario {}.", igdbId, userEmail);
        }

        List<PublicGameCommentDTO> publicComments = Collections.emptyList();
        if (gameEntityForUserAndComments != null) {
            // Solo buscar comentarios si el juego tiene una entrada en la BD local.
            List<UserGame> userGamesWithComments = userGameRepository.findPublicCommentsForGame(gameEntityForUserAndComments, VisibilidadEnum.PUBLICO);
            publicComments = userGamesWithComments.stream()
                    .map(ug -> PublicGameCommentDTO.builder()
                            .username(ug.getUser().getNombreUsuario())
                            .userPublicId(ug.getUser().getPublicId())
                            .commentText(ug.getComment())
                            .commentDate(ug.getUpdatedAt()) // Usar updatedAt de UserGame como fecha del comentario
                            .build())
                    .collect(Collectors.toList());
            logger.debug("Se encontraron {} comentarios públicos para el juego local con IGDB ID {}.", publicComments.size(), igdbId);
        } else {
            logger.debug("Juego con IGDB ID {} no existe en BD local, no se buscan comentarios públicos.", igdbId);
        }

        return GameDetailDTO.builder()
                .gameInfo(gameInfoDto)
                .userGameData(userGameDataDto)
                .publicComments(publicComments)
                .build();
    }

    /**
     * Elimina un juego de la biblioteca personal del usuario.
     *
     * @param userEmail El email del usuario.
     * @param igdbId El ID de IGDB del juego a eliminar de la biblioteca.
     * @throws ResourceNotFoundException Si el usuario no se encuentra, o si el juego
     * con el {@code igdbId} especificado no está en su biblioteca
     * y, por lo tanto, no puede ser eliminado.
     */
    @Override
    @Transactional
    public void removeGameFromLibrary(String userEmail, Long igdbId) {
        User user = getUserByEmail(userEmail);
        // Verifica si la entrada existe antes de intentar borrarla para dar un mensaje de error más específico.
        if (!userGameRepository.existsByUserAndGame_IgdbId(user, igdbId)) {
            throw new ResourceNotFoundException("Juego con IGDB ID " + igdbId + " no encontrado en la biblioteca del usuario " + userEmail + " para eliminar.");
        }
        userGameRepository.deleteByUserAndGame_IgdbId(user, igdbId);
        logger.info("Usuario {} eliminó el juego con IGDB ID {} de su biblioteca.", userEmail, igdbId);
    }
}