package mp.tfg.mycheckpoint.service;

import mp.tfg.mycheckpoint.dto.game.GameDTO;
import mp.tfg.mycheckpoint.dto.game.GameSummaryDTO;

import java.util.List;
import java.util.Optional;

public interface GameService {
    Optional<GameDTO> getGameById(Long id); // Usa el findById con EntityGraph
    Optional<GameDTO> getGameBySlug(String slug);
    // Podrías añadir métodos para buscar/listar juegos, crear, actualizar...
    // Optional<GameDTO> createGame(GameCreateDTO createDTO); // Si implementas creación
    List<GameSummaryDTO> searchGamesByName(String name); // Ejemplo búsqueda
}