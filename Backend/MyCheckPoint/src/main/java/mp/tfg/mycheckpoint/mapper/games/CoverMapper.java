package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.CoverDto;
import mp.tfg.mycheckpoint.entity.games.Cover;
import org.mapstruct.Mapper;

import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring") // Para que Spring lo detecte como un Bean y se pueda inyectar
public interface CoverMapper {

    CoverMapper INSTANCE = Mappers.getMapper(CoverMapper.class); // Para uso estático si no se inyecta

    // MapStruct mapea automáticamente campos con el mismo nombre.
    // Cover.igdbId (Embeddable) a CoverDto.igdbId
    // Cover.url a CoverDto.url
    CoverDto toDto(Cover cover);

    Cover toEntity(CoverDto coverDto);
}