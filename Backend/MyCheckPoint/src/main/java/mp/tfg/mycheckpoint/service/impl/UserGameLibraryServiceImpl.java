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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserGameLibraryServiceImpl implements UserGameLibraryService {

    private static final Logger logger = LoggerFactory.getLogger(UserGameLibraryServiceImpl.class);

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;
    private final UserGameMapper userGameMapper;
    private final GameMapper gameGeneralMapper;
    private final GameService gameService;
    private final IgdbService igdbService;

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

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Game ensureGameExists(Long igdbId) {
        Optional<Game> gameOpt = gameRepository.findByIgdbId(igdbId);

        if (gameOpt.isPresent()) {
            Game existingGameEntity = gameOpt.get();

            // Comprobar si la ENTIDAD existente tiene todos los detalles.
            if (!existingGameEntity.isFullDetails()) {
                logger.info("Juego con IGDB ID {} existe localmente pero está marcado como parcial (entidad.isFullDetails=false). Obteniendo detalles completos de IGDB.", igdbId);

                // 1. Obtener el DTO completo de IGDB.
                GameDto gameDtoFromIgdb = igdbService.findGameByIgdbId(igdbId).block();

                if (gameDtoFromIgdb == null) {
                    logger.error("No se pudo encontrar el juego con IGDB ID {} en IGDB para completar detalles, aunque existía parcialmente.", igdbId);
                    // Considera qué hacer aquí. ¿Devolver el parcial? ¿Lanzar error?
                    // Por coherencia con el flujo deseado, es mejor lanzar un error si no se puede completar.
                    throw new ResourceNotFoundException("Juego no encontrado en IGDB con ID: " + igdbId + " para completar detalles.");
                }

                // 2. Marcar este DTO como que contiene todos los detalles.
                gameDtoFromIgdb.setFullDetails(true);

                // 3. Llamar a gameService.saveGames() para actualizar la entidad existente.
                // La lógica dentro de GameService (findOrCreateAndUpdateBaseGame y processSingleGameDto)
                // debería manejar la actualización completa de 'existingGameEntity' porque
                // gameDtoFromIgdb.isFullDetails() es true.
                // Es importante que processSingleGameDto procese TODAS las relaciones (ManyToMany, etc.)
                // cuando el DTO que recibe está marcado como isFullDetails=true.
                logger.debug("Llamando a gameService.saveGames para actualizar el juego IGDB ID {} con DTO completo.", igdbId);
                List<Game> updatedGames = gameService.saveGames(Collections.singletonList(gameDtoFromIgdb));

                if (updatedGames.isEmpty() || updatedGames.get(0) == null) {
                    logger.error("Error al actualizar el juego (IGDB ID {}) con detalles completos usando gameService.saveGames.", igdbId);
                    throw new RuntimeException("Falló la actualización del juego desde IGDB con ID: " + igdbId + " después de obtener detalles completos.");
                }

                Game fullyUpdatedGameEntity = updatedGames.get(0);
                logger.info("Juego con IGDB ID {} actualizado con detalles completos. Entidad.isFullDetails ahora es: {}", igdbId, fullyUpdatedGameEntity.isFullDetails());
                return fullyUpdatedGameEntity; // Devuelve la entidad actualizada y ahora completa.

            } else {
                // El juego existe y la entidad ya está marcada como con detalles completos.
                logger.debug("Juego con IGDB ID {} existe localmente y ya tiene detalles completos (entidad.isFullDetails=true).", igdbId);
                return existingGameEntity;
            }
        } else {
            // El juego no existe localmente, obtenerlo de IGDB y guardarlo.
            logger.info("Juego con IGDB ID {} no encontrado en BD local. Obteniendo de IGDB y guardando.", igdbId);
            GameDto gameDtoFromIgdb = igdbService.findGameByIgdbId(igdbId).block();

            if (gameDtoFromIgdb == null) {
                throw new ResourceNotFoundException("Juego no encontrado en IGDB con ID: " + igdbId);
            }

            // Al obtenerlo por primera vez directamente para añadir a la biblioteca, se asume completo.
            gameDtoFromIgdb.setFullDetails(true);

            logger.debug("Llamando a gameService.saveGames para guardar el nuevo juego IGDB ID {} con DTO completo.", igdbId);
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

    @Override
    @Transactional
    public UserGameResponseDTO addOrUpdateGameInLibrary(String userEmail, Long igdbId, UserGameDataDTO userGameDataDTO) {
        User user = getUserByEmail(userEmail);
        Game game = ensureGameExists(igdbId);

        UserGame userGame = userGameRepository.findByUserAndGame(user, game)
                .orElseGet(() -> UserGame.builder().user(user).game(game).build());

        userGameMapper.updateFromDto(userGameDataDTO, userGame);
        UserGame savedUserGame = userGameRepository.save(userGame);
        logger.info("User {} {} game with IGDB ID {} (internal UserGame ID: {}) to their library.",
                userEmail, (userGame.getInternalId() == null || userGame.getInternalId().equals(0L)) ? "added" : "updated", igdbId, savedUserGame.getInternalId());
        return userGameMapper.toResponseDto(savedUserGame);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserGameResponseDTO> getUserGameLibrary(String userEmail) {
        User user = getUserByEmail(userEmail);
        return userGameRepository.findByUser(user).stream()
                .map(userGameMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserGameResponseDTO getUserGameFromLibrary(String userEmail, Long igdbId) {
        User user = getUserByEmail(userEmail);
        return userGameRepository.findByUserAndGame_IgdbId(user, igdbId)
                .map(userGameMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Game with IGDB ID " + igdbId + " not found in library for user " + userEmail));
    }

    @Override
    @Transactional(readOnly = true)
    public GameDetailDTO getGameDetailsForUser(Long igdbId, String userEmail) {
        Optional<Game> gameEntityOptional = gameRepository.findByIgdbId(igdbId);
        GameDto gameInfoDto;
        Game gameEntityForUserAndComments = gameEntityOptional.orElse(null); // Usar para userGameData y comments

        if (gameEntityOptional.isPresent()) {
            Game localGameEntity = gameEntityOptional.get();
            logger.debug("Juego con IGDB ID {} encontrado localmente. FullDetails: {}", igdbId, localGameEntity.isFullDetails());

            if (localGameEntity.isFullDetails()) {
                // El juego existe localmente y TIENE detalles completos.
                logger.debug("Usando datos completos locales para el gameInfo del juego IGDB ID {}.", igdbId);
                // gameService.getGameByIgdbIdOriginal carga la entidad y sus colecciones LAZY.
                Game fullyLoadedLocalGame = gameService.getGameByIgdbIdOriginal(igdbId); // Puede devolver null si no se encuentra, aunque ya hicimos findByIgdbId
                if (fullyLoadedLocalGame == null) { // Salvaguarda
                    throw new ResourceNotFoundException("Error al cargar la entidad local completa para el juego IGDB ID: " + igdbId);
                }
                gameInfoDto = gameGeneralMapper.toDto(fullyLoadedLocalGame);
            } else {
                // El juego existe localmente PERO está marcado como PARCIAL (isFullDetails = false).
                // Obtener los detalles completos de IGDB para la respuesta, SIN GUARDARLOS en la BDD local.
                logger.info("Juego IGDB ID {} existe localmente pero es parcial. Obteniendo detalles completos de IGDB solo para la vista (sin guardar).", igdbId);
                gameInfoDto = igdbService.findGameByIgdbId(igdbId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Juego no encontrado en IGDB con ID: " + igdbId + " al intentar obtener detalles completos para la vista.")))
                        .block();
                // En este caso, gameInfoDto viene de IGDB. La entidad local (localGameEntity o gameEntityForUserAndComments) sigue siendo parcial.
            }
        } else {
            // El juego NO existe localmente. Obtener de IGDB.
            logger.info("Juego con IGDB ID {} no encontrado localmente. Obteniendo de IGDB para la vista.", igdbId);
            gameInfoDto = igdbService.findGameByIgdbId(igdbId)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Juego no encontrado en IGDB con ID: " + igdbId)))
                    .block();
            // gameEntityForUserAndComments ya es null en este caso.
        }

        if (gameInfoDto == null) {
            // Si después de todas las lógicas, gameInfoDto sigue siendo null (ej. no se encontró ni localmente ni en IGDB).
            throw new ResourceNotFoundException("No se pudo obtener información del juego con IGDB ID: " + igdbId);
        }

        // --- Obtener datos específicos del usuario y comentarios públicos ---
        // Estos siempre se basan en la existencia del juego en la BDD local (gameEntityForUserAndComments)

        UserGameResponseDTO userGameDataDto = null;
        if (userEmail != null && gameEntityForUserAndComments != null) { // Solo si el juego existe localmente
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

        } else if (userEmail != null && gameEntityForUserAndComments == null) {
            logger.debug("Juego con IGDB ID {} no existe en BD local, no se pueden obtener datos de UserGame para el usuario {}.", igdbId, userEmail);
        }

        List<PublicGameCommentDTO> publicComments = Collections.emptyList(); // Inicializar a lista vacía
        if (gameEntityForUserAndComments != null) {
            List<UserGame> userGamesWithComments = userGameRepository.findPublicCommentsForGame(gameEntityForUserAndComments, VisibilidadEnum.PUBLICO);
            publicComments = userGamesWithComments.stream()
                    .map(ug -> PublicGameCommentDTO.builder()
                            .username(ug.getUser().getNombreUsuario())
                            .userPublicId(ug.getUser().getPublicId())
                            .commentText(ug.getComment())
                            .commentDate(ug.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());
            logger.debug("Se encontraron {} comentarios públicos para el juego local con IGDB ID {}.", publicComments.size(), igdbId);
        } else {
            logger.debug("Juego con IGDB ID {} no existe en BD local, no se buscan comentarios públicos.", igdbId);
        }

        return GameDetailDTO.builder()
                .gameInfo(gameInfoDto) // Este es el DTO que viene de la entidad local completa o de IGDB directamente
                .userGameData(userGameDataDto)
                .publicComments(publicComments)
                .build();
    }

    @Override
    @Transactional
    public void removeGameFromLibrary(String userEmail, Long igdbId) {
        User user = getUserByEmail(userEmail);
        if (!userGameRepository.existsByUserAndGame_IgdbId(user, igdbId)) {
            throw new ResourceNotFoundException("Game with IGDB ID " + igdbId + " not found in library for user " + userEmail + " to delete.");
        }
        userGameRepository.deleteByUserAndGame_IgdbId(user, igdbId);
        logger.info("User {} removed game with IGDB ID {} from their library.", userEmail, igdbId);
    }
}