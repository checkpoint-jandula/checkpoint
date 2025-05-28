package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.GameModeDto;
import mp.tfg.mycheckpoint.entity.games.GameMode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GameModeMapper {

    GameModeMapper INSTANCE = Mappers.getMapper(GameModeMapper.class);

    // GameModeDto.igdbId a GameMode.igdbId
    // GameModeDto.name a GameMode.name
    GameModeDto toDto(GameMode gameMode);

    // Ignoramos internalId y games al mapear de DTO a Entidad,
    // internalId es generado por la BDD.
    // 'games' es una relación bidireccional gestionada por JPA.
    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "games", ignore = true)
    GameMode toEntity(GameModeDto gameModeDto);
}