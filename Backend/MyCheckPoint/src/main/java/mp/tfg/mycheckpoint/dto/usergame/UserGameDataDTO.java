package mp.tfg.mycheckpoint.dto.usergame;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.UserGamePersonalPlatform;
import mp.tfg.mycheckpoint.dto.enums.UserGameStatus;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGameDataDTO {

    // El igdbId del juego se pasará como path variable en el controlador, no aquí.

    @JsonProperty("status")
    private UserGameStatus status;

    @JsonProperty("personal_platform")
    private UserGamePersonalPlatform personalPlatform;

    @JsonProperty("has_possession")
    private Boolean hasPossession;

    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 10, message = "Score must be at most 10") // O el rango que definas
    @JsonProperty("score")
    private Float score;

    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    @JsonProperty("comment")
    private String comment;

    @Size(max = 2000, message = "Private comment cannot exceed 2000 characters")
    @JsonProperty("private_comment")
    private String privateComment;

    @JsonProperty("start_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonProperty("end_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Min(value = 0, message = "Duration must be non-negative")
    @JsonProperty("story_duration_hours")
    private Float storyDurationHours;

    @Min(value = 0, message = "Duration must be non-negative")
    @JsonProperty("story_secondary_duration_hours")
    private Float storySecondaryDurationHours;

    @Min(value = 0, message = "Duration must be non-negative")
    @JsonProperty("completionist_duration_hours")
    private Float completionistDurationHours;
}
