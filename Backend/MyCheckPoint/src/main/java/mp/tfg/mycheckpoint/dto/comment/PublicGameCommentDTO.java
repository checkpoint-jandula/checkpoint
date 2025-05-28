package mp.tfg.mycheckpoint.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicGameCommentDTO {

    @JsonProperty("username")
    private String username;

    @JsonProperty("user_public_id")
    private UUID userPublicId; // Para enlazar al perfil público del usuario

    @JsonProperty("comment_text")
    private String commentText;

    @JsonProperty("comment_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime commentDate; // Usaremos updatedAt de UserGame como fecha del comentario
}