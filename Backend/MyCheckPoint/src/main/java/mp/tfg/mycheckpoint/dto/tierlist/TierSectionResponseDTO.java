package mp.tfg.mycheckpoint.dto.tierlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierSectionResponseDTO {

    @JsonProperty("internal_id")
    private Long internalId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("order")
    private int order;

    @JsonProperty("is_default_unclassified")
    private boolean isDefaultUnclassified;

    @JsonProperty("items")
    private List<TierListItemGameInfoDTO> items;
}