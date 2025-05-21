package mp.tfg.mycheckpoint.service.games;


import jakarta.transaction.Transactional;
import mp.tfg.mycheckpoint.dto.enums.GameType;
import mp.tfg.mycheckpoint.dto.enums.ReleaseStatus;
import mp.tfg.mycheckpoint.dto.games.*;
import mp.tfg.mycheckpoint.entity.games.*;
import mp.tfg.mycheckpoint.mapper.games.*;
import mp.tfg.mycheckpoint.repository.games.*;
import org.hibernate.Hibernate; // IMPORTANTE
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private final GameRepository gameRepository;
    private final GameModeRepository gameModeRepository;
    private final GenreRepository genreRepository;
    private final FranchiseRepository franchiseRepository;
    private final GameEngineRepository gameEngineRepository;
    private final KeywordRepository keywordRepository;
    private final PlatformRepository platformRepository;
    private final ThemeRepository themeRepository;
    private final CompanyRepository companyRepository;
    private final GameCompanyInvolvementRepository gameCompanyInvolvementRepository;

    private final GameMapper gameMapper;
    private final GameModeMapper gameModeMapper;
    private final GenreMapper genreMapper;
    private final FranchiseMapper franchiseMapper;
    private final GameEngineMapper gameEngineMapper;
    private final KeywordMapper keywordMapper;
    private final PlatformMapper platformMapper;
    private final PlatformLogoMapper platformLogoMapper;
    private final ThemeMapper themeMapper;
    private final CompanyMapper companyMapper;
    private final InvolvedCompanyMapper involvedCompanyMapper;

    private final CoverMapper coverMapper;
    private final ArtworkMapper artworkMapper;
    private final ScreenshotMapper screenshotMapper;
    private final VideoMapper videoMapper;
    private final WebsiteMapper websiteMapper;


    @Autowired
    public GameService(GameRepository gameRepository, GameModeRepository gameModeRepository,
                       GenreRepository genreRepository, FranchiseRepository franchiseRepository,
                       GameEngineRepository gameEngineRepository, KeywordRepository keywordRepository,
                       PlatformRepository platformRepository,PlatformLogoMapper platformLogoMapper, ThemeRepository themeRepository,
                       CompanyRepository companyRepository, GameCompanyInvolvementRepository gameCompanyInvolvementRepository,
                       GameMapper gameMapper, GameModeMapper gameModeMapper, GenreMapper genreMapper,
                       FranchiseMapper franchiseMapper, GameEngineMapper gameEngineMapper,
                       KeywordMapper keywordMapper, PlatformMapper platformMapper, ThemeMapper themeMapper,
                       CompanyMapper companyMapper, InvolvedCompanyMapper involvedCompanyMapper,
                       CoverMapper coverMapper, ArtworkMapper artworkMapper, ScreenshotMapper screenshotMapper,
                       VideoMapper videoMapper, WebsiteMapper websiteMapper) {
        this.gameRepository = gameRepository;
        this.gameModeRepository = gameModeRepository;
        this.genreRepository = genreRepository;
        this.franchiseRepository = franchiseRepository;
        this.gameEngineRepository = gameEngineRepository;
        this.keywordRepository = keywordRepository;
        this.platformRepository = platformRepository;
        this.themeRepository = themeRepository;
        this.companyRepository = companyRepository;
        this.gameCompanyInvolvementRepository = gameCompanyInvolvementRepository;
        this.gameMapper = gameMapper;
        this.gameModeMapper = gameModeMapper;
        this.genreMapper = genreMapper;
        this.franchiseMapper = franchiseMapper;
        this.gameEngineMapper = gameEngineMapper;
        this.keywordMapper = keywordMapper;
        this.platformMapper = platformMapper;
        this.platformLogoMapper = platformLogoMapper;
        this.themeMapper = themeMapper;
        this.companyMapper = companyMapper;
        this.involvedCompanyMapper = involvedCompanyMapper;
        this.coverMapper = coverMapper;
        this.artworkMapper = artworkMapper;
        this.screenshotMapper = screenshotMapper;
        this.videoMapper = videoMapper;
        this.websiteMapper = websiteMapper;
    }

    @Transactional
    public List<Game> saveGames(List<GameDto> gameDtos) {
        List<Game> processedGames = new ArrayList<>();
        if (gameDtos == null) return processedGames;
        for (GameDto gameDto : gameDtos) {
            if (gameDto != null && gameDto.getIgdbId() != null) {
                // Si gameDto.isFullDetails() no está seteado explícitamente antes de llamar a saveGames,
                // y este es un punto de entrada genérico para guardar, es razonable asumir
                // que el DTO pretende ser completo. GameDto ya se inicializa con isFullDetails = true.
                // gameDto.setFullDetails(true); // Ya no es necesario aquí si el DTO lo tiene por defecto.
                Game processedGameEntity = processSingleGameDto(gameDto, null);
                if (processedGameEntity != null) {
                    processedGames.add(processedGameEntity);
                } else {
                    logger.warn("processSingleGameDto devolvió null para GameDto IGDB ID: {}", gameDto.getIgdbId());
                }
            } else if (gameDto != null) {
                logger.warn("GameDto con IGDB ID nulo: {}", gameDto.getName());
            } else {
                logger.warn("GameDto nulo en la lista.");
            }
        }
        return processedGames;
    }

    private Game processSingleGameDto(GameDto gameDto, Game prospectiveParentGameEntity) {
        if (gameDto == null || gameDto.getIgdbId() == null) {
            logger.warn("processSingleGameDto: GameDto nulo o sin IgdbId. Saltando.");
            return null;
        }

        logger.debug("Inicio procesando GameDto: ID={}, Name={}, isFullDetails={}, ProspectiveParentIGDBID={}",
                gameDto.getIgdbId(), gameDto.getName(), gameDto.isFullDetails(),
                (prospectiveParentGameEntity != null ? prospectiveParentGameEntity.getIgdbId() : "null"));

        // 1. Encontrar o crear la entidad base del juego y guardarla inicialmente
        Game currentGameEntity = findOrCreateAndUpdateBaseGame(gameDto, prospectiveParentGameEntity);
        if (currentGameEntity == null) { // No debería ocurrir si gameDto e igdbId no son nulos
            logger.error("Error: findOrCreateBaseGame devolvió null para GameDto IGDB ID: {}", gameDto.getIgdbId());
            return null;
        }

        // Guardamos aquí para asegurar que la entidad está gestionada y tiene un ID interno
        // antes de procesar relaciones más complejas que podrían depender de ello.
        try {
            currentGameEntity = gameRepository.save(currentGameEntity);
        } catch (Exception e) {
            logger.error("Error guardando la entidad base del juego (IGDB ID: {}) : {}", gameDto.getIgdbId(), e.getMessage(), e);
            throw e; // Relanzar para que la transacción haga rollback si es necesario
        }
        final Game managedGameEntity = currentGameEntity; // Ahora está gestionada

        // 2. Procesar relaciones jerárquicas (juego padre, versión padre)
        // Estas relaciones pueden implicar llamadas recursivas a processSingleGameDto,
        // por lo que es bueno tener la entidad actual ya guardada y gestionada.
        processParentAndVersionRelationships(gameDto, managedGameEntity);

        // 3. Si el DTO tiene todos los detalles, procesar colecciones y compañías involucradas
        if (gameDto.isFullDetails()) {
            logger.debug("DTO completo (ID: {}), procesando colecciones detalladas.", managedGameEntity.getIgdbId());
            processAssociatedManyToManyCollections(gameDto, managedGameEntity);
            processInvolvedCompanies(gameDto, managedGameEntity);
        } else {
            logger.debug("DTO parcial (ID: {}), se omite procesamiento de colecciones detalladas.", managedGameEntity.getIgdbId());
        }

        // 4. Guardar la entidad principal después de establecer relaciones ManyToMany y OneToMany (como InvolvedCompanies)
        // Esto asegura que los cambios en las colecciones se persistan.
        Game savedMainGameEntity;
        try {
            savedMainGameEntity = gameRepository.save(managedGameEntity);
        } catch (Exception e) {
            logger.error("Error guardando managedGameEntity (IGDB ID: {}) después de procesar colecciones: {}", gameDto.getIgdbId(), e.getMessage(), e);
            throw e;
        }

        // 5. Procesar listas de juegos hijos (DLCs, expansiones, bundles) y relacionados (remakes, remasters, similares)
        // Estas operaciones pueden implicar más llamadas a processSingleGameDto para esas entidades relacionadas
        // y luego establecer la relación con 'savedMainGameEntity'.
        processChildGameLists(gameDto, savedMainGameEntity);
        processRelatedGameLists(gameDto, savedMainGameEntity); // Incluye remakes, remasters, similar_games

        // 6. Guardado final para persistir cualquier cambio en las relaciones de listas (como similarGames, childGames etc.)
        Game finalSavedEntity;
        try {
            finalSavedEntity = gameRepository.save(savedMainGameEntity);
        } catch (Exception e) {
            logger.error("Error en el guardado final del juego (IGDB ID: {}): {}",
                    (savedMainGameEntity != null ? savedMainGameEntity.getIgdbId() : gameDto.getIgdbId()),
                    e.getMessage(), e);
            throw e;
        }

        // 7. Inicializar colecciones LAZY antes de devolver la entidad (si es necesario para el llamador)
        if (finalSavedEntity != null) {
            initializeLazyCollections(finalSavedEntity);
        }

        logger.debug("Fin procesando GameDto: ID={}", gameDto.getIgdbId());
        return finalSavedEntity;
    }

    private Game findOrCreateAndUpdateBaseGame(GameDto gameDto, Game prospectiveParentGameEntity) {
        // Buscar si el juego ya existe en la base de datos por su IGDB ID.
        Optional<Game> existingGameOptional = gameRepository.findByIgdbId(gameDto.getIgdbId());

        Game gameToProcess; // Esta será la entidad que se devolverá, ya sea existente o nueva.

        if (existingGameOptional.isPresent()) {
            // El juego ya existe, así que lo vamos a actualizar.
            Game existingGame = existingGameOptional.get();
            logger.debug("Juego existente IGDB ID: {}. Actualizando. isFullDetails DTO: {}, isFullDetails Entidad: {}",
                    gameDto.getIgdbId(), gameDto.isFullDetails(), existingGame.isFullDetails());

            if (gameDto.isFullDetails()) {
                // El DTO entrante se considera completo, por lo que aplicamos una actualización completa
                // de los campos básicos y las listas de elementos embebidos usando el mapper.
                logger.debug("GameDto (ID: {}) es completo. Actualización completa.", gameDto.getIgdbId());
                gameMapper.updateFromDto(gameDto, existingGame); // MapStruct actualiza 'existingGame' en el sitio.

                // Si la entidad existente no estaba marcada como completa, y el DTO sí lo está,
                // actualizamos la marca en la entidad.
                if (!existingGame.isFullDetails()) {
                    // Asumiendo que tu entidad Game tiene un campo boolean isFullDetails y su setter
                     existingGame.setFullDetails(true); // DESCOMENTA SI TIENES ESTE CAMPO Y SETTER EN LA ENTIDAD GAME
                    logger.debug("Entidad Game (ID: {}) marcada como isFullDetails = true (si el campo existe).", existingGame.getIgdbId());
                }
            } else {
                // El DTO entrante es parcial (isFullDetails = false), así que solo actualizamos
                // los campos básicos definidos en updateSelectiveFields.
                // No cambiamos el estado de existingGame.isFullDetails basado en un DTO parcial.
                logger.debug("GameDto (ID: {}) es parcial. Actualización selectiva.", gameDto.getIgdbId());
                updateSelectiveFields(gameDto, existingGame);
            }

            // Establecer el estado de lanzamiento (ReleaseStatus) basado en el DTO.
            setFirstReleaseStatusFromDto(gameDto, existingGame);

            // Asignar el 'prospectiveParentGameEntity' si es necesario:
            // - Si se proporcionó un 'prospectiveParentGameEntity'.
            // - Y el juego existente no tiene un padre O su padre actual es diferente al prospectivo.
            if (prospectiveParentGameEntity != null &&
                    (existingGame.getParentGame() == null ||
                            !existingGame.getParentGame().getIgdbId().equals(prospectiveParentGameEntity.getIgdbId()))) {
                existingGame.setParentGame(prospectiveParentGameEntity);
                logger.debug("Asignado prospectiveParentGameEntity (ID: {}) al juego existente (ID: {}).",
                        prospectiveParentGameEntity.getIgdbId(), existingGame.getIgdbId());
            }
            gameToProcess = existingGame;

        } else {
            // El juego no existe en la base de datos, así que creamos uno nuevo.
            logger.debug("Creando nuevo juego para IGDB ID: {}", gameDto.getIgdbId());
            Game newGame = gameMapper.toEntity(gameDto); // Mapea los campos básicos del DTO a una nueva entidad.

            // Establecer el estado de 'isFullDetails' de la nueva entidad basado en el DTO.
            // Asumiendo que tu entidad Game tiene un campo boolean isFullDetails y su setter
            // newGame.setFullDetails(gameDto.isFullDetails()); // DESCOMENTA SI TIENES ESTE CAMPO Y SETTER EN LA ENTIDAD GAME
            logger.debug("Nueva entidad Game (ID: {}) creada con isFullDetails = {} (si el campo existe).",
                    newGame.getIgdbId(), gameDto.isFullDetails());


            // Establecer el estado de lanzamiento (ReleaseStatus) para el nuevo juego.
            setFirstReleaseStatusFromDto(gameDto, newGame);

            // Si se proporcionó un 'prospectiveParentGameEntity', asignarlo al nuevo juego.
            if (prospectiveParentGameEntity != null) {
                newGame.setParentGame(prospectiveParentGameEntity);
                logger.debug("Asignado prospectiveParentGameEntity (ID: {}) al nuevo juego (ID: {}).",
                        prospectiveParentGameEntity.getIgdbId(), newGame.getIgdbId());
            }
            gameToProcess = newGame;
        }

        return gameToProcess; // Devolver la entidad (existente actualizada o nueva).
    }

    private void processParentAndVersionRelationships(GameDto gameDto, Game managedGameEntity) {
        // Procesar Parent Game (si no se asignó como 'prospectiveParentGameEntity')
        if (gameDto.getParentGameInfo() != null && managedGameEntity.getParentGame() == null) {
            DlcInfoDto parentDto = gameDto.getParentGameInfo();
            if (parentDto.getIgdbId() != null && !parentDto.getIgdbId().equals(managedGameEntity.getIgdbId())) {
                GameDto parentAsGameDto = convertDlcInfoToGameDto(parentDto);
                if (parentAsGameDto != null) {
                    Game parentEntity = processSingleGameDto(parentAsGameDto, null); // El padre no tiene un padre prospectivo en este contexto
                    if (parentEntity != null) {
                        managedGameEntity.setParentGame(parentEntity);
                    }
                }
            }
        }

        // Procesar Version Parent
        if (gameDto.getVersionParent() != null && managedGameEntity.getVersionParentGame() == null) {
            DlcInfoDto vpDto = gameDto.getVersionParent();
            if (vpDto.getIgdbId() != null && !vpDto.getIgdbId().equals(managedGameEntity.getIgdbId())) {
                GameDto vpAsGameDto = convertDlcInfoToGameDto(vpDto);
                if (vpAsGameDto != null) {
                    Game vpEntity = processSingleGameDto(vpAsGameDto, null);
                    if (vpEntity != null) {
                        managedGameEntity.setVersionParentGame(vpEntity);
                    }
                }
            }
        }
    }

    private void processChildGameLists(GameDto gameDto, Game parentGame) {
        processChildGameList(gameDto.getDlcs(), parentGame, "DLCs");
        processChildGameList(gameDto.getExpansions(), parentGame, "Expansions");
        processChildGameList(gameDto.getBundles(), parentGame, "Bundles");
    }

    private void processRelatedGameLists(GameDto gameDto, Game mainGame) {
        // Remakes
        if (gameDto.getRemakes() != null) {
            updateRelatedGameCollection(
                    gameDto.getRemakes().stream()
                            .map(this::convertDlcInfoToGameDto)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()),
                    mainGame,
                    mainGame.getRemakeVersions(),
                    "Remakes"
            );
        }

        // Remasters
        if (gameDto.getRemasters() != null) {
            updateRelatedGameCollection(
                    gameDto.getRemasters().stream()
                            .map(this::convertDlcInfoToGameDto)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()),
                    mainGame,
                    mainGame.getRemasterVersions(),
                    "Remasters"
            );
        }

        // Similar Games
        if (gameDto.getSimilarGames() != null) {
            updateRelatedGameCollection(
                    gameDto.getSimilarGames().stream()
                            .map(this::convertSimilarGameInfoToGameDto) // Usa el conversor adecuado
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()),
                    mainGame,
                    mainGame.getSimilarGames(),
                    "SimilarGames"
            );
        }
    }

    // Método helper para actualizar colecciones de juegos relacionados (remakes, remasters, similar)
    private void updateRelatedGameCollection(List<GameDto> relatedGameDtos, Game mainGame, Set<Game> existingRelatedCollection, String relationName) {
        logger.debug("Procesando lista de {} para el juego principal ID: {}", relationName, mainGame.getIgdbId());
        Set<Game> newRelatedEntities = new HashSet<>();

        for (GameDto relatedDto : relatedGameDtos) {
            if (relatedDto.getIgdbId() != null && !relatedDto.getIgdbId().equals(mainGame.getIgdbId())) {
                Game relatedEntity = processSingleGameDto(relatedDto, null); // Procesar como juego independiente
                if (relatedEntity != null) {
                    newRelatedEntities.add(relatedEntity);
                }
            }
        }

        // Compara y actualiza la colección solo si hay cambios
        // Esto es importante para evitar operaciones innecesarias de base de datos
        // y para que Hibernate maneje correctamente las relaciones ManyToMany.
        if (!existingRelatedCollection.equals(newRelatedEntities)) {
            logger.debug("Colección de '{}' para el juego {} ha cambiado. Actualizando.", relationName, mainGame.getIgdbId());
            existingRelatedCollection.clear();
            existingRelatedCollection.addAll(newRelatedEntities);
        }
    }


    private void initializeLazyCollections(Game game) {
        if (game == null) return;
        logger.debug("Inicializando colecciones LAZY para Game ID: {} antes de devolver.", game.getIgdbId());
        Hibernate.initialize(game.getChildGames());
        Hibernate.initialize(game.getSimilarGames());
        Hibernate.initialize(game.getRemakeVersions());
        Hibernate.initialize(game.getRemasterVersions());

        if (game.getInvolvedCompanies() != null) Hibernate.initialize(game.getInvolvedCompanies());
        if (game.getGameModes() != null) Hibernate.initialize(game.getGameModes());
        if (game.getGenres() != null) Hibernate.initialize(game.getGenres());
        if (game.getThemes() != null) Hibernate.initialize(game.getThemes());
        if (game.getKeywords() != null) Hibernate.initialize(game.getKeywords());
        if (game.getPlatforms() != null) Hibernate.initialize(game.getPlatforms());
        if (game.getGameEngines() != null) Hibernate.initialize(game.getGameEngines());
        if (game.getFranchises() != null) Hibernate.initialize(game.getFranchises());
        if (game.getArtworks() != null) Hibernate.initialize(game.getArtworks());
        if (game.getScreenshots() != null) Hibernate.initialize(game.getScreenshots());
        if (game.getWebsites() != null) Hibernate.initialize(game.getWebsites());
        if (game.getVideos() != null) Hibernate.initialize(game.getVideos());
    }

    private void updateSelectiveFields(GameDto dto, Game entity) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getSlug() != null) entity.setSlug(dto.getSlug());
        if (dto.getCover() != null) {
            entity.setCover(coverMapper.toEntity(dto.getCover()));
        }
        if (dto.getTotalRating() != null) entity.setTotalRating(dto.getTotalRating());
        if (dto.getTotalRatingCount() != null) entity.setTotalRatingCount(dto.getTotalRatingCount());
        if (dto.getGameType() != null) entity.setGameType(dto.getGameType());
        if (dto.getFirstReleaseDate() != null) entity.setFirstReleaseDate(gameMapper.mapTimestampToInstant(dto.getFirstReleaseDate()));

        if (dto.getSummary() != null) {
            if (!dto.getSummary().isEmpty() || entity.getSummary() == null || entity.getSummary().isEmpty()) {
                entity.setSummary(dto.getSummary());
            }
        }
        if (dto.getStoryline() != null) {
            if (!dto.getStoryline().isEmpty() || entity.getStoryline() == null || entity.getStoryline().isEmpty()) {
                entity.setStoryline(dto.getStoryline());
            }
        }

        if (dto.getArtworks() != null && !dto.getArtworks().isEmpty()) {
            entity.getArtworks().clear();
            entity.getArtworks().addAll(dto.getArtworks().stream().map(artworkMapper::toEntity).collect(Collectors.toList()));
        }
        if (dto.getScreenshots() != null && !dto.getScreenshots().isEmpty()) {
            entity.getScreenshots().clear();
            entity.getScreenshots().addAll(dto.getScreenshots().stream().map(screenshotMapper::toEntity).collect(Collectors.toList()));
        }
        if (dto.getVideos() != null && !dto.getVideos().isEmpty()) {
            entity.getVideos().clear();
            entity.getVideos().addAll(dto.getVideos().stream().map(videoMapper::toEntity).collect(Collectors.toList()));
        }
        if (dto.getWebsites() != null && !dto.getWebsites().isEmpty()) {
            entity.getWebsites().clear();
            entity.getWebsites().addAll(dto.getWebsites().stream().map(websiteMapper::toEntity).collect(Collectors.toList()));
        }
    }

    private void setFirstReleaseStatusFromDto(GameDto gameDto, Game gameEntity) {
        if (gameDto.getGameStatus() != null && gameDto.getGameStatus().getId() != null) {
            Integer statusId = gameDto.getGameStatus().getId();
            ReleaseStatus statusEnum = ReleaseStatus.mapFromIgdbValue(statusId);
            if (gameEntity.getFirstReleaseStatus() != statusEnum) {
                gameEntity.setFirstReleaseStatus(statusEnum);
                logger.debug("Establecido firstReleaseStatus: {} (IGDB game_status.id: {}) para IGDB ID {}", statusEnum, statusId, gameDto.getIgdbId());
            }
            gameDto.setFirstReleaseStatus(statusEnum);
        } else if (gameEntity.getFirstReleaseStatus() == null || (gameDto.isFullDetails() && gameDto.getGameStatus() == null)) {
            if (gameEntity.getFirstReleaseStatus() != ReleaseStatus.UNKNOWN) {
                gameEntity.setFirstReleaseStatus(ReleaseStatus.UNKNOWN);
                logger.debug("IGDB game_status nulo o no presente en DTO (fullDetails: {}). FirstReleaseStatus puesto a UNKNOWN para IGDB ID {}.", gameDto.isFullDetails(), gameDto.getIgdbId());
            }
            gameDto.setFirstReleaseStatus(ReleaseStatus.UNKNOWN);
        }
    }

    private void processChildGameList(List<DlcInfoDto> dlcInfoList, Game parentGame, String listType) {
        if (dlcInfoList != null && parentGame != null) {
            logger.debug("Procesando lista de {} para el juego padre ID: {}", listType, parentGame.getIgdbId());
            dlcInfoList.forEach(dlcInfo -> {
                if (dlcInfo != null && dlcInfo.getIgdbId() != null && !dlcInfo.getIgdbId().equals(parentGame.getIgdbId())) {
                    GameDto childGameDto = convertDlcInfoToGameDto(dlcInfo);
                    if (childGameDto != null) {
                        processSingleGameDto(childGameDto, parentGame);
                    }
                }
            });
        }
    }

    private void processRelatedGameList(List<DlcInfoDto> relatedInfoDtoList, Game mainGame, Set<Game> entityCollectionToUpdate, String relationType) {
        if (relatedInfoDtoList != null && mainGame != null) {
            logger.debug("Procesando lista de {} para el juego principal ID: {}", relationType, mainGame.getIgdbId());
            Set<Long> existingRelatedIds = entityCollectionToUpdate.stream().map(Game::getIgdbId).collect(Collectors.toSet());
            Set<Game> newRelatedEntities = new HashSet<>();

            for (DlcInfoDto dto : relatedInfoDtoList) {
                if (dto != null && dto.getIgdbId() != null && !dto.getIgdbId().equals(mainGame.getIgdbId())) {
                    GameDto relatedAsFullDto = convertDlcInfoToGameDto(dto);
                    if (relatedAsFullDto != null) {
                        Game relatedEntity = processSingleGameDto(relatedAsFullDto, null);
                        if (relatedEntity != null) {
                            newRelatedEntities.add(relatedEntity);
                        }
                    }
                }
            }

            Set<Long> newRelatedIds = newRelatedEntities.stream().map(Game::getIgdbId).collect(Collectors.toSet());
            if (!existingRelatedIds.equals(newRelatedIds)) {
                logger.debug("Colección de '{}' para el juego {} ha cambiado. Actualizando.", relationType, mainGame.getIgdbId());
                entityCollectionToUpdate.clear();
                entityCollectionToUpdate.addAll(newRelatedEntities);
            }
        }
    }

    private GameDto convertDlcInfoToGameDto(DlcInfoDto dlcInfo) {
        if (dlcInfo == null || dlcInfo.getIgdbId() == null) return null;
        GameDto gameDto = new GameDto();
        gameDto.setIgdbId(dlcInfo.getIgdbId());
        gameDto.setName(dlcInfo.getName());
        gameDto.setCover(dlcInfo.getCover());
        gameDto.setTotalRating(dlcInfo.getTotalRating());
        gameDto.setGameType(dlcInfo.getGameType());
        gameDto.setSlug(dlcInfo.getSlug());
        initializeEmptyCollections(gameDto);
        gameDto.setFullDetails(false);
        return gameDto;
    }

    private GameDto convertSimilarGameInfoToGameDto(SimilarGameInfoDto similarInfo) {
        if (similarInfo == null || similarInfo.getIgdbId() == null) return null;
        GameDto gameDto = new GameDto();
        gameDto.setIgdbId(similarInfo.getIgdbId());
        gameDto.setName(similarInfo.getName());
        gameDto.setSlug(similarInfo.getSlug());
        gameDto.setSummary(similarInfo.getSummary());
        gameDto.setCover(similarInfo.getCover());
        gameDto.setTotalRating(similarInfo.getTotalRating());
        gameDto.setGameType(GameType.GAME);
        initializeEmptyCollections(gameDto);
        gameDto.setFullDetails(false);
        return gameDto;
    }

    private void initializeEmptyCollections(GameDto gameDto) {
        // ... (sin cambios)
        if (gameDto == null) return;
        if (gameDto.getGameModes() == null) gameDto.setGameModes(new ArrayList<>());
        if (gameDto.getGenres() == null) gameDto.setGenres(new ArrayList<>());
        if (gameDto.getArtworks() == null) gameDto.setArtworks(new ArrayList<>());
        if (gameDto.getFranchises() == null) gameDto.setFranchises(new ArrayList<>());
        if (gameDto.getGameEngines() == null) gameDto.setGameEngines(new ArrayList<>());
        if (gameDto.getKeywords() == null) gameDto.setKeywords(new ArrayList<>());
        if (gameDto.getPlatforms() == null) gameDto.setPlatforms(new ArrayList<>());
        if (gameDto.getScreenshots() == null) gameDto.setScreenshots(new ArrayList<>());
        if (gameDto.getWebsites() == null) gameDto.setWebsites(new ArrayList<>());
        if (gameDto.getVideos() == null) gameDto.setVideos(new ArrayList<>());
        if (gameDto.getThemes() == null) gameDto.setThemes(new ArrayList<>());
        if (gameDto.getDlcs() == null) gameDto.setDlcs(new ArrayList<>());
        if (gameDto.getExpansions() == null) gameDto.setExpansions(new ArrayList<>());
        if (gameDto.getBundles() == null) gameDto.setBundles(new ArrayList<>());
        if (gameDto.getRemakes() == null) gameDto.setRemakes(new ArrayList<>());
        if (gameDto.getRemasters() == null) gameDto.setRemasters(new ArrayList<>());
        if (gameDto.getSimilarGames() == null) gameDto.setSimilarGames(new ArrayList<>());
        if (gameDto.getInvolvedCompanies() == null) gameDto.setInvolvedCompanies(new ArrayList<>());
    }

    /**
     * Obtiene una entidad GameMode existente por igdbId o crea una nueva si no existe.
     * Actualiza el nombre si es diferente.
     */
    private GameMode getOrCreateGameMode(GameModeDto dto) {
        if (dto == null || dto.getIgdbId() == null) {
            return null;
        }
        return gameModeRepository.findByIgdbId(dto.getIgdbId())
                .map(existingEntity -> {
                    if (!Objects.equals(dto.getName(), existingEntity.getName())) {
                        existingEntity.setName(dto.getName());
                        return gameModeRepository.save(existingEntity); // Guardar si hay cambios
                    }
                    return existingEntity;
                })
                .orElseGet(() -> gameModeRepository.save(gameModeMapper.toEntity(dto)));
    }

    /**
     * Obtiene una entidad Genre existente por igdbId o crea una nueva si no existe.
     * Actualiza el nombre si es diferente.
     */
    private Genre getOrCreateGenre(GenreDto dto) {
        if (dto == null || dto.getIgdbId() == null) {
            return null;
        }
        return genreRepository.findByIgdbId(dto.getIgdbId())
                .map(existingEntity -> {
                    if (!Objects.equals(dto.getName(), existingEntity.getName())) {
                        existingEntity.setName(dto.getName());
                        return genreRepository.save(existingEntity);
                    }
                    return existingEntity;
                })
                .orElseGet(() -> genreRepository.save(genreMapper.toEntity(dto)));
    }

    /**
     * Obtiene una entidad Franchise existente por igdbId o crea una nueva si no existe.
     * Actualiza el nombre si es diferente.
     */
    private Franchise getOrCreateFranchise(FranchiseDto dto) {
        if (dto == null || dto.getIgdbId() == null) {
            return null;
        }
        return franchiseRepository.findByIgdbId(dto.getIgdbId())
                .map(existingEntity -> {
                    if (!Objects.equals(dto.getName(), existingEntity.getName())) {
                        existingEntity.setName(dto.getName());
                        return franchiseRepository.save(existingEntity);
                    }
                    return existingEntity;
                })
                .orElseGet(() -> franchiseRepository.save(franchiseMapper.toEntity(dto)));
    }

    /**
     * Obtiene una entidad GameEngine existente por igdbId o crea una nueva si no existe.
     * Actualiza el nombre si es diferente.
     */
    private GameEngine getOrCreateGameEngine(GameEngineDto dto) {
        if (dto == null || dto.getIgdbId() == null) {
            return null;
        }
        return gameEngineRepository.findByIgdbId(dto.getIgdbId())
                .map(existingEntity -> {
                    if (!Objects.equals(dto.getName(), existingEntity.getName())) {
                        existingEntity.setName(dto.getName());
                        return gameEngineRepository.save(existingEntity);
                    }
                    return existingEntity;
                })
                .orElseGet(() -> gameEngineRepository.save(gameEngineMapper.toEntity(dto)));
    }

    /**
     * Obtiene una entidad Keyword existente por igdbId o crea una nueva si no existe.
     * Actualiza el nombre si es diferente.
     */
    private Keyword getOrCreateKeyword(KeywordDto dto) {
        if (dto == null || dto.getIgdbId() == null) {
            return null;
        }
        return keywordRepository.findByIgdbId(dto.getIgdbId())
                .map(existingEntity -> {
                    if (!Objects.equals(dto.getName(), existingEntity.getName())) {
                        existingEntity.setName(dto.getName());
                        return keywordRepository.save(existingEntity);
                    }
                    return existingEntity;
                })
                .orElseGet(() -> keywordRepository.save(keywordMapper.toEntity(dto)));
    }

    /**
     * Obtiene una entidad Platform existente por igdbId o crea una nueva si no existe.
     * Actualiza campos relevantes si son diferentes.
     */
    private Platform getOrCreatePlatform(PlatformDto dto) {
        if (dto == null || dto.getIgdbId() == null) {
            return null;
        }
        return platformRepository.findByIgdbId(dto.getIgdbId())
                .map(existingEntity -> {
                    boolean changed = false;
                    if (!Objects.equals(dto.getName(), existingEntity.getName())) {
                        existingEntity.setName(dto.getName());
                        changed = true;
                    }
                    if (!Objects.equals(dto.getAlternativeName(), existingEntity.getAlternativeName())) {
                        existingEntity.setAlternativeName(dto.getAlternativeName());
                        changed = true;
                    }

                    PlatformLogoDto logoDto = dto.getPlatformLogo();
                    PlatformLogo currentLogoEntity = existingEntity.getPlatformLogo();

                    if (logoDto != null) {
                        // Usa la instancia inyectada de PlatformLogoMapper
                        PlatformLogo newLogoEntity = platformLogoMapper.toEntity(logoDto); // <--- CORREGIDO AQUÍ
                        if (currentLogoEntity == null || !currentLogoEntity.equals(newLogoEntity)) {
                            existingEntity.setPlatformLogo(newLogoEntity);
                            changed = true;
                        }
                    } else if (currentLogoEntity != null) {
                        existingEntity.setPlatformLogo(null);
                        changed = true;
                    }

                    return changed ? platformRepository.save(existingEntity) : existingEntity;
                })
                .orElseGet(() -> {
                    // Usa la instancia inyectada de PlatformMapper
                    Platform newPlatform = platformMapper.toEntity(dto); // <--- CORREGIDO AQUÍ (usa la instancia de clase)
                    return platformRepository.save(newPlatform);
                });
    }

    private void processAssociatedManyToManyCollections(GameDto gameDto, Game gameEntity) {
        logger.debug("Procesando colecciones ManyToMany para el juego completo {}", gameEntity.getIgdbId());

        // GameModes
        if (gameDto.getGameModes() != null) {
            Set<GameMode> gameModesFromDto = gameDto.getGameModes().stream()
                    .map(this::getOrCreateGameMode) // Usa el helper
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!gameEntity.getGameModes().equals(gameModesFromDto)) {
                gameEntity.setGameModes(gameModesFromDto);
            }
        } else { // Si el DTO viene con la colección nula, podría interpretarse como "eliminar todas"
            if (!gameEntity.getGameModes().isEmpty()) {
                gameEntity.getGameModes().clear();
            }
        }

        // Genres
        if (gameDto.getGenres() != null) {
            Set<Genre> genresFromDto = gameDto.getGenres().stream()
                    .map(this::getOrCreateGenre) // Usa el helper
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!gameEntity.getGenres().equals(genresFromDto)) {
                gameEntity.setGenres(genresFromDto);
            }
        } else {
            if (!gameEntity.getGenres().isEmpty()) {
                gameEntity.getGenres().clear();
            }
        }

        // Franchises
        if (gameDto.getFranchises() != null) {
            Set<Franchise> franchisesFromDto = gameDto.getFranchises().stream()
                    .map(this::getOrCreateFranchise) // Usa el helper
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!gameEntity.getFranchises().equals(franchisesFromDto)) {
                gameEntity.setFranchises(franchisesFromDto);
            }
        } else {
            if (!gameEntity.getFranchises().isEmpty()) {
                gameEntity.getFranchises().clear();
            }
        }

        // GameEngines
        if (gameDto.getGameEngines() != null) {
            Set<GameEngine> gameEnginesFromDto = gameDto.getGameEngines().stream()
                    .map(this::getOrCreateGameEngine) // Usa el helper
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!gameEntity.getGameEngines().equals(gameEnginesFromDto)) {
                gameEntity.setGameEngines(gameEnginesFromDto);
            }
        } else {
            if (!gameEntity.getGameEngines().isEmpty()) {
                gameEntity.getGameEngines().clear();
            }
        }

        // Keywords
        if (gameDto.getKeywords() != null) {
            Set<Keyword> keywordsFromDto = gameDto.getKeywords().stream()
                    .map(this::getOrCreateKeyword) // Usa el helper
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!gameEntity.getKeywords().equals(keywordsFromDto)) {
                gameEntity.setKeywords(keywordsFromDto);
            }
        } else {
            if (!gameEntity.getKeywords().isEmpty()) {
                gameEntity.getKeywords().clear();
            }
        }

        // Platforms
        if (gameDto.getPlatforms() != null) {
            Set<Platform> platformsFromDto = gameDto.getPlatforms().stream()
                    .map(this::getOrCreatePlatform) // Usa el helper
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!gameEntity.getPlatforms().equals(platformsFromDto)) {
                gameEntity.setPlatforms(platformsFromDto);
            }
        } else {
            if (!gameEntity.getPlatforms().isEmpty()) {
                gameEntity.getPlatforms().clear();
            }
        }

        // Themes
        if (gameDto.getThemes() != null) {
            Set<Theme> themesFromDto = gameDto.getThemes().stream()
                    .map(this::getOrCreateTheme) // Usa el helper
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!gameEntity.getThemes().equals(themesFromDto)) {
                gameEntity.setThemes(themesFromDto);
            }
        } else {
            if (!gameEntity.getThemes().isEmpty()) {
                gameEntity.getThemes().clear();
            }
        }
    }


    /**
     * Obtiene una entidad Theme existente por igdbId o crea una nueva si no existe.
     * Actualiza el nombre si es diferente.
     */
    private Theme getOrCreateTheme(ThemeDto dto) {
        if (dto == null || dto.getIgdbId() == null) {
            return null;
        }
        return themeRepository.findByIgdbId(dto.getIgdbId())
                .map(existingEntity -> {
                    if (!Objects.equals(dto.getName(), existingEntity.getName())) {
                        existingEntity.setName(dto.getName());
                        return themeRepository.save(existingEntity);
                    }
                    return existingEntity;
                })
                .orElseGet(() -> themeRepository.save(themeMapper.toEntity(dto)));
    }

    private void processInvolvedCompanies(GameDto gameDto, Game gameEntity) {
        logger.debug("Procesando InvolvedCompanies para el juego completo {}", gameEntity.getIgdbId());
        Set<GameCompanyInvolvement> involvementsFromDtoProcessing = new HashSet<>();
        if (gameDto.getInvolvedCompanies() != null && !gameDto.getInvolvedCompanies().isEmpty()) {
            for(InvolvedCompanyDto invDto : gameDto.getInvolvedCompanies()){
                if(invDto==null||invDto.getInvolvementIgdbId()==null) continue;
                CompanyInfoDto ciDto=invDto.getCompany();
                if(ciDto==null||ciDto.getIgdbId()==null){logger.warn("InvolvedCompany (ID: {}) tiene CompanyInfo nula o CompanyInfo.igdbId nulo. Saltando.", invDto.getInvolvementIgdbId());continue;}

                Company cEnt=companyRepository.findByIgdbId(ciDto.getIgdbId())
                        .map(eC->{
                            if(!Objects.equals(eC.getName(),ciDto.getName())){
                                eC.setName(ciDto.getName());
                                return companyRepository.save(eC);
                            }
                            return eC;
                        })
                        .orElseGet(()->companyRepository.save(companyMapper.toEntity(ciDto)));

                GameCompanyInvolvement inv=gameCompanyInvolvementRepository.findByInvolvementIgdbId(invDto.getInvolvementIgdbId())
                        .map(eI->{
                            boolean changed = false;
                            if (!Objects.equals(eI.getGame().getIgdbId(), gameEntity.getIgdbId())) {eI.setGame(gameEntity); changed = true;} // Re-asociar si es necesario
                            if (!Objects.equals(eI.getCompany().getIgdbId(), cEnt.getIgdbId())) {eI.setCompany(cEnt); changed = true;}
                            if (eI.isDeveloper() != invDto.isDeveloper()) {eI.setDeveloper(invDto.isDeveloper()); changed = true;}
                            if (eI.isPublisher() != invDto.isPublisher()) {eI.setPublisher(invDto.isPublisher()); changed = true;}
                            if (eI.isPorting() != invDto.isPorting()) {eI.setPorting(invDto.isPorting()); changed = true;}
                            if (eI.isSupporting() != invDto.isSupporting()) {eI.setSupporting(invDto.isSupporting()); changed = true;}
                            return changed ? gameCompanyInvolvementRepository.save(eI) : eI;
                        })
                        .orElseGet(()->{
                            GameCompanyInvolvement nI=new GameCompanyInvolvement();
                            nI.setInvolvementIgdbId(invDto.getInvolvementIgdbId());
                            nI.setGame(gameEntity);
                            nI.setCompany(cEnt);
                            nI.setDeveloper(invDto.isDeveloper());
                            nI.setPublisher(invDto.isPublisher());
                            nI.setPorting(invDto.isPorting());
                            nI.setSupporting(invDto.isSupporting());
                            return gameCompanyInvolvementRepository.save(nI);
                        });
                involvementsFromDtoProcessing.add(inv);
            }
        }
        if (!gameEntity.getInvolvedCompanies().equals(involvementsFromDtoProcessing)) {
            gameEntity.getInvolvedCompanies().clear();
            gameEntity.getInvolvedCompanies().addAll(involvementsFromDtoProcessing);
        }
    }

    public List<Game> getAllGamesOriginal() {
        List<Game> games = gameRepository.findAll();
        // Inicializar colecciones necesarias para el DTO si se van a mapear fuera de la sesión
        for (Game game : games) {
            Hibernate.initialize(game.getChildGames());
            Hibernate.initialize(game.getSimilarGames());
            Hibernate.initialize(game.getRemakeVersions());
            Hibernate.initialize(game.getRemasterVersions());
            Hibernate.initialize(game.getInvolvedCompanies());
            // ... y otras colecciones que tu GameMapper necesite para el listado general
        }
        return games;
    }

    public Game getGameByIgdbIdOriginal(Long igdbId) {
        return gameRepository.findByIgdbId(igdbId)
                .map(game -> {
                    // Inicializar colecciones necesarias para el DTO si se van a mapear fuera de la sesión
                    Hibernate.initialize(game.getChildGames());
                    Hibernate.initialize(game.getSimilarGames());
                    Hibernate.initialize(game.getRemakeVersions());
                    Hibernate.initialize(game.getRemasterVersions());
                    Hibernate.initialize(game.getInvolvedCompanies());
                    Hibernate.initialize(game.getGameModes());
                    Hibernate.initialize(game.getGenres());
                    Hibernate.initialize(game.getThemes());
                    Hibernate.initialize(game.getKeywords());
                    Hibernate.initialize(game.getPlatforms());
                    Hibernate.initialize(game.getGameEngines());
                    Hibernate.initialize(game.getFranchises());
                    Hibernate.initialize(game.getArtworks());
                    Hibernate.initialize(game.getScreenshots());
                    Hibernate.initialize(game.getWebsites());
                    Hibernate.initialize(game.getVideos());
                    return game;
                })
                .orElse(null);
    }
}