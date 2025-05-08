package mp.tfg.mycheckpoint.mapper;
import mp.tfg.mycheckpoint.dto.igdb.KeywordDTO;
import mp.tfg.mycheckpoint.entity.Keyword;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface KeywordMapper {
    @Mapping(source = "idigdb", target = "id") KeywordDTO toDto(Keyword entity);
    Set<KeywordDTO> toDtoSet(Set<Keyword> entities);
}