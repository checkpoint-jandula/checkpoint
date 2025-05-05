package mp.tfg.mycheckpoint.dto.juegousuario;

import lombok.*;
import mp.tfg.mycheckpoint.dto.juego.JuegoSummaryDTO; // O JuegoDTO si quieres todos los detalles
import mp.tfg.mycheckpoint.dto.plataforma.PlataformaDTO; // O PlataformaSummaryDTO si lo creas
import mp.tfg.mycheckpoint.entity.enums.EstadoEnum;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Hereda de JuegoUsuarioDTO para no repetir campos, o lista todos los campos
public class JuegoUsuarioDetalleDTO extends JuegoUsuarioDTO { // Extender de DTO básico es una opción

    // Incluir detalles de las entidades relacionadas que se quieren mostrar
    private JuegoSummaryDTO juego; // Detalles resumidos del juego
    private PlataformaDTO plataforma; // Detalles de nuestra Plataforma (si está asociada)

    // No es necesario listar los campos del DTO base si se hereda
    // Pero si no heredas, lista todos los campos de JuegoUsuarioDTO aquí + los de abajo
    /*
    private Long id;
    private EstadoEnum estado;
    // ... otros campos de JuegoUsuario ...
    */

}