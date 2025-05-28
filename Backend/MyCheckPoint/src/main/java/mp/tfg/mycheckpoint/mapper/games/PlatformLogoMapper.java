package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.PlatformLogoDto;
import mp.tfg.mycheckpoint.entity.games.PlatformLogo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PlatformLogoMapper {
    PlatformLogoMapper INSTANCE = Mappers.getMapper(PlatformLogoMapper.class);

    PlatformLogoDto toDto(PlatformLogo platformLogo);
    PlatformLogo toEntity(PlatformLogoDto platformLogoDto);
}
