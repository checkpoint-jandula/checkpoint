package mp.tfg.mycheckpoint.dto.gameList;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameListRequestDTO { // Usado para crear y actualizar

    @NotBlank(message = "List name cannot be blank")
    @Size(min = 1, max = 150, message = "List name must be between 1 and 150 characters")
    @JsonProperty("name")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @JsonProperty("description")
    private String description; // Opcional

    @NotNull(message = "Public status cannot be null")
    @JsonProperty("is_public")
    private Boolean isPublic;
}
