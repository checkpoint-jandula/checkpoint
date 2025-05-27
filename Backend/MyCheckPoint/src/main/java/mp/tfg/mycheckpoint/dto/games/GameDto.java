package mp.tfg.mycheckpoint.dto.games;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import mp.tfg.mycheckpoint.dto.enums.GameType;
import mp.tfg.mycheckpoint.dto.enums.ReleaseStatus;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameDto {
    @Schema(description = "ID único del juego en IGDB.", example = "1020")
    @JsonProperty("id")
    private Long igdbId;

    @Schema(description = "Información de la carátula del juego.")
    private CoverDto cover;

    @Schema(description = "Modos de juego disponibles (ej. Un jugador, Multijugador).", nullable = true)
    @JsonProperty("game_modes")
    private List<GameModeDto> gameModes = new ArrayList<>();

    @Schema(description = "Géneros a los que pertenece el juego (ej. RPG, Acción).", nullable = true)
    private List<GenreDto> genres = new ArrayList<>();

    @Schema(description = "Información de los artes del juego.", nullable = true)
    @JsonProperty("artworks")
    private List<ArtworkDto> artworks = new ArrayList<>();

    @Schema(description = "Nombre completo del juego.", example = "The Legend of Zelda: Breath of the Wild")
    private String name;

    @Schema(description = "Fecha del primer lanzamiento del juego (timestamp Unix en segundos).", example = "1488499200", format = "int64", nullable = true)
    @JsonProperty("first_release_date")
    private Long firstReleaseDate;

    @Schema(description = "Estado de lanzamiento del juego.", nullable = true)
    @JsonProperty("first_release_status")
    private ReleaseStatus firstReleaseStatus;

    @Schema(description = "Franquicias a las que pertenece el juego.", nullable = true)
    @JsonProperty("franchises")
    private List<FranchiseDto> franchises = new ArrayList<>();

    @Schema(description = "Motores de juego utilizados.", nullable = true)
    @JsonProperty("game_engines")
    private List<GameEngineDto> gameEngines = new ArrayList<>();

    @Schema(description = "Palabras clave asociadas al juego.", nullable = true)
    @JsonProperty("keywords")
    private List<KeywordDto> keywords = new ArrayList<>();

    @Schema(description = "Plataformas en las que el juego está disponible.", nullable = true)
    @JsonProperty("platforms")
    private List<PlatformDto> platforms = new ArrayList<>();

    @Schema(description = "Capturas de pantalla del juego.", nullable = true)
    @JsonProperty("screenshots")
    private List<ScreenshotDto> screenshots = new ArrayList<>();

    @Schema(description = "Sitios web relacionados con el juego.", nullable = true)
    @JsonProperty("websites")
    private List<WebsiteDto> websites = new ArrayList<>();

    @Schema(description = "Vídeos relacionados con el juego.", nullable = true)
    @JsonProperty("videos")
    private List<VideoDto> videos = new ArrayList<>();

    @Schema(description = "Calificación total del juego (promedio de usuarios/críticos).", example = "97.0", nullable = true)
    @JsonProperty("total_rating")
    private Double totalRating;

    @Schema(description = "Número total de calificaciones recibidas.", example = "2500", nullable = true)
    @JsonProperty("total_rating_count")
    private Integer totalRatingCount;

    @Schema(description = "Temas principales del juego (ej. Fantasía, Ciencia Ficción).", nullable = true)
    @JsonProperty("themes")
    private List<ThemeDto> themes = new ArrayList<>();

    @Schema(description = "Identificador URL amigable del juego.", example = "the-legend-of-zelda-breath-of-the-wild", nullable = true)
    private String slug;

    @Schema(description = "Resumen o sinopsis del juego.", example = "Una aventura épica en un vasto mundo abierto...", nullable = true)
    private String summary;

    @Schema(description = "Argumento o historia principal del juego.", example = "Link despierta después de 100 años...", nullable = true)
    private String storyline;

    @Schema(description = "Tipo de juego (ej. JUEGO_PRINCIPAL, DLC, EXPANSION).", example = "GAME")
    @JsonProperty("game_type")
    private GameType gameType;

    @Schema(description = "Información del juego padre, si este es una expansión o DLC.", nullable = true)
    @JsonProperty("parent_game")
    private DlcInfoDto parentGameInfo;

    @Schema(description = "Lista de DLCs para este juego.", nullable = true)
    @JsonProperty("dlcs")
    private List<DlcInfoDto> dlcs = new ArrayList<>();

    @Schema(description = "Lista de expansiones para este juego.", nullable = true)
    @JsonProperty("expansions")
    private List<DlcInfoDto> expansions = new ArrayList<>();

    @Schema(description = "Lista de paquetes/bundles que incluyen este juego o de los que este juego forma parte.", nullable = true)
    @JsonProperty("bundles")
    private List<DlcInfoDto> bundles = new ArrayList<>();

    @Schema(description = "Información del juego base si este es una versión específica (ej. Edición GOTY).", nullable = true)
    @JsonProperty("version_parent")
    private DlcInfoDto versionParent;

    @Schema(description = "Lista de remakes de este juego.", nullable = true)
    @JsonProperty("remakes")
    private List<DlcInfoDto> remakes = new ArrayList<>();

    @Schema(description = "Lista de remasters de este juego.", nullable = true)
    @JsonProperty("remasters")
    private List<DlcInfoDto> remasters = new ArrayList<>();

    @Schema(description = "Lista de juegos similares a este.", nullable = true)
    @JsonProperty("similar_games")
    private List<SimilarGameInfoDto> similarGames = new ArrayList<>();

    @Schema(description = "Compañías involucradas en el desarrollo/publicación del juego.", nullable = true)
    @JsonProperty("involved_companies")
    private List<InvolvedCompanyDto> involvedCompanies = new ArrayList<>();

    @Schema(description = "Estado numérico del juego según IGDB (mapeado a ReleaseStatus).", nullable = true)
    @JsonProperty("game_status")
    private GameStatusDto gameStatus;

    @JsonIgnore // Para que no se serialice/deserialice desde JSON automáticamente
    @Schema(hidden = true) // Oculta este campo de la documentación ya que es interno
    private boolean isFullDetails = true; // Por defecto, asumimos que es completo


}