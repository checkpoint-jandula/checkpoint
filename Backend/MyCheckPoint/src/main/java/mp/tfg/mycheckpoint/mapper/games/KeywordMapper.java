package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.KeywordDto;
import mp.tfg.mycheckpoint.entity.games.Keyword;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface KeywordMapper {

    KeywordMapper INSTANCE = Mappers.getMapper(KeywordMapper.class);

    KeywordDto toDto(Keyword keyword);

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "games", ignore = true) // Ignorar la colección inversa 'games'
    Keyword toEntity(KeywordDto keywordDto);
}
