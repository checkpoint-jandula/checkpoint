package mp.tfg.mycheckpoint.mapper;
import mp.tfg.mycheckpoint.dto.igdb.LanguageDTO;
import mp.tfg.mycheckpoint.entity.Language;
import org.mapstruct.Mapper;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface LanguageMapper {
    LanguageDTO toDto(Language entity);
    Set<LanguageDTO> toDtoSet(Set<Language> entities);
}