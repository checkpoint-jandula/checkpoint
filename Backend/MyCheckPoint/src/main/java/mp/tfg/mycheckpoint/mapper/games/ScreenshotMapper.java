package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.ScreenshotDto;
import mp.tfg.mycheckpoint.entity.games.Screenshot;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ScreenshotMapper {

    ScreenshotMapper INSTANCE = Mappers.getMapper(ScreenshotMapper.class);

    ScreenshotDto toDto(Screenshot screenshot);

    Screenshot toEntity(ScreenshotDto screenshotDto);
}
