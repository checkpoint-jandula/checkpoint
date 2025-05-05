package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import mp.tfg.mycheckpoint.dto.igdb.PerspectivaJugadorDTO;
import mp.tfg.mycheckpoint.entity.PerspectivaJugador;

@Mapper(componentModel = "spring")
public interface PerspectivaJugadorMapper {

    /**
     * Convierte una entidad {@link PerspectivaJugador} a su DTO {@link PerspectivaJugadorDTO}.
     * @param perspectivaJugador La entidad PerspectivaJugador a convertir.
     * @return El DTO PerspectivaJugadorDTO resultante.
     */
    PerspectivaJugadorDTO toDTO(PerspectivaJugador perspectivaJugador);

    // No se necesita toEntity ni updateEntityFromDTO.
}