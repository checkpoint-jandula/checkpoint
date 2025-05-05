package mp.tfg.mycheckpoint.dto.juegousuario;

import lombok.*;
import mp.tfg.mycheckpoint.entity.enums.EstadoEnum;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JuegoUsuarioDTO {

    private Long id;
    private EstadoEnum estado; // O String
    private Boolean posesion;
    private Float puntuacion;
    private String comentario;
    private String comentarioPrivado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Float duracionHistoria;
    private Float duracionHistoriaSecundarias;
    private Float duracionCompletista;
    private Boolean importado;
    private Long plataformaId; // Solo el ID de nuestra Plataforma
    private Long usuarioId; // Solo el ID interno del Usuario
    private Long juegoId; // Solo el ID interno del Juego
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    private OffsetDateTime fechaEliminacion;

}