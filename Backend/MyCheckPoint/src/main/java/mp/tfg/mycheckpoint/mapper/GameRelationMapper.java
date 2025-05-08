package mp.tfg.mycheckpoint.mapper;

import mp.tfg.mycheckpoint.dto.game.GameRelationInfoDTO;
import mp.tfg.mycheckpoint.entity.GameRelation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named; // Importar @Named
import org.mapstruct.IterableMapping; // Importar @IterableMapping
import java.util.Set;

@Mapper(componentModel = "spring", uses = {GameMapper.class})
public interface GameRelationMapper {

    @Named("mapOutgoingRelation") // Nombrar este método
    @Mapping(source = "relatedGame", target = "relatedGame")
    @Mapping(source = "relationType", target = "relationType")
    GameRelationInfoDTO toDtoFromOutgoing(GameRelation relation);

    @Named("mapIncomingRelation") // Nombrar este método
    @Mapping(source = "originGame", target = "relatedGame") // Mapea originGame a relatedGame del DTO
    @Mapping(source = "relationType", target = "relationType")
    GameRelationInfoDTO toDtoFromIncoming(GameRelation relation);

    @IterableMapping(qualifiedByName = "mapOutgoingRelation") // Usar el método nombrado correcto
    Set<GameRelationInfoDTO> toDtoSetFromOutgoing(Set<GameRelation> relations);

    @IterableMapping(qualifiedByName = "mapIncomingRelation") // Usar el método nombrado correcto
    Set<GameRelationInfoDTO> toDtoSetFromIncoming(Set<GameRelation> relations);
}