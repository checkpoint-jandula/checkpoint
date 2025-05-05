package mp.tfg.mycheckpoint.dto.niveltier;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NivelTierDTO {

    private Long id;
    private Long tierlistId; // ID de la TierList a la que pertenece
    private String nombre;
    private Integer orden;
    private String color;
    private String descripcion;

    // No incluimos la TierList completa ni los juegos asociados en este DTO básico.
    // Los juegos se incluyen en el DTO de detalle de TierList.
}