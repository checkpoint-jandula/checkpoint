package mp.tfg.mycheckpoint.dto.tierlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierListCreateDTO {

    @NotBlank(message = "El nombre de la Tier List no puede estar vacío")
    @Size(max = 255, message = "El nombre de la Tier List no puede exceder los 255 caracteres")
    private String nombre;

    // La OpenAPI sugiere opcionalmente permitir crear niveles iniciales aquí.
    // Si decides hacerlo, necesitarías un campo como:
    // private List<NivelTierCreateDTO> nivelesIniciales; // Necesitarías un NivelTierCreateDTO simple sin referencia a TierList

    // El usuario_id se obtiene del contexto de seguridad o de la URL.
}