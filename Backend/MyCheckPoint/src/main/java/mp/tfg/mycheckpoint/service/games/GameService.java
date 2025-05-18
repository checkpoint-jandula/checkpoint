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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
                       PlatformRepository platformRepository, ThemeRepository themeRepository, CompanyRepository companyRepository,
                       GameCompanyInvolvementRepository gameCompanyInvolvementRepository,
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
            logger.warn("processSingleGameDto: GameDto nulo o sin IgdbId.");
            return null;
        }
        final Game finalProspectiveParentGame = prospectiveParentGameEntity;
        logger.debug("Procesando GameDto: ID={}, Name={}, isFullDetails={}, ProspectiveParentIGDBID={}",
                gameDto.getIgdbId(), gameDto.getName(), gameDto.isFullDetails(),
                (finalProspectiveParentGame != null ? finalProspectiveParentGame.getIgdbId() : "null"));

        Game currentGameEntity = gameRepository.findByIgdbId(gameDto.getIgdbId())
                .map(existingGame -> {
                    logger.debug("Juego existente IGDB ID: {}. Actualizando. isFullDetails: {}", gameDto.getIgdbId(), gameDto.isFullDetails());
                    if (!gameDto.isFullDetails()) {
                        logger.debug("GameDto (ID: {}) es parcial. Actualización selectiva.", gameDto.getIgdbId());
                        updateSelectiveFields(gameDto, existingGame);
                    } else {
                        logger.debug("GameDto (ID: {}) es completo. Actualización completa vía gameMapper.updateFromDto.", gameDto.getIgdbId());
                        gameMapper.updateFromDto(gameDto, existingGame);
                    }
                    setFirstReleaseStatusFromDto(gameDto, existingGame);
                    if (finalProspectiveParentGame != null && (existingGame.getParentGame() == null || !finalProspectiveParentGame.getIgdbId().equals(existingGame.getParentGame().getIgdbId()))) {
                        existingGame.setParentGame(finalProspectiveParentGame);
                    }
                    return existingGame;
                })
                .orElseGet(() -> {
                    logger.debug("Creando nuevo juego para IGDB ID: {}", gameDto.getIgdbId());
                    Game newGame = gameMapper.toEntity(gameDto);
                    setFirstReleaseStatusFromDto(gameDto, newGame);
                    if (finalProspectiveParentGame != null) newGame.setParentGame(finalProspectiveParentGame);
                    return newGame;
                });

        try {
            currentGameEntity = gameRepository.save(currentGameEntity);
        } catch (Exception e) {
            logger.error("Error guardando currentGameEntity (IGDB ID: {}) antes de relaciones detalladas: {}", gameDto.getIgdbId(), e.getMessage(), e);
            throw e;
        }
        final Game managedCurrentGameEntity = currentGameEntity;

        if (gameDto.getParentGameInfo() != null && managedCurrentGameEntity.getParentGame() == null) {
            DlcInfoDto parentDto = gameDto.getParentGameInfo();
            if (parentDto.getIgdbId() != null && !parentDto.getIgdbId().equals(managedCurrentGameEntity.getIgdbId())) {
                GameDto parentAsFull = convertDlcInfoToGameDto(parentDto);
                if (parentAsFull != null) {
                    Game pEntity = processSingleGameDto(parentAsFull, null);
                    if (pEntity != null) managedCurrentGameEntity.setParentGame(pEntity);
                }
            }
        }
        if (gameDto.getVersionParent() != null && managedCurrentGameEntity.getVersionParentGame() == null) {
            DlcInfoDto vpDto = gameDto.getVersionParent();
            if (vpDto.getIgdbId() != null && !vpDto.getIgdbId().equals(managedCurrentGameEntity.getIgdbId())) {
                GameDto vpAsFull = convertDlcInfoToGameDto(vpDto);
                if (vpAsFull != null) {
                    Game vpEntity = processSingleGameDto(vpAsFull, null);
                    if (vpEntity != null) managedCurrentGameEntity.setVersionParentGame(vpEntity);
                }
            }
        }

        if (gameDto.isFullDetails()) {
            logger.debug("DTO completo (ID: {}), procesando colecciones ManyToMany e InvolvedCompanies.", managedCurrentGameEntity.getIgdbId());
            processAssociatedManyToManyCollections(gameDto, managedCurrentGameEntity);
            processInvolvedCompanies(gameDto, managedCurrentGameEntity);
        } else {
            logger.debug("DTO parcial (ID: {}), se omite la actualización masiva de colecciones ManyToMany e InvolvedCompanies.", managedCurrentGameEntity.getIgdbId());
        }

        Game savedMainGameEntity;
        try {
            savedMainGameEntity = gameRepository.save(managedCurrentGameEntity);
        } catch (Exception e) {
            logger.error("Error guardando managedCurrentGameEntity (IGDB ID: {}) después de procesar colecciones: {}", gameDto.getIgdbId(), e.getMessage(), e);
            throw e;
        }

        processChildGameList(gameDto.getDlcs(), savedMainGameEntity, "DLCs");
        processChildGameList(gameDto.getExpansions(), savedMainGameEntity, "Expansions");
        processChildGameList(gameDto.getBundles(), savedMainGameEntity, "Bundles");
        processRelatedGameList(gameDto.getRemakes(), savedMainGameEntity, savedMainGameEntity.getRemakeVersions(), "remake");
        processRelatedGameList(gameDto.getRemasters(), savedMainGameEntity, savedMainGameEntity.getRemasterVersions(), "remaster");

        if (gameDto.getSimilarGames() != null) {
            Set<Game> currentSimilars = savedMainGameEntity.getSimilarGames(); // Podría necesitar inicialización si se accede fuera
            Set<Game> newSimilars = new HashSet<>();
            for (SimilarGameInfoDto simDto : gameDto.getSimilarGames()) {
                if (simDto != null && simDto.getIgdbId() != null && !simDto.getIgdbId().equals(savedMainGameEntity.getIgdbId())) {
                    GameDto simAsFull = convertSimilarGameInfoToGameDto(simDto);
                    if (simAsFull != null) {
                        Game simEntity = processSingleGameDto(simAsFull, null);
                        if (simEntity != null) newSimilars.add(simEntity);
                    }
                }
            }
            if (!currentSimilars.equals(newSimilars)) {
                currentSimilars.clear();
                currentSimilars.addAll(newSimilars);
            }
        }

        Game finalSavedEntity;
        try {
            finalSavedEntity = gameRepository.save(savedMainGameEntity);
        } catch (Exception e) {
            logger.error("Error en guardado final para IGDB ID: {}: {}",
                    (savedMainGameEntity != null ? savedMainGameEntity.getIgdbId() : gameDto.getIgdbId()),
                    e.getMessage(), e);
            throw e;
        }

        // INICIALIZACIÓN DE COLECCIONES LAZY
        if (finalSavedEntity != null) {
            logger.debug("Inicializando colecciones LAZY para Game ID: {} antes de devolver.", finalSavedEntity.getIgdbId());
            Hibernate.initialize(finalSavedEntity.getChildGames()); // Causante del error original
            Hibernate.initialize(finalSavedEntity.getSimilarGames());
            Hibernate.initialize(finalSavedEntity.getRemakeVersions());
            Hibernate.initialize(finalSavedEntity.getRemasterVersions());
            Hibernate.initialize(finalSavedEntity.getInvolvedCompanies());
            // También inicializa las colecciones ManyToMany si tu GameMapper accede a ellas
            // directamente y no solo a través de sus DTOs correspondientes.
            Hibernate.initialize(finalSavedEntity.getGameModes());
            Hibernate.initialize(finalSavedEntity.getGenres());
            Hibernate.initialize(finalSavedEntity.getThemes());
            Hibernate.initialize(finalSavedEntity.getKeywords());
            Hibernate.initialize(finalSavedEntity.getPlatforms());
            Hibernate.initialize(finalSavedEntity.getGameEngines());
            Hibernate.initialize(finalSavedEntity.getFranchises());
            // Las colecciones @ElementCollection (artworks, screenshots, etc.)
            // se cargan EAGER por defecto si no se especifica FetchType,
            // pero si las tienes como LAZY, también necesitarían inicialización aquí.
            // Revisando tu entidad Game, son LAZY, así que las añadimos:
            Hibernate.initialize(finalSavedEntity.getArtworks());
            Hibernate.initialize(finalSavedEntity.getScreenshots());
            Hibernate.initialize(finalSavedEntity.getWebsites());
            Hibernate.initialize(finalSavedEntity.getVideos());
        }
        return finalSavedEntity;
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

    private void processAssociatedManyToManyCollections(GameDto gameDto, Game gameEntity) {
        logger.debug("Procesando colecciones ManyToMany para el juego completo {}", gameEntity.getIgdbId());
        Set<GameMode> gameModesFromDto = new HashSet<>();
        if (gameDto.getGameModes() != null) {
            gameModesFromDto = gameDto.getGameModes().stream()
                    .filter(dto -> dto != null && dto.getIgdbId() != null)
                    .map(dto -> gameModeRepository.findByIgdbId(dto.getIgdbId())
                            .map(e -> {if(!Objects.equals(dto.getName(),e.getName())){e.setName(dto.getName());return gameModeRepository.save(e);}return e;})
                            .orElseGet(()->gameModeRepository.save(gameModeMapper.toEntity(dto))))
                    .collect(Collectors.toSet());
        }
        if (!gameEntity.getGameModes().equals(gameModesFromDto)) {
            gameEntity.setGameModes(gameModesFromDto);
        }

        Set<Genre> genresFromDto = new HashSet<>();
        if (gameDto.getGenres() != null) {
            genresFromDto = gameDto.getGenres().stream()
                    .filter(dto -> dto != null && dto.getIgdbId() != null)
                    .map(dto -> genreRepository.findByIgdbId(dto.getIgdbId())
                            .map(e -> {if(!Objects.equals(dto.getName(),e.getName())){e.setName(dto.getName());return genreRepository.save(e);}return e;})
                            .orElseGet(()->genreRepository.save(genreMapper.toEntity(dto))))
                    .collect(Collectors.toSet());
        }
        if (!gameEntity.getGenres().equals(genresFromDto)) {
            gameEntity.setGenres(genresFromDto);
        }

        Set<Franchise> franchisesFromDto = new HashSet<>();
        if (gameDto.getFranchises() != null) {
            franchisesFromDto = gameDto.getFranchises().stream().filter(dto -> dto != null && dto.getIgdbId() != null).map(dto -> franchiseRepository.findByIgdbId(dto.getIgdbId()).map(e -> {if(!Objects.equals(dto.getName(),e.getName())){e.setName(dto.getName());return franchiseRepository.save(e);}return e;}).orElseGet(()->franchiseRepository.save(franchiseMapper.toEntity(dto)))).collect(Collectors.toSet());
        }
        if (!gameEntity.getFranchises().equals(franchisesFromDto)) gameEntity.setFranchises(franchisesFromDto);

        Set<GameEngine> gameEnginesFromDto = new HashSet<>();
        if (gameDto.getGameEngines() != null) {
            gameEnginesFromDto = gameDto.getGameEngines().stream().filter(dto -> dto != null && dto.getIgdbId() != null).map(dto -> gameEngineRepository.findByIgdbId(dto.getIgdbId()).map(e -> {if(!Objects.equals(dto.getName(),e.getName())){e.setName(dto.getName());return gameEngineRepository.save(e);}return e;}).orElseGet(()->gameEngineRepository.save(gameEngineMapper.toEntity(dto)))).collect(Collectors.toSet());
        }
        if (!gameEntity.getGameEngines().equals(gameEnginesFromDto)) gameEntity.setGameEngines(gameEnginesFromDto);

        Set<Keyword> keywordsFromDto = new HashSet<>();
        if (gameDto.getKeywords() != null) {
            keywordsFromDto = gameDto.getKeywords().stream().filter(dto -> dto != null && dto.getIgdbId() != null).map(dto -> keywordRepository.findByIgdbId(dto.getIgdbId()).map(e -> {if(!Objects.equals(dto.getName(),e.getName())){e.setName(dto.getName());return keywordRepository.save(e);}return e;}).orElseGet(()->keywordRepository.save(keywordMapper.toEntity(dto)))).collect(Collectors.toSet());
        }
        if (!gameEntity.getKeywords().equals(keywordsFromDto)) gameEntity.setKeywords(keywordsFromDto);

        Set<Platform> platformsFromDto = new HashSet<>();
        if (gameDto.getPlatforms() != null) {
            platformsFromDto = gameDto.getPlatforms().stream().filter(dto -> dto != null && dto.getIgdbId() != null).map(dto -> platformRepository.findByIgdbId(dto.getIgdbId()).map(e -> {Platform m=platformMapper.toEntity(dto);
                boolean c=false;if(!Objects.equals(e.getName(),m.getName())){e.setName(m.getName());c=true;}if(!Objects.equals(e.getAlternativeName(),m.getAlternativeName())){e.setAlternativeName(m.getAlternativeName());c=true;}if(m.getPlatformLogo() != null && !Objects.equals(e.getPlatformLogo(),m.getPlatformLogo())){e.setPlatformLogo(m.getPlatformLogo());c=true;} else if (m.getPlatformLogo() == null && e.getPlatformLogo() != null) {e.setPlatformLogo(null); c=true;}return c?platformRepository.save(e):e;}).orElseGet(()->platformRepository.save(platformMapper.toEntity(dto)))).collect(Collectors.toSet());
        }
        if (!gameEntity.getPlatforms().equals(platformsFromDto)) gameEntity.setPlatforms(platformsFromDto);

        Set<Theme> themesFromDto = new HashSet<>();
        if (gameDto.getThemes() != null) {
            themesFromDto = gameDto.getThemes().stream().filter(dto -> dto != null && dto.getIgdbId() != null).map(dto -> themeRepository.findByIgdbId(dto.getIgdbId()).map(e -> {if(!Objects.equals(dto.getName(),e.getName())){e.setName(dto.getName());return themeRepository.save(e);}return e;}).orElseGet(()->themeRepository.save(themeMapper.toEntity(dto)))).collect(Collectors.toSet());
        }
        if (!gameEntity.getThemes().equals(themesFromDto)) gameEntity.setThemes(themesFromDto);
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