package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvolvedCompanyDto {
    @JsonProperty("id")
    private Long involvementIgdbId; // ID de la relación de involucramiento en sí

    private CompanyInfoDto company;

    private boolean developer;
    private boolean porting;
    private boolean publisher;
    private boolean supporting;
}
