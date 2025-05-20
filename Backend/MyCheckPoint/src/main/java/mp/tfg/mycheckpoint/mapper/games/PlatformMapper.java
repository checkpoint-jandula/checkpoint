package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.PlatformDto;
import mp.tfg.mycheckpoint.dto.games.PlatformLogoDto;
import mp.tfg.mycheckpoint.entity.games.Platform;
import mp.tfg.mycheckpoint.entity.games.PlatformLogo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {PlatformLogoMapper.class}) // Indicar que usa PlatformLogoMapper
public interface PlatformMapper {
    PlatformMapper INSTANCE = Mappers.getMapper(PlatformMapper.class);

    // MapStruct usará PlatformLogoMapper para el campo platformLogo
    PlatformDto toDto(Platform platform);

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "games", ignore = true)
    Platform toEntity(PlatformDto platformDto);


}
