package mp.tfg.mycheckpoint.dto.usergame;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.UserGamePersonalPlatform;
import mp.tfg.mycheckpoint.dto.enums.UserGameStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "DTO que representa una entrada en la biblioteca de juegos de un usuario, incluyendo sus datos personales sobre un juego específico.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGameResponseDTO {

    // Podrías incluir el internalId de UserGame si es útil para el frontend
    // @JsonProperty("library_entry_id")
    // private Long libraryEntryId;

    // Información del juego (básica, o podrías anidar un GameSummaryDTO)
    // @JsonProperty("game") // Decidimos más tarde si esto va aquí o en un DTO combinado
    // private GameDto game; // O un GameSummaryDto con menos campos

    @Schema(description = "ID de IGDB del juego al que se refiere esta entrada de la biblioteca.", example = "1020")
    @JsonProperty("game_igdb_id")
    private Long gameIgdbId; // Para identificar el juego

    @Schema(description = "Estado actual del juego en la biblioteca del usuario.", example = "COMPLETED_MAIN_STORY")
    @JsonProperty("status")
    private UserGameStatus status;

    @Schema(description = "Plataforma personal en la que el usuario juega o posee el juego.", example = "PC_STEAM")
    @JsonProperty("personal_platform")
    private UserGamePersonalPlatform personalPlatform;

    @Schema(description = "Indica si el usuario posee el juego.", example = "true")
    @JsonProperty("has_possession")
    private Boolean hasPossession;

    @Schema(description = "Puntuación personal otorgada por el usuario al juego.", example = "9.0", minimum = "0", maximum = "10")
    @JsonProperty("score")
    private Float score;

    @Schema(description = "Comentario público del usuario sobre el juego.", example = "Un clásico indispensable.", nullable = true)
    @JsonProperty("comment")
    private String comment;

    @Schema(description = "Comentario privado del usuario sobre el juego (no visible para otros).", example = "Intentar el final alternativo en la próxima partida.", nullable = true)
    @JsonProperty("private_comment")
    private String privateComment;

    @Schema(description = "Fecha en que el usuario comenzó a jugar (YYYY-MM-DD).", example = "2023-05-10", format = "date", nullable = true)
    @JsonProperty("start_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "Fecha en que el usuario terminó de jugar (YYYY-MM-DD).", example = "2023-07-15", format = "date", nullable = true)
    @JsonProperty("end_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "Horas dedicadas a la historia principal.", example = "55.5", nullable = true)
    @JsonProperty("story_duration_hours")
    private Float storyDurationHours;

    @Schema(description = "Horas dedicadas a la historia principal y secundarias.", example = "80.0", nullable = true)
    @JsonProperty("story_secondary_duration_hours")
    private Float storySecondaryDurationHours;

    @Schema(description = "Horas dedicadas para completar el juego al 100%.", example = "150.0", nullable = true)
    @JsonProperty("completionist_duration_hours")
    private Float completionistDurationHours;

    @Schema(description = "Fecha y hora de creación de esta entrada en la biblioteca.", format = "date-time", example = "2023-05-10T12:00:00.000Z", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime createdAt;

    @Schema(description = "Fecha y hora de la última actualización de esta entrada.", format = "date-time", example = "2023-07-15T18:30:00.000Z", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime updatedAt;
}
