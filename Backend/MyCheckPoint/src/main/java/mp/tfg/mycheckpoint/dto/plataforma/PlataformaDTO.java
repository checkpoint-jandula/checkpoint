package mp.tfg.mycheckpoint.dto.plataforma;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlataformaDTO {

    private Long id;
    private Integer idigdb; // El ID externo de referencia (¿de IGDB?)
    private String nombre;
    private String nombreAlternativo;
    private String logoUrl;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    private OffsetDateTime fechaEliminacion; // Para soft delete, puede ser null

    // No incluimos las colecciones de relaciones (plataformasUsuario, juegosUsuario) aquí
    // para mantener el DTO simple y evitar ciclos/cargas excesivas.
    // Si se necesita, se podría crear un DTO de detalle de Plataforma.
}