package mp.tfg.mycheckpoint.controller;

import mp.tfg.mycheckpoint.dto.game.GameDTO;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/juegos") // Base path según OpenAPI
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    // GET /juegos/{id} - Obtener detalles de un juego por ID interno
    // operationId: getJuegoById (modificado para usar slug también)
    // Nota: OpenAPI usa {id} (Long). Podrías querer usar el {slug} para URLs más amigables.
    // Implementaremos ambos.

    @GetMapping("/{id}")
    public ResponseEntity<GameDTO> getJuegoById(@PathVariable Long id) {
        return gameService.getGameById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Juego no encontrado con ID: " + id));
    }

    // Endpoint adicional para buscar por Slug (más útil para URLs públicas)
    @GetMapping("/slug/{slug}")
    public ResponseEntity<GameDTO> getJuegoBySlug(@PathVariable String slug) {
        return gameService.getGameBySlug(slug)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Juego no encontrado con Slug: " + slug));
    }

    // (Aquí irían endpoints para buscar, crear, actualizar juegos si los implementas)
    // Ejemplo búsqueda básica por nombre
    // @GetMapping("/buscar")
    // public ResponseEntity<List<GameSummaryDTO>> buscarJuegosPorNombre(@RequestParam String nombre) {
    //    return ResponseEntity.ok(gameService.searchGamesByName(nombre));
    // }
}
