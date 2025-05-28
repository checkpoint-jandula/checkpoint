package mp.tfg.mycheckpoint.service.impl;

import mp.tfg.mycheckpoint.dto.gameList.GameListRequestDTO;
import mp.tfg.mycheckpoint.dto.gameList.GameListResponseDTO;
import mp.tfg.mycheckpoint.entity.GameList;
import mp.tfg.mycheckpoint.entity.User;
import mp.tfg.mycheckpoint.entity.UserGame;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.exception.UnauthorizedOperationException;
import mp.tfg.mycheckpoint.mapper.gameList.GameListMapper;
import mp.tfg.mycheckpoint.repository.GameListRepository;
import mp.tfg.mycheckpoint.repository.UserGameRepository;
import mp.tfg.mycheckpoint.repository.UserRepository;
import mp.tfg.mycheckpoint.service.GameListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameListServiceImpl implements GameListService {

    private static final Logger logger = LoggerFactory.getLogger(GameListServiceImpl.class);

    private final UserRepository userRepository;
    private final GameListRepository gameListRepository;
    private final UserGameRepository userGameRepository; // Para encontrar el UserGame a añadir
    private final GameListMapper gameListMapper;

    @Autowired
    public GameListServiceImpl(UserRepository userRepository,
                               GameListRepository gameListRepository,
                               UserGameRepository userGameRepository,
                               GameListMapper gameListMapper) {
        this.userRepository = userRepository;
        this.gameListRepository = gameListRepository;
        this.userGameRepository = userGameRepository;
        this.gameListMapper = gameListMapper;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public GameListResponseDTO createGameList(String userEmail, GameListRequestDTO requestDTO) {
        User owner = getUserByEmail(userEmail);
        GameList gameList = gameListMapper.toEntity(requestDTO);
        gameList.setOwner(owner);
        // gameList.setPublicId(UUID.randomUUID()); // Se genera en @PrePersist
        GameList savedGameList = gameListRepository.save(gameList);
        logger.info("User {} created GameList '{}' (Public ID: {})", userEmail, savedGameList.getName(), savedGameList.getPublicId());
        return gameListMapper.toResponseDto(savedGameList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameListResponseDTO> getAllGameListsForUser(String userEmail) {
        User owner = getUserByEmail(userEmail);
        // Para asegurar que userGames se carga, podrías necesitar un método de repo con JOIN FETCH
        // o confiar en que la sesión de Hibernate siga abierta o que el mapper lo maneje bien.
        // Si usas el mapper con `uses = {UserGameMapper.class}` y las entidades están gestionadas, debería funcionar.
        return gameListRepository.findByOwnerOrderByUpdatedAtDesc(owner).stream()
                .map(gameList -> {
                    // Carga explícita si es necesario y no se usó FETCH en el repo.
                    // Hibernate.initialize(gameList.getUserGames());
                    return gameListMapper.toResponseDto(gameList);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GameListResponseDTO getGameListByPublicIdForUser(String userEmail, UUID listPublicId) {
        User owner = getUserByEmail(userEmail);
        GameList gameList = gameListRepository.findByPublicIdAndOwnerWithGames(listPublicId, owner)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GameList with Public ID " + listPublicId + " not found for user " + userEmail));
        return gameListMapper.toResponseDto(gameList);
    }

    @Override
    @Transactional(readOnly = true)
    public GameListResponseDTO getPublicGameListByPublicId(UUID listPublicId) {
        GameList gameList = gameListRepository.findByPublicIdAndIsPublicTrueWithGames(listPublicId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Public GameList with Public ID " + listPublicId + " not found or is not public."));
        return gameListMapper.toResponseDto(gameList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameListResponseDTO> getAllPublicGameLists() {
        return gameListRepository.findByIsPublicTrueOrderByUpdatedAtDesc().stream()
                .map(gameList -> {
                    // Opcional: Cargar userGames si no se hizo con JOIN FETCH en el repositorio
                    // Hibernate.initialize(gameList.getUserGames());
                    // Si el método del repositorio es simple y no usa JOIN FETCH,
                    // y necesitas los juegos, una opción es volver a buscar cada lista
                    // con un método que sí haga JOIN FETCH o cargar la colección aquí.
                    // Por simplicidad, asumimos que si se necesitan los juegos se usa un método de repo con JOIN FETCH.
                    // Si se usa un repo simple, gameList.getUserGames() podría estar vacío o causar LazyInitException
                    // si se accede fuera de la transacción.
                    // Alternativa:
                    // GameList fetchedList = gameListRepository.findByPublicIdAndIsPublicTrueWithGames(gameList.getPublicId())
                    //                                          .orElse(gameList); // Fallback a la lista original si no se encuentra (no debería pasar)
                    // return gameListMapper.toResponseDto(fetchedList);
                    return gameListMapper.toResponseDto(gameList); // Asume que el mapeo maneja la carga LAZY o el repo la hizo.
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public GameListResponseDTO updateGameList(String userEmail, UUID listPublicId, GameListRequestDTO requestDTO) {
        User owner = getUserByEmail(userEmail);
        GameList gameList = gameListRepository.findByPublicIdAndOwner(listPublicId, owner)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GameList with Public ID " + listPublicId + " not found for user " + userEmail));

        gameListMapper.updateFromDto(requestDTO, gameList);
        GameList updatedGameList = gameListRepository.save(gameList);
        logger.info("User {} updated GameList '{}' (Public ID: {})", userEmail, updatedGameList.getName(), updatedGameList.getPublicId());
        return gameListMapper.toResponseDto(updatedGameList);
    }

    @Override
    @Transactional
    public void deleteGameList(String userEmail, UUID listPublicId) {
        User owner = getUserByEmail(userEmail);
        GameList gameList = gameListRepository.findByPublicIdAndOwner(listPublicId, owner)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GameList with Public ID " + listPublicId + " not found for user " + userEmail));

        // La relación ManyToMany con UserGame se eliminará de la tabla de unión
        // pero las entidades UserGame no se eliminarán.
        gameListRepository.delete(gameList);
        logger.info("User {} deleted GameList '{}' (Public ID: {})", userEmail, gameList.getName(), listPublicId);
    }

    @Override
    @Transactional
    public GameListResponseDTO addGameToCustomList(String userEmail, UUID listPublicId, Long userGameInternalId) {
        User owner = getUserByEmail(userEmail);
        GameList gameList = gameListRepository.findByPublicIdAndOwnerWithGames(listPublicId, owner) // Cargar con juegos
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GameList with Public ID " + listPublicId + " not found for user " + userEmail));

        UserGame userGame = userGameRepository.findById(userGameInternalId)
                .orElseThrow(() -> new ResourceNotFoundException("UserGame entry with ID " + userGameInternalId + " not found."));

        // Verificar que el UserGame pertenece al mismo usuario que es dueño de la lista
        if (!userGame.getUser().getId().equals(owner.getId())) {
            throw new UnauthorizedOperationException(
                    "Cannot add game to list: UserGame entry does not belong to the owner of the list.");
        }

        if (gameList.getUserGames().contains(userGame)) {
            logger.warn("Game (UserGame ID: {}) is already in list '{}' (Public ID: {}). No action taken.",
                    userGameInternalId, gameList.getName(), listPublicId);
            // Devolver la lista tal cual o un mensaje/error específico
            return gameListMapper.toResponseDto(gameList);
        }

        gameList.getUserGames().add(userGame);
        GameList updatedGameList = gameListRepository.save(gameList); // Guardar para persistir la asociación
        logger.info("Added game (UserGame ID: {}) to list '{}' (Public ID: {}) for user {}",
                userGameInternalId, updatedGameList.getName(), listPublicId, userEmail);
        return gameListMapper.toResponseDto(updatedGameList);
    }

    @Override
    @Transactional
    public void removeGameFromCustomList(String userEmail, UUID listPublicId, Long userGameInternalId) {
        User owner = getUserByEmail(userEmail);
        GameList gameList = gameListRepository.findByPublicIdAndOwnerWithGames(listPublicId, owner) // Cargar con juegos
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GameList with Public ID " + listPublicId + " not found for user " + userEmail));

        UserGame userGameToRemove = userGameRepository.findById(userGameInternalId)
                .orElseThrow(() -> new ResourceNotFoundException("UserGame entry with ID " + userGameInternalId + " not found."));

        // No es estrictamente necesario verificar pertenencia aquí si solo se remueve, pero es buena práctica
        if (!userGameToRemove.getUser().getId().equals(owner.getId())) {
            throw new UnauthorizedOperationException(
                    "Cannot remove game from list: UserGame entry does not belong to the owner of the list.");
        }

        boolean removed = gameList.getUserGames().remove(userGameToRemove);
        if (removed) {
            gameListRepository.save(gameList); // Guardar para persistir la eliminación de la asociación
            logger.info("Removed game (UserGame ID: {}) from list '{}' (Public ID: {}) for user {}",
                    userGameInternalId, gameList.getName(), listPublicId, userEmail);
        } else {
            logger.warn("Game (UserGame ID: {}) was not found in list '{}' (Public ID: {}). No action taken.",
                    userGameInternalId, gameList.getName(), listPublicId);
            // Puedes lanzar una excepción o simplemente no hacer nada si no estaba.
            // throw new ResourceNotFoundException("Game (UserGame ID: " + userGameInternalId + ") not in list " + listPublicId);
        }
    }
}
