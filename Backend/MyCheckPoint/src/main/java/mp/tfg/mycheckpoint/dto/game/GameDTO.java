package mp.tfg.mycheckpoint.dto.game;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.GameTypeEnum;
import mp.tfg.mycheckpoint.dto.enums.StatusEnum;
import mp.tfg.mycheckpoint.dto.igdb.GenreDTO; // Usar DTOs de catálogo
import mp.tfg.mycheckpoint.dto.platform.PlatformDTO;
import mp.tfg.mycheckpoint.dto.igdb.*; // Importar nuevos DTOs
import mp.tfg.mycheckpoint.dto.platform.PlatformDTO;// Usar DTOs de catálogo
import mp.tfg.mycheckpoint.dto.game.GameRelationInfoDTO;
import mp.tfg.mycheckpoint.dto.game.RelatedGameDTO;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.List; // Para Artwork, Video, Web


// Corresponde a Juego en OpenAPI modificado a snake_case
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {
    @JsonProperty("id")
    private Long id; // ID interno
    @JsonProperty("id_igdb")
    private Long idigdb;
    @JsonProperty("nombre")
    private String nombre;
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("game_type")
    private GameTypeEnum gameType;
    @JsonProperty("resumen")
    private String resumen;
    @JsonProperty("historia")
    private String historia;
    @JsonProperty("total_rating")
    private Float totalRating;
    @JsonProperty("total_rating_count")
    private Integer totalRatingCount;
    @JsonProperty("fecha_lanzamiento")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaLanzamiento;
    @JsonProperty("cover_url")
    private String coverUrl;
    @JsonProperty("motor_grafico")
    private String motorGrafico;
    @JsonProperty("franquicia")
    private String franquicia;
    @JsonProperty("status")
    private StatusEnum status;

    // --- Relaciones ---
    @JsonProperty("generos")
    private Set<GenreDTO> genres;

    @JsonProperty("plataformas")
    private Set<PlatformDTO> platforms;

    @JsonProperty("companias") // Nombre de la propiedad JSON
    private Set<CompanyDTO> companies; // Nombre del campo Java

    @JsonProperty("keywords")
    private Set<KeywordDTO> keywords;

    @JsonProperty("modos_juego") // Nombre de la propiedad JSON
    private Set<GameModeDTO> gameModes; // Nombre del campo Java

    @JsonProperty("temas") // Nombre de la propiedad JSON
    private Set<ThemeDTO> themes; // Nombre del campo Java

    @JsonProperty("soporte_idiomas") // Decide cómo llamar a esto
    private Set<GameLanguageSupportDTO> languageSupports; // DTO para la relación con tipo soporte

    @JsonProperty("imagenes") // Nombre de la propiedad JSON
    private List<ArtworkDTO> artworks; // Nombre del campo Java (Lista podría tener orden)

    @JsonProperty("videos")
    private List<VideoDTO> videos;

    @JsonProperty("webs")
    private List<WebDTO> webs;


    // --- Relaciones Juego-a-Juego (Nuevo estilo) ---
    @JsonProperty("dlcs")
    private Set<RelatedGameDTO> dlcs = new HashSet<>(); // Inicializar para evitar nulls

    @JsonProperty("expansiones")
    private Set<RelatedGameDTO> expansions = new HashSet<>(); // Inicializar

    @JsonProperty("juegos_similares") // Nombre consistente con IGDB 'similar_games'
    private Set<RelatedGameDTO> similarGames = new HashSet<>(); // Inicializar
}