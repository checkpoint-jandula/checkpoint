package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import mp.tfg.mycheckpoint.dto.igdb.VideoDTO;
import mp.tfg.mycheckpoint.entity.Video;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    /**
     * Convierte una entidad {@link Video} a su DTO {@link VideoDTO}.
     * @param video La entidad Video a convertir.
     * @return El DTO VideoDTO resultante.
     */
    VideoDTO toDTO(Video video);

    // No se necesita toEntity ni updateEntityFromDTO.
}