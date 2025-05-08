package mp.tfg.mycheckpoint.mapper;

import mp.tfg.mycheckpoint.dto.enums.JuegoRelacionTipoEnum;
import mp.tfg.mycheckpoint.dto.game.GameDTO;
import mp.tfg.mycheckpoint.dto.game.GameSummaryDTO;
import mp.tfg.mycheckpoint.dto.game.RelatedGameDTO; // Importar
import mp.tfg.mycheckpoint.entity.Game;
import mp.tfg.mycheckpoint.entity.GameRelation;   // Importar
import mp.tfg.mycheckpoint.repository.GameRelationRepository; // Necesitamos el repo para buscar relaciones
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired; // Para inyectar repositorio

import java.util.Collections;
import java.util.HashSet; // Para inicializar
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring", uses = {
        GenreMapper.class, PlatformMapper.class, CompanyMapper.class,
        KeywordMapper.class, GameModeMapper.class, ThemeMapper.class,
        GameLanguageSupportMapper.class, ArtworkMapper.class,
        VideoMapper.class, WebMapper.class // Ya no necesitamos GameRelationMapper aquí
})
// Hacemos el mapper abstracto para poder inyectar dependencias (el repositorio)
public abstract class GameMapper {

    // Inyectamos el repositorio para buscar las relaciones
    // MapStruct generará la implementación correcta
    @Autowired
    protected GameRelationRepository gameRelationRepository;

    // Mapeo básico de Game a GameDTO (sin las relaciones juego-a-juego)
    // MapStruct mapeará las colecciones de catálogos usando los otros mappers en 'uses'
    @Mapping(target = "dlcs", ignore = true) // Ignorar en mapeo automático inicial
    @Mapping(target = "expansions", ignore = true)
    @Mapping(target = "similarGames", ignore = true)
    public abstract GameDTO toDto(Game game);

    // Mapeo a Resumen (no necesita las relaciones juego-a-juego)
    public abstract GameSummaryDTO toSummaryDto(Game game);

    // Nuevo método para mapear Game a RelatedGameDTO (usado en @AfterMapping)
    @Mapping(source = "id", target = "id")
    @Mapping(source = "idigdb", target = "idigdb")
    @Mapping(source = "nombre", target = "nombre")
    @Mapping(source = "slug", target = "slug")
    @Mapping(source = "coverUrl", target = "coverUrl")
    @Mapping(source = "gameType", target = "gameType")
    @Mapping(source = "totalRating", target = "totalRating")
    protected abstract RelatedGameDTO gameToRelatedGameDto(Game game);


    // Este método se ejecuta DESPUÉS del mapeo automático de toDto
    @AfterMapping
    protected void afterMappingGameToGameDTO(Game game, @MappingTarget GameDTO gameDTO) {
        if (game == null || game.getId() == null) {
            gameDTO.setDlcs(Collections.emptySet());
            gameDTO.setExpansions(Collections.emptySet());
            gameDTO.setSimilarGames(Collections.emptySet());
            return;
        }

        // Buscar las relaciones salientes para este juego en la BBDD
        Set<GameRelation> outgoingRelations = gameRelationRepository.findByOriginGameId(game.getId());

        // Filtrar y mapear para DLCs
        Set<RelatedGameDTO> dlcs = outgoingRelations.stream()
                .filter(relation -> relation.getRelationType() == JuegoRelacionTipoEnum.DLC)
                .map(GameRelation::getRelatedGame) // Obtener la entidad Game relacionada
                .map(this::gameToRelatedGameDto) // Mapear la entidad Game a RelatedGameDTO
                .collect(Collectors.toSet());
        gameDTO.setDlcs(dlcs);

        // Filtrar y mapear para Expansiones
        Set<RelatedGameDTO> expansions = outgoingRelations.stream()
                .filter(relation -> relation.getRelationType() == JuegoRelacionTipoEnum.EXPANSION)
                .map(GameRelation::getRelatedGame)
                .map(this::gameToRelatedGameDto)
                .collect(Collectors.toSet());
        gameDTO.setExpansions(expansions);

        // Filtrar y mapear para Juegos Similares
        Set<RelatedGameDTO> similarGames = outgoingRelations.stream()
                .filter(relation -> relation.getRelationType() == JuegoRelacionTipoEnum.SIMILAR)
                .map(GameRelation::getRelatedGame)
                .map(this::gameToRelatedGameDto)
                .collect(Collectors.toSet());
        gameDTO.setSimilarGames(similarGames);
    }
}