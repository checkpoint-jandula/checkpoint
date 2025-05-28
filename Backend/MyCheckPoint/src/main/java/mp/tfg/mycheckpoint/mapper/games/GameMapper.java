package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.DlcInfoDto;
import mp.tfg.mycheckpoint.dto.games.GameDto;
import mp.tfg.mycheckpoint.dto.games.SimilarGameInfoDto;
import mp.tfg.mycheckpoint.entity.games.Game;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", uses = {
        CoverMapper.class, GameModeMapper.class, GenreMapper.class, ArtworkMapper.class,
        FranchiseMapper.class, GameEngineMapper.class, KeywordMapper.class, PlatformMapper.class,
        ScreenshotMapper.class, WebsiteMapper.class, VideoMapper.class, ThemeMapper.class,
        InvolvedCompanyMapper.class
})
public interface GameMapper {

    @Mapping(source = "igdbId", target = "igdbId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "cover", target = "cover")
    @Mapping(source = "totalRating", target = "totalRating")
    @Mapping(source = "gameType", target = "gameType")
    @Mapping(source = "slug", target = "slug")
    DlcInfoDto gameEntityToDlcInfoDto(Game gameEntity);

    @Mapping(source = "igdbId", target = "igdbId")
    @Mapping(source = "cover", target = "cover")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "slug", target = "slug")
    @Mapping(source = "summary", target = "summary")
    @Mapping(source = "totalRating", target = "totalRating")
    SimilarGameInfoDto gameEntityToSimilarGameInfoDto(Game gameEntity);

    @Mapping(source = "parentGame", target = "parentGameInfo")
    @Mapping(source = "versionParentGame", target = "versionParent")
    @Mapping(source = "remakeVersions", target = "remakes")
    @Mapping(source = "remasterVersions", target = "remasters")
    @Mapping(source = "similarGames", target = "similarGames")
    @Mapping(source = "involvedCompanies", target = "involvedCompanies")
    @Mapping(source = "firstReleaseStatus", target = "firstReleaseStatus")
    GameDto toDto(Game game);

    @AfterMapping
    default void mapChildGamesToCategorizedLists(Game game, @MappingTarget GameDto gameDto) {
        List<DlcInfoDto> dlcList = new ArrayList<>();
        List<DlcInfoDto> expansionList = new ArrayList<>();
        List<DlcInfoDto> bundleList = new ArrayList<>();

        if (game.getChildGames() != null && !game.getChildGames().isEmpty()) {
            for (Game childGame : game.getChildGames()) {
                DlcInfoDto infoDto = gameEntityToDlcInfoDto(childGame);
                if (infoDto != null && childGame.getGameType() != null) {
                    switch (childGame.getGameType()) {
                        case DLC:
                            dlcList.add(infoDto);
                            break;
                        case EXPANSION:
                        case STANDALONE_EXPANSION:
                            expansionList.add(infoDto);
                            break;
                        case BUNDLE:
                            bundleList.add(infoDto);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        gameDto.setDlcs(dlcList);
        gameDto.setExpansions(expansionList);
        gameDto.setBundles(bundleList);
    }

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "parentGame", ignore = true)
    @Mapping(target = "childGames", ignore = true)
    @Mapping(target = "versionParentGame", ignore = true)
    @Mapping(target = "remakeVersions", ignore = true)
    @Mapping(target = "remasterVersions", ignore = true)
    @Mapping(target = "similarGames", ignore = true)
    @Mapping(target = "involvedCompanies", ignore = true)
    @Mapping(target = "gameModes", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "themes", ignore = true)
    @Mapping(target = "keywords", ignore = true)
    @Mapping(target = "platforms", ignore = true)
    @Mapping(target = "gameEngines", ignore = true)
    @Mapping(target = "franchises", ignore = true)
    @Mapping(target = "firstReleaseStatus", ignore = true)
    Game toEntity(GameDto gameDto);

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "parentGame", ignore = true)
    @Mapping(target = "childGames", ignore = true)
    @Mapping(target = "versionParentGame", ignore = true)
    @Mapping(target = "remakeVersions", ignore = true)
    @Mapping(target = "remasterVersions", ignore = true)
    @Mapping(target = "similarGames", ignore = true)
    @Mapping(target = "involvedCompanies", ignore = true)
    @Mapping(target = "gameModes", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "themes", ignore = true)
    @Mapping(target = "keywords", ignore = true)
    @Mapping(target = "platforms", ignore = true)
    @Mapping(target = "gameEngines", ignore = true)
    @Mapping(target = "franchises", ignore = true)
    @Mapping(target = "firstReleaseStatus", ignore = true)
    void updateFromDto(GameDto gameDto, @MappingTarget Game game);

    default Instant mapTimestampToInstant(Long timestamp) {
        if (timestamp == null) return null;
        return Instant.ofEpochSecond(timestamp);
    }

    default Long mapInstantToTimestamp(Instant instant) {
        if (instant == null) return null;
        return instant.getEpochSecond();
    }
}