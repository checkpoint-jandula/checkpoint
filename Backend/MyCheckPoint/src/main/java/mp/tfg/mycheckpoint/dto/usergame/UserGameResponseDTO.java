package mp.tfg.mycheckpoint.dto.usergame;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.UserGamePersonalPlatform;
import mp.tfg.mycheckpoint.dto.enums.UserGameStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

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

    @JsonProperty("game_igdb_id")
    private Long gameIgdbId; // Para identificar el juego

    @JsonProperty("status")
    private UserGameStatus status;

    @JsonProperty("personal_platform")
    private UserGamePersonalPlatform personalPlatform;

    @JsonProperty("has_possession")
    private Boolean hasPossession;

    @JsonProperty("score")
    private Float score;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("private_comment")
    private String privateComment;

    @JsonProperty("start_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonProperty("end_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @JsonProperty("story_duration_hours")
    private Float storyDurationHours;

    @JsonProperty("story_secondary_duration_hours")
    private Float storySecondaryDurationHours;

    @JsonProperty("completionist_duration_hours")
    private Float completionistDurationHours;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime updatedAt;
}
