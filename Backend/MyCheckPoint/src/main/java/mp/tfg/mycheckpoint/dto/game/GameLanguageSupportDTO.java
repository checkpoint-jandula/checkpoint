package mp.tfg.mycheckpoint.dto.game; // O dto.igdb si prefieres
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data; @Data
public class GameLanguageSupportDTO {
    // Incluimos info del idioma directamente aquí
    @JsonProperty("idioma_id") private Long languageId; // ID interno del idioma
    @JsonProperty("idioma_nombre") private String languageName;
    @JsonProperty("idioma_locale") private String languageLocale;
    @JsonProperty("tipo_soporte") private Integer tipoSoporte; // 1, 2, 3
}