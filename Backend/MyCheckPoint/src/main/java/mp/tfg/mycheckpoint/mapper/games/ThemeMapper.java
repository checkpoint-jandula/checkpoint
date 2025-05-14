package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.ThemeDto;
import mp.tfg.mycheckpoint.entity.games.Theme;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ThemeMapper {

    ThemeMapper INSTANCE = Mappers.getMapper(ThemeMapper.class);

    ThemeDto toDto(Theme theme);

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "games", ignore = true)
    Theme toEntity(ThemeDto themeDto);
}
