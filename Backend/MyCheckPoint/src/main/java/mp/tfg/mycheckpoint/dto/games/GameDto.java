package mp.tfg.mycheckpoint.dto.games;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import mp.tfg.mycheckpoint.dto.enums.GameType;
import mp.tfg.mycheckpoint.dto.enums.ReleaseStatus;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameDto {
    @JsonProperty("id")
    private Long igdbId;
    private CoverDto cover;
    @JsonProperty("game_modes")
    private List<GameModeDto> gameModes = new ArrayList<>();
    private List<GenreDto> genres = new ArrayList<>();
    @JsonProperty("artworks")
    private List<ArtworkDto> artworks = new ArrayList<>();
    private String name;
    @JsonProperty("first_release_date")
    private Long firstReleaseDate;
    @JsonProperty("first_release_status")
    private ReleaseStatus firstReleaseStatus;
    @JsonProperty("franchises")
    private List<FranchiseDto> franchises = new ArrayList<>();
    @JsonProperty("game_engines")
    private List<GameEngineDto> gameEngines = new ArrayList<>();
    @JsonProperty("keywords")
    private List<KeywordDto> keywords = new ArrayList<>();
    @JsonProperty("platforms")
    private List<PlatformDto> platforms = new ArrayList<>();
    @JsonProperty("screenshots")
    private List<ScreenshotDto> screenshots = new ArrayList<>();
    @JsonProperty("websites")
    private List<WebsiteDto> websites = new ArrayList<>();
    @JsonProperty("videos")
    private List<VideoDto> videos = new ArrayList<>();
    @JsonProperty("total_rating")
    private Double totalRating;
    @JsonProperty("total_rating_count")
    private Integer totalRatingCount;
    @JsonProperty("themes")
    private List<ThemeDto> themes = new ArrayList<>();
    private String slug;
    private String summary;
    private String storyline;
    @JsonProperty("game_type")
    private GameType gameType;
    @JsonProperty("parent_game")
    private DlcInfoDto parentGameInfo;
    @JsonProperty("dlcs")
    private List<DlcInfoDto> dlcs = new ArrayList<>();
    @JsonProperty("expansions")
    private List<DlcInfoDto> expansions = new ArrayList<>();
    @JsonProperty("bundles")
    private List<DlcInfoDto> bundles = new ArrayList<>();
    @JsonProperty("version_parent")
    private DlcInfoDto versionParent;
    @JsonProperty("remakes")
    private List<DlcInfoDto> remakes = new ArrayList<>();
    @JsonProperty("remasters")
    private List<DlcInfoDto> remasters = new ArrayList<>();
    @JsonProperty("similar_games")
    private List<SimilarGameInfoDto> similarGames = new ArrayList<>();
    @JsonProperty("involved_companies")
    private List<InvolvedCompanyDto> involvedCompanies = new ArrayList<>();
    // MODIFICADO: Añadido nuevo campo para el status numérico de IGDB
    @JsonProperty("game_status")
    private GameStatusDto gameStatus;

    @JsonIgnore // Para que no se serialice/deserialice desde JSON automáticamente
    private boolean isFullDetails = true; // Por defecto, asumimos que es completo


}