package mp.tfg.mycheckpoint.service.games;


import mp.tfg.mycheckpoint.dto.games.GameDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class IgdbService {

    private static final Logger logger = LoggerFactory.getLogger(IgdbService.class);
    private final WebClient igdbWebClient;

    private static final String DEFAULT_GAME_FIELDS = """
            fields
                id, name, slug, summary, storyline, first_release_date, total_rating, total_rating_count, game_type,
                cover.id, cover.url,
                artworks.id, artworks.url,
                screenshots.id, screenshots.url,
                videos.id, videos.name, videos.video_id,
                websites.id, websites.url,
                game_modes.id, game_modes.name,
                genres.id, genres.name,
                themes.id, themes.name,
                keywords.id, keywords.name,
                game_engines.id, game_engines.name,
                franchises.id, franchises.name,
                platforms.id, platforms.name, platforms.alternative_name, platforms.platform_logo.id, platforms.platform_logo.url,
                involved_companies.id, involved_companies.developer, involved_companies.publisher, involved_companies.porting, involved_companies.supporting,
                involved_companies.company.id, involved_companies.company.name,
                dlcs.id, dlcs.name, dlcs.slug, dlcs.game_type, dlcs.total_rating, dlcs.cover.id, dlcs.cover.url,
                expansions.id, expansions.name, expansions.slug, expansions.game_type, expansions.total_rating, expansions.cover.id, expansions.cover.url,
                bundles.id, bundles.name, bundles.slug, bundles.game_type, bundles.total_rating, bundles.cover.id, bundles.cover.url,
                similar_games.id, similar_games.name, similar_games.slug, similar_games.summary, similar_games.total_rating, similar_games.cover.id, similar_games.cover.url,
                parent_game.id, parent_game.name, parent_game.slug, parent_game.game_type, parent_game.total_rating, parent_game.cover.id, parent_game.cover.url,
                version_parent.id, version_parent.name, version_parent.slug, version_parent.game_type, version_parent.total_rating, version_parent.cover.id, version_parent.cover.url,
                remakes.id, remakes.name, remakes.slug, remakes.game_type, remakes.total_rating, remakes.cover.id, remakes.cover.url,
                remasters.id, remasters.name, remasters.slug, remasters.game_type, remasters.total_rating, remasters.cover.id, remasters.cover.url,
                game_status.id;
            """;

    @Autowired
    public IgdbService(WebClient igdbWebClient) {
        this.igdbWebClient = igdbWebClient;
    }

    public Flux<GameDto> findGamesByName(String gameName) {
        String queryBody = DEFAULT_GAME_FIELDS + "where name = \"" + gameName + "\"; limit 5;";
        logger.info("Querying IGDB with body: {}", queryBody);
        return igdbWebClient.post()
                .uri("/games")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(queryBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    logger.error("Error from IGDB API: {}, Body: {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("IGDB API Error: " + clientResponse.statusCode() + " - " + errorBody));
                                }))
                .bodyToFlux(GameDto.class)
                .doOnError(error -> logger.error("Error during IGDB call or deserialization: {}", error.getMessage(), error));
    }

    public Mono<GameDto> findGameByIgdbId(Long igdbId) {
        String queryBody = DEFAULT_GAME_FIELDS + "where id = " + igdbId + ";";
        logger.info("Querying IGDB with body: {}", queryBody);
        return igdbWebClient.post()
                .uri("/games")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(queryBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    logger.error("Error from IGDB API: {}, Body: {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("IGDB API Error: " + clientResponse.statusCode() + " - " + errorBody));
                                }))
                .bodyToFlux(GameDto.class)
                .next()
                .doOnError(error -> logger.error("Error during IGDB call or deserialization for ID {}: {}", igdbId, error.getMessage(), error));
    }

    /**
     * Busca juegos en IGDB aplicando filtros dinámicos.
     * Los timestamps de fecha deben ser en formato Unix (segundos).
     * Ejemplo: 1420070400 (1 de enero de 2015) & 1451606399 (31 de diciembre de 2015)
     */
    public Flux<GameDto> findGamesByCustomFilter(
            Long releaseDateStart, Long releaseDateEnd,
            Integer genreId, Integer themeId, Integer gameModeId,
            Integer limit) {

        List<String> whereClauses = new ArrayList<>();

        if (releaseDateStart != null) {
            whereClauses.add("first_release_date >= " + releaseDateStart);
        }
        if (releaseDateEnd != null) {
            whereClauses.add("first_release_date < " + releaseDateEnd);
        }
        if (genreId != null) {
            whereClauses.add("genres = (" + genreId + ")"); // IGDB usa paréntesis para "any of" aunque sea uno solo
        }
        if (themeId != null) {
            whereClauses.add("themes = (" + themeId + ")");
        }
        if (gameModeId != null) {
            whereClauses.add("game_modes = (" + gameModeId + ")");
        }

        String fields = "fields name, total_rating, cover.url, first_release_date, genres.name, themes.name, game_modes.name, id;"; // Añadimos slug y id para consistencia con GameDto
        String whereCondition = whereClauses.isEmpty() ? "" : "where " + String.join(" & ", whereClauses) + ";";
        String limitCondition = (limit != null && limit > 0) ? "limit " + limit + ";" : "limit 10;"; // Límite por defecto

        String queryBody = fields + " " + whereCondition + " " + limitCondition;

        logger.info("Querying IGDB with custom filter body: {}", queryBody);

        return igdbWebClient.post()
                .uri("/games")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(queryBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    logger.error("Error from IGDB API (custom filter): {}, Body: {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("IGDB API Error (custom filter): " + clientResponse.statusCode() + " - " + errorBody));
                                }))
                .bodyToFlux(GameDto.class) // Asumimos que la respuesta puede mapearse a GameDto (o un subconjunto de él)
                .map(gameDto -> {
                    // Si los campos son un subconjunto estricto y quieres marcarlo, aquí podrías hacerlo.
                    // gameDto.setFullDetails(false); // Por ejemplo, si estos filtros siempre devuelven datos parciales.
                    // Sin embargo, GameDto ya tiene campos para cover, name, total_rating, etc.
                    return gameDto;
                })
                .doOnError(error -> logger.error("Error during IGDB custom filter call or deserialization: {}", error.getMessage(), error));
    }
}