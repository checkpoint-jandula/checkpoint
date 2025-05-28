package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.ArtworkDto;
import mp.tfg.mycheckpoint.entity.games.Artwork;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ArtworkMapper {

    ArtworkMapper INSTANCE = Mappers.getMapper(ArtworkMapper.class);

    ArtworkDto toDto(Artwork artwork);

    Artwork toEntity(ArtworkDto artworkDto);
}