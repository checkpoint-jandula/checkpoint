package mp.tfg.mycheckpoint.mapper.tierlist;

import mp.tfg.mycheckpoint.dto.tierlist.TierListItemGameInfoDTO;
import mp.tfg.mycheckpoint.entity.TierListItem;
import mp.tfg.mycheckpoint.entity.games.Game; // Asegúrate que la entidad Game esté importada
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TierListItemMapper {

    @Mapping(target = "tierListItemId", source = "tierListItem.internalId")
    @Mapping(target = "userGameId", source = "tierListItem.userGame.internalId")
    @Mapping(target = "gameIgdbId", source = "tierListItem.userGame.game.igdbId")
    @Mapping(target = "gameName", source = "tierListItem.userGame.game.name")
    @Mapping(target = "gameCoverUrl", source = "tierListItem.userGame.game", qualifiedByName = "gameToCoverUrlTierList")
    @Mapping(target = "itemOrder", source = "tierListItem.itemOrder")
    TierListItemGameInfoDTO toGameInfoDTO(TierListItem tierListItem);

    List<TierListItemGameInfoDTO> toGameInfoDTOList(List<TierListItem> tierListItems);

    @Named("gameToCoverUrlTierList") // Renombrado para evitar colisión si existe otro similar
    default String gameToCoverUrlTierList(Game game) {
        if (game != null && game.getCover() != null) {
            return game.getCover().getUrl();
        }
        return null; // O una URL por defecto para la carátula
    }
}