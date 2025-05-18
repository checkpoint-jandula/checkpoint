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
            return gameOpt.get();
        } else {
            logger.info("Game with IGDB ID {} not found in local DB. Fetching from IGDB and saving.", igdbId);
            GameDto gameDtoFromIgdb = igdbService.findGameByIgdbId(igdbId).block();
            if (gameDtoFromIgdb == null) {
                throw new ResourceNotFoundException("Game not found in IGDB with ID: " + igdbId);
            }
            gameDtoFromIgdb.setFullDetails(true);
            List<Game> savedGames = gameService.saveGames(Collections.singletonList(gameDtoFromIgdb));
            if (savedGames.isEmpty() || savedGames.get(0) == null) {
                throw new RuntimeException("Failed to save game from IGDB with ID: " + igdbId);
            }
            return savedGames.get(0);
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
        Game gameEntity = gameRepository.findByIgdbId(igdbId).orElse(null);
        GameDto gameInfoDto;

        if (gameEntity != null) {
            gameEntity = gameService.getGameByIgdbIdOriginal(igdbId);
            gameInfoDto = gameGeneralMapper.toDto(gameEntity);
        } else {
            logger.info("Game with IGDB ID {} not found locally for detail view. Fetching from IGDB.", igdbId);
            gameInfoDto = igdbService.findGameByIgdbId(igdbId)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Game not found with IGDB ID: " + igdbId)))
                    .block();
        }

        if (gameInfoDto == null) {
            throw new ResourceNotFoundException("Game not found with IGDB ID: " + igdbId);
        }

        UserGameResponseDTO userGameDataDto = null;
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                Game gameForUserGameSearch = gameRepository.findByIgdbId(igdbId).orElse(null);
                if (gameForUserGameSearch != null) {
                    userGameDataDto = userGameRepository.findByUserAndGame(user, gameForUserGameSearch)
                            .map(userGameMapper::toResponseDto)
                            .orElse(null);
                } else {
                    logger.info("Game with IGDB ID {} not found in local DB for user-specific data for user {}", igdbId, userEmail);
                }
            }
        }

        List<PublicGameCommentDTO> publicComments = new ArrayList<>();
        // Usamos gameEntity que ya fue cargado y potencialmente enriquecido por gameService.getGameByIgdbIdOriginal()
        // o si no existe localmente, gameForComments será null.
        Game gameForComments = gameEntity; // gameEntity ya tiene la información completa si existe localmente.

        if (gameForComments != null) {
            List<UserGame> userGamesWithComments = userGameRepository.findPublicCommentsForGame(gameForComments, VisibilidadEnum.PUBLICO);
            publicComments = userGamesWithComments.stream()
                    .map(ug -> PublicGameCommentDTO.builder()
                            .username(ug.getUser().getNombreUsuario())
                            .userPublicId(ug.getUser().getPublicId())
                            .commentText(ug.getComment())
                            .commentDate(ug.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());
        } else {
            // Si gameEntity es null, significa que el juego solo se obtuvo de IGDB y no está en nuestra BD local.
            // Por lo tanto, no puede tener comentarios de UserGame asociados en nuestra BD.
            logger.info("Game with IGDB ID {} not found locally, so no public comments can be retrieved from local database.", igdbId);
        }

        return GameDetailDTO.builder()
                .gameInfo(gameInfoDto)
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