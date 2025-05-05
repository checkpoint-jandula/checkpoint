package mp.tfg.mycheckpoint.entity.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PlataformaUsuarioId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "plataforma_id")
    private Long plataformaId;
}