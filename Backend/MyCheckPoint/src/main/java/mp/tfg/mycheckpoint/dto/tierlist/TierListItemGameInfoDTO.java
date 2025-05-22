package mp.tfg.mycheckpoint.dto.tierlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierListItemGameInfoDTO {

    @JsonProperty("tier_list_item_id")
    private Long tierListItemId;

    @JsonProperty("user_game_id")
    private Long userGameId;

    @JsonProperty("game_igdb_id")
    private Long gameIgdbId;

    @JsonProperty("game_name")
    private String gameName;

    @JsonProperty("game_cover_url")
    private String gameCoverUrl;

    @JsonProperty("item_order")
    private int itemOrder;
}