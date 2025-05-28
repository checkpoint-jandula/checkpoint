package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO que encapsula el estado de una fecha de lanzamiento específica de un juego,
 * utilizando el ID numérico proporcionado por IGDB.
 * Este ID se utiliza para determinar el {@link mp.tfg.mycheckpoint.dto.enums.ReleaseStatus}.
 * Nota: Este DTO parece incompleto o renombrable si solo lleva el 'status'.
 * Si pretendía llevar la fecha, faltarían campos. Por ahora, se documenta como está.
 * El nombre {@code GameStatusDto} es más preciso si solo lleva el ID del estado.
 */
@Schema(description = "DTO que representa el estado de una fecha de lanzamiento específica de un juego (usualmente para obtener el ID de estado de IGDB).") // Añadido Schema a nivel de clase
@Data
@NoArgsConstructor
@AllArgsConstructor // Añadido por si se usa con todos los args
public class ReleaseDateDto {

    /**
     * El ID numérico del estado de lanzamiento según IGDB para una fecha específica.
     * Este valor se mapeará a un {@link mp.tfg.mycheckpoint.dto.enums.ReleaseStatus}.
     */
    @Schema(description = "ID numérico del estado de lanzamiento según IGDB para una fecha específica.", example = "0") // Añadido Schema
    @JsonProperty("status")
    private Integer status; // Recibe el valor entero directamente de IGDB
}