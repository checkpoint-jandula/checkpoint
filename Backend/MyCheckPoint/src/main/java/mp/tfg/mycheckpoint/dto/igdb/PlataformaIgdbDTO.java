package mp.tfg.mycheckpoint.dto.igdb;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlataformaIgdbDTO {

    private Long id; // Opcional
    private Long idigdbPlatformId; // Coincide con el nombre en la entidad
    private String nombre;
    private String abreviatura; // Añadido ya que está en la entidad PlataformaIGDB
    private String logoUrl; // Añadido ya que está en la entidad PlataformaIGDB

}