package mp.tfg.mycheckpoint.dto.amistad;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmistadDTO {

    // Nota: La entidad Amistad usa un EmbeddedId (AmistadId) con usuarioId y amigoId.
    // En el DTO, podemos exponer estos IDs directamente o anidar un objeto ID si fuera necesario.
    // Exponerlos directamente suele ser más sencillo para DTOs.

    private Long usuarioId; // ID del usuario que inició la relación o es el "lado izquierdo"
    private Long amigoId; // ID del usuario que es el amigo o es el "lado derecho"

    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    private OffsetDateTime fechaEliminacion; // Para soft delete, puede ser null

    // Opcionalmente, podrías incluir resúmenes básicos de los usuarios para no tener que buscarlos aparte
    // private UsuarioSummaryDTO usuario;
    // private UsuarioSummaryDTO amigo;
    // Esto dependerá de si necesitas devolver info de los usuarios en la respuesta de Amistad
}