package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.GenreDto;
import mp.tfg.mycheckpoint.entity.games.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GenreMapper {

    GenreMapper INSTANCE = Mappers.getMapper(GenreMapper.class);

    GenreDto toDto(Genre genre);

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "games", ignore = true)
    Genre toEntity(GenreDto genreDto);
}