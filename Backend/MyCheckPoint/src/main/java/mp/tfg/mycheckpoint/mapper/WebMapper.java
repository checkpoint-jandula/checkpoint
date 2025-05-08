package mp.tfg.mycheckpoint.mapper;
import mp.tfg.mycheckpoint.dto.igdb.WebDTO;
import mp.tfg.mycheckpoint.entity.Web;
import org.mapstruct.Mapper;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface WebMapper {
    WebDTO toDto(Web entity);
    List<WebDTO> toDtoListFromSet(Set<Web> entities);
}