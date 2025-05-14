package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.GameEngineDto;
import mp.tfg.mycheckpoint.entity.games.GameEngine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GameEngineMapper {

    GameEngineMapper INSTANCE = Mappers.getMapper(GameEngineMapper.class);

    GameEngineDto toDto(GameEngine gameEngine);

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "games", ignore = true) // Ignorar la colección inversa 'games'
    GameEngine toEntity(GameEngineDto gameEngineDto);
}
