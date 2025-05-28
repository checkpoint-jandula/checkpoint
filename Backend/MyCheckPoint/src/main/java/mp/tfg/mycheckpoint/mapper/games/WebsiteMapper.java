package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.WebsiteDto;
import mp.tfg.mycheckpoint.entity.games.Website;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface WebsiteMapper {

    WebsiteMapper INSTANCE = Mappers.getMapper(WebsiteMapper.class);

    WebsiteDto toDto(Website website);

    Website toEntity(WebsiteDto websiteDto);
}
