package mp.tfg.mycheckpoint.mapper;
import mp.tfg.mycheckpoint.dto.igdb.ArtworkDTO;
import mp.tfg.mycheckpoint.entity.Artwork;
import org.mapstruct.Mapper;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ArtworkMapper {
    ArtworkDTO toDto(Artwork entity);
    List<ArtworkDTO> toDtoListFromSet(Set<Artwork> entities);
}