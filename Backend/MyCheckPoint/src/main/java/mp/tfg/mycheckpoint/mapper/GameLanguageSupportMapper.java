package mp.tfg.mycheckpoint.mapper;

import mp.tfg.mycheckpoint.dto.game.GameLanguageSupportDTO;
import mp.tfg.mycheckpoint.entity.GameLanguageSupport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {LanguageMapper.class}) // Usa LanguageMapper si es necesario
public interface GameLanguageSupportMapper {

    // Mapear campos del idioma anidado al DTO aplanado
    @Mapping(source = "language.id", target = "languageId")
    @Mapping(source = "language.nombre", target = "languageName")
    @Mapping(source = "language.locale", target = "languageLocale")
    @Mapping(source = "tipoSoporte", target = "tipoSoporte") // Mapeo directo
    GameLanguageSupportDTO toDto(GameLanguageSupport entity);

    Set<GameLanguageSupportDTO> toDtoSet(Set<GameLanguageSupport> entities);
}