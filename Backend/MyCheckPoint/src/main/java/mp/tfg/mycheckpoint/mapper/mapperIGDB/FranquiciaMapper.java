package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import mp.tfg.mycheckpoint.dto.igdb.FranquiciaDTO;
import mp.tfg.mycheckpoint.entity.Franquicia;

@Mapper(componentModel = "spring")
public interface FranquiciaMapper {

    /**
     * Convierte una entidad {@link Franquicia} a su DTO {@link FranquiciaDTO}.
     * @param franquicia La entidad Franquicia a convertir.
     * @return El DTO FranquiciaDTO resultante.
     */
    FranquiciaDTO toDTO(Franquicia franquicia);

    // No se necesita toEntity ni updateEntityFromDTO.
}