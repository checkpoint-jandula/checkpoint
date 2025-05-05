package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import mp.tfg.mycheckpoint.dto.igdb.TemaDTO;
import mp.tfg.mycheckpoint.entity.Tema;

@Mapper(componentModel = "spring")
public interface TemaMapper {

    /**
     * Convierte una entidad {@link Tema} a su DTO {@link TemaDTO}.
     * @param tema La entidad Tema a convertir.
     * @return El DTO TemaDTO resultante.
     */
    TemaDTO toDTO(Tema tema);

    // No se necesita toEntity ni updateEntityFromDTO.
}