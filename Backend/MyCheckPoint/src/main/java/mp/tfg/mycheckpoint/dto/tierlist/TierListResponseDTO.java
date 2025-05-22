package mp.tfg.mycheckpoint.dto.tierlist;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.TierListType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierListResponseDTO {

    @JsonProperty("public_id")
    private UUID publicId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("type")
    private TierListType type;

    @JsonProperty("source_game_list_public_id")
    private UUID sourceGameListPublicId;

    @JsonProperty("owner_username")
    private String ownerUsername;

    @JsonProperty("is_public")
    private boolean isPublic;

    @JsonProperty("sections")
    private List<TierSectionResponseDTO> sections;

    @JsonProperty("unclassified_section")
    private TierSectionResponseDTO unclassifiedSection;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime updatedAt;
}