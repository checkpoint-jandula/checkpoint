package mp.tfg.mycheckpoint.dto.igdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Corresponde a Compania en OpenAPI modificado a snake_case
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO {

    @JsonProperty("id") // ID interno de nuestra tabla compania, o id_igdb? Decide qué exponer.
    private Long id; // Por consistencia con otros DTOs, usemos el ID interno nuestro

    @JsonProperty("id_igdb") // Incluimos el id_igdb también
    private Long idigdb;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("developer")
    private Boolean developer;

    @JsonProperty("publisher")
    private Boolean publisher;

    @JsonProperty("porting")
    private Boolean porting;

    @JsonProperty("supporting")
    private Boolean supporting;
}