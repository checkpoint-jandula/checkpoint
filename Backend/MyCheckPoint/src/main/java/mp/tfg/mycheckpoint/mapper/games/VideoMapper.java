package mp.tfg.mycheckpoint.mapper.games;



import mp.tfg.mycheckpoint.dto.games.VideoDto;
import mp.tfg.mycheckpoint.entity.games.Video;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    VideoMapper INSTANCE = Mappers.getMapper(VideoMapper.class);

    VideoDto toDto(Video video);

    // MapStruct debería manejar el mapeo de videoId (DTO) a videoId (Entidad)
    // si los nombres de campo después de la conversión snake_case a camelCase coinciden.
    // Si el campo en VideoDto fuera "video_id" y en Video fuera "videoId",
    // MapStruct lo haría automáticamente. Como ambos son "videoId" (uno por @JsonProperty), está bien.
    Video toEntity(VideoDto videoDto);
}
