package mp.tfg.mycheckpoint.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameLanguageSupportId implements Serializable {
    private Long game; // El nombre debe coincidir con el campo en GameLanguageSupport
    private Long language; // El nombre debe coincidir con el campo en GameLanguageSupport
    private Integer tipoSoporte;
}
