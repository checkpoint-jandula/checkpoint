package mp.tfg.mycheckpoint.controller;


import mp.tfg.mycheckpoint.dto.games.GameDto;
import mp.tfg.mycheckpoint.entity.games.Game;
import mp.tfg.mycheckpoint.mapper.games.GameMapper;
import mp.tfg.mycheckpoint.service.games.GameService;
import mp.tfg.mycheckpoint.service.games.IgdbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/juegos")
public class GameController {

    private final GameService gameService;
    private final GameMapper gameMapper;
    private final IgdbService igdbService;
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    public GameController(GameService gameService, GameMapper gameMapper, IgdbService igdbService) {
        this.gameService = gameService;
        this.gameMapper = gameMapper;
        this.igdbService = igdbService;
    }

    @PostMapping
    public ResponseEntity<List<GameDto>> addGames(@RequestBody List<GameDto> gameDtos) {
        // Asumimos que los DTOs que llegan aquí son para crear nuevos juegos o actualizar
        // completamente. Si vienen de una fuente externa directa, deberían ser 'fullDetails'.
        // Si es un cliente el que los construye, él es responsable de su completitud.
        // GameService ya maneja la lógica de isFullDetails si se setea en el DTO.
        // Por defecto, un GameDto se inicializa con isFullDetails = true.
        gameDtos.forEach(dto -> dto.setFullDetails(true)); // Aseguramos que se marquen como completos si es la intención.

        List<Game> savedGameEntities = gameService.saveGames(gameDtos);
        List<GameDto> savedResultDtos = savedGameEntities.stream()
                .map(gameMapper::toDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(savedResultDtos, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GameDto>> getAllGames() {
        List<Game> games = gameService.getAllGamesOriginal();
        List<GameDto> gameDtos = games.stream()
                .map(gameMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gameDtos);
    }

    @GetMapping("/igdb/{igdbId}")
    public ResponseEntity<GameDto> getGameByIgdbId(@PathVariable Long igdbId) {
        Game game = gameService.getGameByIgdbIdOriginal(igdbId);
        if (game != null) {
            return ResponseEntity.ok(gameMapper.toDto(game));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/igdb/buscar")
    public Flux<GameDto> buscarJuegosEnIgdb(@RequestParam String nombre) {
        return igdbService.findGamesByName(nombre)
                .map(gameDto -> {
                    // Los DTOs que vienen de una búsqueda general pueden ser resúmenes.
                    // Si queremos que al guardarlos se traten como parciales por defecto:
                    // gameDto.setFullDetails(false);
                    // Sin embargo, findGamesByName en IgdbService usa DEFAULT_GAME_FIELDS,
                    // así que podrían ser completos. Depende de tu definición.
                    // Por ahora, dejamos que el valor por defecto de GameDto (isFullDetails=true)
                    // o lo que IgdbService devuelva se mantenga.
                    // Si se guardasen directamente desde aquí, habría que considerarlo.
                    return gameDto;
                });
    }

    @PostMapping("/igdb/sincronizar/{igdbId}")
    public Mono<ResponseEntity<GameDto>> sincronizarJuegoDesdeIgdb(@PathVariable Long igdbId) {
        return igdbService.findGameByIgdbId(igdbId)
                .flatMap(gameDtoFromIgdb -> {
                    gameDtoFromIgdb.setFullDetails(true);
                    logger.info("GameDto recibido de IGDB para sincronizar (ID: {}): Name='{}', isFullDetails={}",
                            gameDtoFromIgdb.getIgdbId(), gameDtoFromIgdb.getName(), gameDtoFromIgdb.isFullDetails());
                    try {
                        // saveGames ahora devuelve entidades con colecciones inicializadas
                        List<Game> savedGameEntities = gameService.saveGames(List.of(gameDtoFromIgdb));

                        if (!savedGameEntities.isEmpty() && savedGameEntities.get(0) != null) {
                            Game savedGameEntity = savedGameEntities.get(0);
                            // El mapeo aquí debería funcionar ahora
                            GameDto responseDto = gameMapper.toDto(savedGameEntity);

                            logger.info("Juego sincronizado y guardado correctamente (IGDB ID: {}).", responseDto.getIgdbId());
                            return Mono.just(ResponseEntity.ok(responseDto));
                        } else {
                            // ... (manejo de error) ...
                            logger.error("gameService.saveGames no devolvió una entidad válida para IGDB ID: {}", igdbId);
                            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<GameDto>build());
                        }
                    } catch (Exception e) {
                        // ... (manejo de error) ...
                        logger.error("Excepción durante gameService.saveGames o gameMapper.toDto para IGDB ID: {}", igdbId, e);
                        return Mono.error(e);
                    }
                })
                // ... (switchIfEmpty y onErrorResume) ...
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Juego no encontrado en IGDB (ID: {}) para sincronizar.", igdbId);
                    return Mono.just(ResponseEntity.notFound().<GameDto>build());
                }))
                .onErrorResume(e -> {
                    logger.error("Error en el flujo de sincronización para IGDB ID: {}. Error: {}. Tipo Excepción: {}", igdbId, e.getMessage(), e.getClass().getName(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<GameDto>build());
                });
    }
}