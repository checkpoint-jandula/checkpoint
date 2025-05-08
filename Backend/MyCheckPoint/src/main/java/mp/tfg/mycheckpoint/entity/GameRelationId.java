package mp.tfg.mycheckpoint.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mp.tfg.mycheckpoint.dto.enums.JuegoRelacionTipoEnum;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRelationId implements Serializable {
    private Long originGame; // Nombre coincide con el campo en GameRelation
    private Long relatedGame; // Nombre coincide con el campo en GameRelation
    private JuegoRelacionTipoEnum relationType;
}