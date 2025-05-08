package mp.tfg.mycheckpoint.mapper;
import mp.tfg.mycheckpoint.dto.igdb.ThemeDTO;
import mp.tfg.mycheckpoint.entity.Theme;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ThemeMapper {
    @Mapping(source = "idigdbThemeId", target = "id") ThemeDTO toDto(Theme entity);
    Set<ThemeDTO> toDtoSet(Set<Theme> entities);
}