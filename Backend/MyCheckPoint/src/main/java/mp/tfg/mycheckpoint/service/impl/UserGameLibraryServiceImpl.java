package mp.tfg.mycheckpoint.service.impl;

import mp.tfg.mycheckpoint.dto.comment.PublicGameCommentDTO; // IMPORTACIÓN ACTUALIZADA
import mp.tfg.mycheckpoint.dto.enums.FriendshipStatus;
import mp.tfg.mycheckpoint.dto.enums.TierListType;
import mp.tfg.mycheckpoint.dto.enums.VisibilidadEnum;
import mp.tfg.mycheckpoint.dto.games.GameDto;
import mp.tfg.mycheckpoint.dto.usergame.GameDetailDTO;
import mp.tfg.mycheckpoint.dto.usergame.UserGameDataDTO;
import mp.tfg.mycheckpoint.dto.usergame.UserGameResponseDTO;
import mp.tfg.mycheckpoint.entity.*;
import mp.tfg.mycheckpoint.entity.games.Game;
import mp.tfg.mycheckpoint.exception.InvalidOperationException;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.exception.UnauthorizedOperationException;
import mp.tfg.mycheckpoint.mapper.games.GameMapper;
import mp.tfg.mycheckpoint.mapper.UserGameMapper;
import mp.tfg.mycheckpoint.repository.*;
import mp.tfg.mycheckpoint.repository.games.GameRepository;
import mp.tfg.mycheckpoint.service.GameListService;
import mp.tfg.mycheckpoint.service.TierListService;
import mp.tfg.mycheckpoint.service.UserGameLibraryService;
import mp.tfg.mycheckpoint.service.games.GameService;
import mp.tfg.mycheckpoint.service.games.IgdbService;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


import java.util.*;
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

    private final GameListService gameListService;
    private final TierListService tierListService;
    private final GameListRepository gameListRepository;
    private final TierListRepository tierListRepository;
    private final TierListItemRepository tierListItemRepository;
    private final FriendshipRepository friendshipRepository;

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
                                      IgdbService igdbService,
                                      GameListService gameListService,
                                      TierListService tierListService,
                                      GameListRepository gameListRepository,
                                      TierListRepository tierListRepository,
                                      TierListItemRepository tierListItemRepository,
                                      FriendshipRepository friendshipRepository){
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
        this.userGameMapper = userGameMapper;
        this.gameGeneralMapper = gameGeneralMapper;
        this.gameService = gameService;
        this.igdbService = igdbService;
        this.gameListService = gameListService;
        this.tierListService = tierListService;
        this.gameListRepository = gameListRepository;
        this.tierListRepository = tierListRepository;
        this.tierListItemRepository = tierListItemRepository;
        this.friendshipRepository = friendshipRepository;
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
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    /**
     * Asegura que un juego con el {@code igdbId} especificado exista en la base de datos local.
     * Si el juego ya existe localmente:
     * Si está marcado como parcial ({@code isFullDetails = false}), intenta obtener
     * los detalles completos de IGDB y actualizar la entidad local.
     * Si ya tiene detalles completos, devuelve la entidad existente.
     * Si el juego no existe localmente, lo obtiene de IGDB, lo guarda en la base de datos
     * local (marcado como con detalles completos) y lo devuelve.
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
        Game game = ensureGameExists(igdbId);

        // 1. Buscamos la entrada en la biblioteca y la guardamos en un Optional
        Optional<UserGame> userGameOptional = userGameRepository.findByUserAndGame(user, game);

        UserGame userGame;
        // 2. Usamos un if/else para comprobar si la entrada ya existe
        if (userGameOptional.isPresent()) {
            // Si existe, la obtenemos del Optional para actualizarla
            userGame = userGameOptional.get();
            logger.debug("Entrada UserGame encontrada (ID: {}). Se procederá a actualizar.", userGame.getInternalId());
        } else {
            // Si no existe, creamos una nueva instancia
            userGame = UserGame.builder().user(user).game(game).build();
            logger.debug("No se encontró entrada UserGame. Se creará una nueva.");
        }

        userGameMapper.updateFromDto(userGameDataDTO, userGame);
        UserGame savedUserGame = userGameRepository.save(userGame);

        // El log original para saber si se añadió o actualizó sigue siendo válido
        logger.info("Usuario {} {} el juego con IGDB ID {} (ID interno UserGame: {}) en su biblioteca.",
                userEmail, (userGameOptional.isPresent()) ? "actualizó" : "añadió",
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

        // 1. Obtener la lista de entidades desde el repositorio
        List<UserGame> userGames = userGameRepository.findByUser(user);

        // 2. Crear una nueva lista vacía para los DTOs de respuesta
        List<UserGameResponseDTO> responseDTOs = new ArrayList<>();

        // 3. Iterar sobre la lista de entidades con un bucle for-each
        for (UserGame userGame : userGames) {
            // 4. Convertir cada entidad a su DTO correspondiente
            UserGameResponseDTO dto = userGameMapper.toResponseDto(userGame);
            // 5. Añadir el DTO a la lista de respuesta
            responseDTOs.add(dto);
        }

        // 6. Devolver la nueva lista de DTOs
        return responseDTOs;
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

        // 1. Buscar la entrada de juego en la biblioteca del usuario.
        Optional<UserGame> userGameOptional = userGameRepository.findByUserAndGame_IgdbId(user, igdbId);

        // 2. Comprobar explícitamente si la entrada fue encontrada.
        if (userGameOptional.isPresent()) {
            // 3. Si se encontró, obtener la entidad.
            UserGame userGame = userGameOptional.get();
            // 4. Mapear la entidad a DTO y devolverla.
            return userGameMapper.toResponseDto(userGame);
        } else {
            // 5. Si no se encontró, lanzar la excepción.
            throw new ResourceNotFoundException(
                    "Juego con IGDB ID " + igdbId + " no encontrado en la biblioteca del usuario " + userEmail);
        }
    }

    /**
     * Obtiene una vista detallada de un juego, combinando su información general,
     * los datos específicos del usuario (si está autenticado y el juego está en su biblioteca),
     * y una lista de comentarios públicos realizados por otros usuarios sobre ese juego.
     * Si el juego existe localmente y tiene detalles completos, se usa esa información.
     * Si existe localmente pero es parcial, se obtienen los detalles completos de IGDB
     * solo para la respuesta (sin actualizar la entidad local en este flujo).
     * Si no existe localmente, se obtiene de IGDB para la respuesta.
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
        // --- Lógica para obtener la información general del juego (GameDto) ---
        Optional<Game> gameEntityOptional = gameRepository.findByIgdbId(igdbId);
        GameDto gameInfoDto;

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
                        .block();
            }
        } else {
            logger.info("Juego con IGDB ID {} no encontrado localmente. Obteniendo de IGDB para la vista.", igdbId);
            gameInfoDto = igdbService.findGameByIgdbId(igdbId)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Juego no encontrado en IGDB con ID: " + igdbId)))
                    .block();
        }

        if (gameInfoDto == null) {
            throw new ResourceNotFoundException("No se pudo obtener información del juego con IGDB ID: " + igdbId);
        }

        // --- Lógica para obtener los datos del juego del usuario (UserGameResponseDTO) ---
        UserGameResponseDTO userGameDataDto = null;
        Game gameEntityForUserAndComments = gameEntityOptional.orElse(null);

        if (userEmail != null && gameEntityForUserAndComments != null) {
            User user = getUserByEmail(userEmail); // Reutilizamos el método privado existente

            Optional<UserGame> userGameOptional = userGameRepository.findByUserAndGame(user, gameEntityForUserAndComments);
            if (userGameOptional.isPresent()) {
                UserGame userGame = userGameOptional.get();
                userGameDataDto = userGameMapper.toResponseDto(userGame);
            }
        } else if (userEmail != null) {
            logger.debug("Juego con IGDB ID {} no existe en BD local, no se pueden obtener datos de UserGame para el usuario {}.", igdbId, userEmail);
        }

        // --- Lógica para obtener los comentarios públicos ---
        List<PublicGameCommentDTO> publicComments = new ArrayList<>();
        if (gameEntityForUserAndComments != null) {
            List<UserGame> userGamesWithComments = userGameRepository.findPublicCommentsForGame(gameEntityForUserAndComments);

            for (UserGame ug : userGamesWithComments) {
                PublicGameCommentDTO commentDto = PublicGameCommentDTO.builder()
                        .username(ug.getUser().getNombreUsuario())
                        .userPublicId(ug.getUser().getPublicId())
                        .commentText(ug.getComment())
                        .commentDate(ug.getUpdatedAt())
                        .build();
                publicComments.add(commentDto);
            }
            logger.debug("Se encontraron {} comentarios públicos para el juego local con IGDB ID {}.", publicComments.size(), igdbId);
        } else {
            logger.debug("Juego con IGDB ID {} no existe en BD local, no se buscan comentarios públicos.", igdbId);
        }

        // --- Construcción del DTO final ---
        return GameDetailDTO.builder()
                .gameInfo(gameInfoDto)
                .userGameData(userGameDataDto)
                .publicComments(publicComments)
                .build();
    }

    private void initializeTierListDetails(TierList tierList) {
        if (tierList == null) return;
        Hibernate.initialize(tierList.getSections());
        if (tierList.getSections() != null) {
            for (TierSection section : tierList.getSections()) {
                Hibernate.initialize(section.getItems());
                if (section.getItems() != null) {
                    for (TierListItem item : section.getItems()) {
                        if (item.getUserGame() != null) {
                            Hibernate.initialize(item.getUserGame());
                            if (item.getUserGame().getGame() != null) {
                                Hibernate.initialize(item.getUserGame().getGame());
                                if (item.getUserGame().getGame().getCover() != null) {
                                    Hibernate.initialize(item.getUserGame().getGame().getCover());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public void removeGameFromLibrary(String userEmail, Long igdbId) {
        User user = getUserByEmail(userEmail);

        Game game = gameRepository.findByIgdbId(igdbId)
                .orElseThrow(() -> new ResourceNotFoundException("Juego con IGDB ID " + igdbId + " no encontrado en el sistema. No se puede eliminar de la biblioteca."));

        UserGame userGameToRemove = userGameRepository.findByUserAndGame(user, game)
                .orElseThrow(() -> new ResourceNotFoundException("Juego con IGDB ID " + igdbId + " no encontrado en la biblioteca del usuario " + userEmail + " para eliminar."));

        Long userGameInternalId = userGameToRemove.getInternalId();
        logger.info("INICIO PROCESO DE ELIMINACIÓN COMPLETA: UserGame ID: {} (Juego IGDB ID: {}) para el usuario: {}", userGameInternalId, igdbId, userEmail);

        // PASO 1: Eliminar de todas las GameLists del usuario.
        // Esto DEBE desencadenar la sincronización de las TierLists tipo FROM_GAMELIST asociadas
        // a través de la lógica en GameListServiceImpl.removeGameFromCustomList -> TierListService.
        logger.info("[PASO 1/3] Procesando eliminación de UserGame ID {} de las GameLists del usuario {}...", userGameInternalId, userEmail);
        // Utiliza un método de repositorio que haga JOIN FETCH de userGames para evitar N+1 o LazyInitializationException
        // Ejemplo: List<GameList> userOwnedGameLists = gameListRepository.findAllByOwnerWithUserGames(user);
        // Si no, carga individualmente o usa Hibernate.initialize. Por simplicidad, aquí obtengo todas y filtro:
        List<GameList> allUserOwnedGameLists = gameListRepository.findByOwnerOrderByUpdatedAtDesc(user); // Podría no cargar los userGames
        int gameListsProcessedCount = 0;
        for (GameList gl : allUserOwnedGameLists) {
            // Cargar explícitamente userGames si no se hizo con fetch en la query del repositorio
            Hibernate.initialize(gl.getUserGames());
            boolean isInThisList = gl.getUserGames().stream()
                    .anyMatch(ug -> ug.getInternalId().equals(userGameInternalId));

            if (isInThisList) {
                gameListsProcessedCount++;
                logger.debug("UserGame ID {} encontrado en GameList '{}' (Public ID: {}). Procediendo a eliminar y sincronizar TierList asociada (si es FROM_GAMELIST).",
                        userGameInternalId, gl.getName(), gl.getPublicId());
                try {
                    // Esta llamada es crítica. gameListService.removeGameFromCustomList DEBE
                    // asegurar que si hay una TierList FROM_GAMELIST asociada, se sincronice
                    // y el TierListItem correspondiente al userGameToRemove sea eliminado.
                    gameListService.removeGameFromCustomList(userEmail, gl.getPublicId(), userGameInternalId);
                } catch (Exception e) {
                    logger.error("Error al procesar la eliminación/sincronización para UserGame ID {} en GameList ID {}: {}. El proceso continuará.",
                            userGameInternalId, gl.getPublicId(), e.getMessage(), e);
                    // Decide si un error aquí debería detener todo. Si la sincronización es vital,
                    // propagar la excepción podría ser mejor para asegurar la consistencia.
                }
            }
        }
        logger.info("[PASO 1/3 COMPLETADO] UserGame ID {} procesado para {} GameList(s).", userGameInternalId, gameListsProcessedCount);

        // PASO 2: Eliminar de todas las TierLists de tipo PROFILE_GLOBAL del usuario.
        // Las de tipo FROM_GAMELIST deberían haberse manejado en el PASO 1.
        logger.info("[PASO 2/3] Procesando eliminación de UserGame ID {} de TierLists tipo PROFILE_GLOBAL del usuario {}...", userGameInternalId, userEmail);
        List<TierList> userProfileTierLists = tierListRepository.findByOwnerAndType(user, TierListType.PROFILE_GLOBAL);
        int profileTierListsProcessedCount = 0;

        for (TierList tl : userProfileTierLists) {
            initializeTierListDetails(tl); // Carga secciones e ítems para esta TierList

            List<TierListItem> itemsToDeleteInThisTierList = new ArrayList<>();
            for (TierSection section : tl.getSections()) {
                for (TierListItem item : section.getItems()) {
                    if (item.getUserGame() != null && Objects.equals(item.getUserGame().getInternalId(), userGameInternalId)) {
                        itemsToDeleteInThisTierList.add(item);
                    }
                }
            }

            if (!itemsToDeleteInThisTierList.isEmpty()) {
                profileTierListsProcessedCount++;
                logger.debug("UserGame ID {} encontrado en {} TierListItem(s) dentro de TierList PROFILE_GLOBAL '{}' (ID: {}). Procediendo a eliminar.",
                        userGameInternalId, itemsToDeleteInThisTierList.size(), tl.getName(), tl.getPublicId());
                for (TierListItem itemToRemove : itemsToDeleteInThisTierList) {
                    try {
                        tierListService.removeItemFromTierList(userEmail, tl.getPublicId(), itemToRemove.getInternalId());
                    } catch (Exception e) {
                        logger.error("Error al eliminar TierListItem ID {} de TierList PROFILE_GLOBAL ID {}: {}. El proceso continuará.",
                                itemToRemove.getInternalId(), tl.getPublicId(), e.getMessage(), e);
                    }
                }
            }
        }
        logger.info("[PASO 2/3 COMPLETADO] UserGame ID {} procesado para {} TierList(s) PROFILE_GLOBAL.", userGameInternalId, profileTierListsProcessedCount);

        // PASO 3: Verificación final y eliminación del UserGame de la biblioteca.
        logger.info("[PASO 3/3 - Verificación] Comprobando referencias restantes a UserGame ID {} antes de la eliminación final de la biblioteca...", userGameInternalId);
        long remainingReferencesInTierListItems = tierListItemRepository.countByUserGame(userGameToRemove);

        if (remainingReferencesInTierListItems > 0) {
            logger.error("ERROR CRÍTICO DE INTEGRIDAD DE DATOS: Después de todos los intentos de limpieza, todavía hay {} TierListItem(s) referenciando UserGame ID {}.",
                    remainingReferencesInTierListItems, userGameInternalId);

            // Log detallado de los ítems restantes
            List<TierListItem> remainingItems = tierListItemRepository.findByUserGame(userGameToRemove);
            for (TierListItem item : remainingItems) {
                TierSection section = item.getTierSection();
                TierList tierList = (section != null) ? section.getTierList() : null;
                // Cargar explícitamente para evitar LazyInitializationException en los logs
                if (section != null) Hibernate.initialize(section); // Carga la sección si es proxy
                if (tierList != null) Hibernate.initialize(tierList); // Carga la TierList si es proxy

                logger.error("TierListItem RESTANTE: ID={}, Order={}, SectionID={}, SectionName='{}', TierListPublicID={}, TierListName='{}', TierListType={}",
                        item.getInternalId(),
                        item.getItemOrder(),
                        section != null ? section.getInternalId() : "null",
                        section != null ? section.getName() : "null",
                        tierList != null ? tierList.getPublicId() : "null",
                        tierList != null ? tierList.getName() : "null",
                        tierList != null ? tierList.getType() : "null" // Esto te dirá si el problema persiste con FROM_GAMELIST
                );
            }
            // Importante: No eliminar el UserGame si aún hay referencias para evitar el error de FK
            // y poder diagnosticar el fallo en la lógica de limpieza previa.
            throw new IllegalStateException("FALLO DE LIMPIEZA PREVIA: UserGame ID " + userGameInternalId +
                    " todavía está referenciado por " + remainingReferencesInTierListItems +
                    " TierListItem(s). La eliminación de la biblioteca se aborta para prevenir error de FK y permitir diagnóstico.");
        } else {
            logger.info("[PASO 3/3 - Verificación COMPLETADA] No quedan TierListItems referenciando UserGame ID {}. Procediendo a eliminar de la biblioteca.", userGameInternalId);
            userGameRepository.delete(userGameToRemove);
            logger.info("ÉXITO FINAL: Usuario {} eliminó el juego con IGDB ID {} (UserGame ID: {}) de su biblioteca y todas sus referencias asociadas.", userEmail, igdbId, userGameInternalId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserGameResponseDTO> getPublicUserGameLibrary(UUID publicId, String currentUserEmail) {
        User targetUser = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con Public ID: " + publicId));

        VisibilidadEnum visibility = targetUser.getVisibilidadPerfil();

        switch (visibility) {
            case PUBLICO:
                // Acceso permitido para todos
                break;

            case PRIVADO:
                throw new UnauthorizedOperationException("La biblioteca de este usuario es privada.");

            case SOLO_AMIGOS:
                if (currentUserEmail == null) {
                    throw new UnauthorizedOperationException("Esta biblioteca solo es visible para amigos. Debes iniciar sesión para verla.");
                }
                User currentUser = getUserByEmail(currentUserEmail); // Reutilizamos el método privado existente
                if (currentUser.getId().equals(targetUser.getId())) {
                    // El usuario está viendo su propio perfil, lo permitimos siempre
                    break;
                }
                // Comprobamos si son amigos
                boolean areFriends = friendshipRepository
                        .findFriendshipBetweenUsersWithStatus(currentUser, targetUser, FriendshipStatus.ACCEPTED)
                        .isPresent();
                if (!areFriends) {
                    throw new UnauthorizedOperationException("Esta biblioteca solo es visible para amigos.");
                }
                break;
        }

        return userGameRepository.findByUser(targetUser).stream()
                .map(userGameMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}