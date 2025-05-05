package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import mp.tfg.mycheckpoint.dto.igdb.IdiomaDTO;
import mp.tfg.mycheckpoint.entity.Idioma;

@Mapper(componentModel = "spring")
public interface IdiomaMapper {

    /**
     * Convierte una entidad {@link Idioma} a su DTO {@link IdiomaDTO}.
     * @param idioma La entidad Idioma a convertir.
     * @return El DTO IdiomaDTO resultante.
     */
    IdiomaDTO toDTO(Idioma idioma);

    // No se necesita toEntity ni updateEntityFromDTO.
}