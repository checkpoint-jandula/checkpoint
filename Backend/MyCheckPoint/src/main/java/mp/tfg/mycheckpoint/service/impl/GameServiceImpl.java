package mp.tfg.mycheckpoint.service.impl;

import mp.tfg.mycheckpoint.dto.game.GameDTO;
import mp.tfg.mycheckpoint.dto.game.GameSummaryDTO;
import mp.tfg.mycheckpoint.mapper.GameMapper;
import mp.tfg.mycheckpoint.repository.GameRepository;
import mp.tfg.mycheckpoint.service.GameService;
import mp.tfg.mycheckpoint.util.SlugUtil; // Importar SlugUtil
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Para mapear listas

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final GameMapper gameMapper;
    // private final SlugUtil slugUtil; // Asume que tienes esta clase de utilidad

    @Autowired
    public GameServiceImpl(GameRepository gameRepository, GameMapper gameMapper /*, SlugUtil slugUtil */) {
        this.gameRepository = gameRepository;
        this.gameMapper = gameMapper;
        // this.slugUtil = slugUtil;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameDTO> getGameById(Long id) {
        // Usa el método findById del repositorio que definimos con @EntityGraph
        return gameRepository.findById(id)
                .map(gameMapper::toDto); // MapStruct usa los mappers de Genre/Platform
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameDTO> getGameBySlug(String slug) {
        // Cuidado: findBySlug no tiene EntityGraph por defecto, las relaciones serían LAZY
        // Si necesitas relaciones aquí, crea un método en el repo con @EntityGraph
        return gameRepository.findBySlug(slug)
                .map(gameMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSummaryDTO> searchGamesByName(String name) {
        // Implementar búsqueda en repositorio (ej. usando Specification o Query Methods)
        // Por simplicidad, ejemplo básico (esto trae todos y filtra en memoria, ¡NO ÓPTIMO!)
        // Deberías crear un método en GameRepository tipo: List<Game> findByNombreContainingIgnoreCase(String name);
        return gameRepository.findAll().stream() // ¡EJEMPLO BÁSICO, MEJORAR!
                .filter(game -> game.getNombre().toLowerCase().contains(name.toLowerCase()))
                .map(gameMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    // Ejemplo de cómo sería un método create (requiere más lógica):
    /*
    @Override
    @Transactional
    public Optional<GameDTO> createGame(GameCreateDTO createDTO) {
        // Validar si ya existe por id_igdb?
        // Mapear createDTO a Game entidad
        Game game = gameMapper.toEntity(createDTO); // Necesitarías este mapeo
        // Generar slug
        game.setSlug(slugUtil.toSlug(game.getNombre()));
        // Gestionar relaciones (buscar entidades Genero, Plataforma, etc., por ID y añadirlas a los Sets del juego)
        // ... lógica para buscar/crear géneros, plataformas, etc., y asociarlos ...
        Game savedGame = gameRepository.save(game);
        return Optional.of(gameMapper.toDto(savedGame));
    }
    */
}