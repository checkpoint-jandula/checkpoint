package mp.tfg.mycheckpoint.mapper;
import mp.tfg.mycheckpoint.dto.igdb.VideoDTO;
import mp.tfg.mycheckpoint.entity.Video;
import org.mapstruct.Mapper;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface VideoMapper {
    VideoDTO toDto(Video entity);
    List<VideoDTO> toDtoListFromSet(Set<Video> entities);
}