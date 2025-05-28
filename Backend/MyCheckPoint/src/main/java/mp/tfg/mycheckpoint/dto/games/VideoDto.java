package mp.tfg.mycheckpoint.dto.games;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO que representa un vídeo asociado a un juego (ej. tráiler, gameplay).
 */
@Schema(description = "DTO para un vídeo asociado a un juego.") // Añadido Schema a nivel de clase
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
    /**
     * ID único del vídeo, generalmente proveniente de una fuente externa como IGDB.
     */
    @Schema(description = "ID del vídeo desde la fuente externa (ej. IGDB).", example = "4567") // Añadido Schema
    @JsonProperty("id")
    private Long igdbId;

    /**
     * Nombre o título del vídeo.
     */
    @Schema(description = "Nombre o título del vídeo.", example = "Official Trailer") // Añadido Schema
    private String name;

    /**
     * Identificador del vídeo en la plataforma donde está alojado (ej. ID de YouTube).
     */
    @Schema(description = "Identificador del vídeo en la plataforma de origen (ej. ID de YouTube).", example = "dQw4w9WgXcQ") // Añadido Schema
    @JsonProperty("video_id") // Para mapear la clave JSON "video_id"
    private String videoId;
}
