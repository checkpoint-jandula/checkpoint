package mp.tfg.mycheckpoint.service.impl;

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
import mp.tfg.mycheckpoint.service.games.GameService; // Para asegurar que el juego existe
import mp.tfg.mycheckpoint.service.games.IgdbService;   // Para obtener de IGDB si no existe
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono; // Para manejar la respuesta de IgdbService

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
    private final GameMapper gameGeneralMapper; // Mapper general para Game a GameDto
    private final GameService gameService; // Para sincronizar juegos desde IGDB
    private final IgdbService igdbService; // Para buscar en IGDB

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
            return gameOpt.get();
        } else {
            logger.info("Game with IGDB ID {} not found in local DB. Fetching from IGDB and saving.", igdbId);
            GameDto gameDtoFromIgdb = igdbService.findGameByIgdbId(igdbId).block(); // Bloqueante, considera alternativas si estás en un contexto totalmente reactivo.
            if (gameDtoFromIgdb == null) {
                throw new ResourceNotFoundException("Game not found in IGDB with ID: " + igdbId);
            }
            gameDtoFromIgdb.setFullDetails(true); // Marcar como completo para GameService
            List<Game> savedGames = gameService.saveGames(Collections.singletonList(gameDtoFromIgdb));
            if (savedGames.isEmpty()) {
                throw new RuntimeException("Failed to save game from IGDB with ID: " + igdbId);
            }
            return savedGames.get(0);
        }
    }

    @Override
    @Transactional
    public UserGameResponseDTO addOrUpdateGameInLibrary(String userEmail, Long igdbId, UserGameDataDTO userGameDataDTO) {
        User user = getUserByEmail(userEmail);
        Game game = ensureGameExists(igdbId); // Asegura que el juego esté en nuestra BD

        UserGame userGame = userGameRepository.findByUserAndGame(user, game)
                .orElseGet(() -> UserGame.builder().user(user).game(game).build());

        userGameMapper.updateFromDto(userGameDataDTO, userGame);
        UserGame savedUserGame = userGameRepository.save(userGame);
        logger.info("User {} {} game with IGDB ID {} (internal UserGame ID: {}) to their library.",
                userEmail, userGame.getInternalId() == null ? "added" : "updated", igdbId, savedUserGame.getInternalId());
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
        // Game game = gameRepository.findByIgdbId(igdbId)
        //        .orElseThrow(() -> new ResourceNotFoundException("Game with IGDB ID " + igdbId + " not found in local DB. Cannot fetch library data."));
        // No necesitamos buscar Game primero si UserGameRepository puede buscar por user y game_igdb_id
        return userGameRepository.findByUserAndGame_IgdbId(user, igdbId)
                .map(userGameMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Game with IGDB ID " + igdbId + " not found in library for user " + userEmail));
    }

    @Override
    @Transactional(readOnly = true)
    public GameDetailDTO getGameDetailsForUser(Long igdbId, String userEmail) {
        // 1. Obtener información general del juego
        Game gameEntity = gameRepository.findByIgdbId(igdbId).orElse(null);
        GameDto gameInfoDto;

        if (gameEntity != null) {
            // Si está en nuestra BD, lo mapeamos. GameService se encarga de inicializar LAZY props.
            gameEntity = gameService.getGameByIgdbIdOriginal(igdbId); // Asegura inicialización de colecciones
            gameInfoDto = gameGeneralMapper.toDto(gameEntity);
        } else {
            // Si no está en nuestra BD, lo buscamos en IGDB (sin guardarlo aquí, solo para visualización)
            logger.info("Game with IGDB ID {} not found locally for detail view. Fetching from IGDB.", igdbId);
            gameInfoDto = igdbService.findGameByIgdbId(igdbId)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Game not found with IGDB ID: " + igdbId)))
                    .block(); // De nuevo, block() puede ser un problema en un flujo full reactivo.
            // Si se obtiene de IGDB, no se guarda automáticamente aquí, solo se usa para el DTO.
            // Si quisieras guardarlo siempre que se consulta, llamarías a gameService.saveGames.
        }

        if (gameInfoDto == null) { // Doble chequeo por si IGDB tampoco lo devuelve
            throw new ResourceNotFoundException("Game not found with IGDB ID: " + igdbId);
        }


        // 2. Obtener información específica del usuario si está logueado
        UserGameResponseDTO userGameDataDto = null;
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                // Si el juego general se obtuvo de IGDB y no está en gameRepository, gameEntity sería null.
                // Necesitamos asegurar que tenemos una entidad Game para la búsqueda de UserGame.
                // Si gameEntity es null aquí, significa que el juego se obtuvo directamente de IGDB
                // y no se persistió (o aún no) en este flujo.
                // Si el flujo requiere que el 'Game' exista para añadir a UserGame, ensureGameExists se encargaría.
                // Para solo *ver* UserGame, el Game debe existir en nuestra BD.
                Game gameForUserGameSearch = gameRepository.findByIgdbId(igdbId).orElse(null);
                if (gameForUserGameSearch != null) {
                    userGameDataDto = userGameRepository.findByUserAndGame(user, gameForUserGameSearch)
                            .map(userGameMapper::toResponseDto)
                            .orElse(null); // Es null si no está en la biblioteca
                } else {
                    logger.info("Game with IGDB ID {} not found in local DB, so no user-specific data can be retrieved for user {}", igdbId, userEmail);
                }
            }
        }

        return GameDetailDTO.builder()
                .gameInfo(gameInfoDto)
                .userGameData(userGameDataDto)
                .build();
    }


    @Override
    @Transactional
    public void removeGameFromLibrary(String userEmail, Long igdbId) {
        User user = getUserByEmail(userEmail);
        // No es necesario buscar Game primero si el repositorio lo permite
        if (!userGameRepository.existsByUserAndGame_IgdbId(user, igdbId)) {
            throw new ResourceNotFoundException("Game with IGDB ID " + igdbId + " not found in library for user " + userEmail + " to delete.");
        }
        userGameRepository.deleteByUserAndGame_IgdbId(user, igdbId);
        logger.info("User {} removed game with IGDB ID {} from their library.", userEmail, igdbId);
    }
}
