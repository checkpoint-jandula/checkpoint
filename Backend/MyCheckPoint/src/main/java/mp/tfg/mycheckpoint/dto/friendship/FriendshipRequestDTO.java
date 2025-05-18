package mp.tfg.mycheckpoint.dto.friendship;

// Este DTO podría no ser necesario si el targetPublicId se pasa como PathVariable.
// Si se pasara en el body, sería así:
/*
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipRequestDTO {
    @NotNull
    @JsonProperty("receiver_public_id")
    private UUID receiverPublicId;
}
*/
// Por ahora, asumiremos que el ID del receptor va en el Path, por lo que no necesitamos este DTO específico.