package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import mp.tfg.mycheckpoint.dto.igdb.ModoJuegoDTO;
import mp.tfg.mycheckpoint.entity.ModoJuego;

@Mapper(componentModel = "spring")
public interface ModoJuegoMapper {

    /**
     * Convierte una entidad {@link ModoJuego} a su DTO {@link ModoJuegoDTO}.
     * @param modoJuego La entidad ModoJuego a convertir.
     * @return El DTO ModoJuegoDTO resultante.
     */
    ModoJuegoDTO toDTO(ModoJuego modoJuego);

    // No se necesita toEntity ni updateEntityFromDTO.
}