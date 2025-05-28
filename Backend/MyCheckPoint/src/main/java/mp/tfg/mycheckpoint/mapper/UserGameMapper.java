package mp.tfg.mycheckpoint.mapper;

import mp.tfg.mycheckpoint.dto.usergame.UserGameDataDTO;
import mp.tfg.mycheckpoint.dto.usergame.UserGameResponseDTO;
import mp.tfg.mycheckpoint.entity.UserGame;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserGameMapper {

    UserGameMapper INSTANCE = Mappers.getMapper(UserGameMapper.class);

    @Mapping(source = "game.igdbId", target = "gameIgdbId")
        // @Mapping(source = "internalId", target = "libraryEntryId") // Si decides incluirlo
    UserGameResponseDTO toResponseDto(UserGame userGame);

    // No necesitamos un toEntity desde UserGameDataDTO directamente porque
    // la entidad UserGame se crea/actualiza en el servicio con User y Game ya obtenidos.
    // En su lugar, usaremos un método para actualizar una entidad existente.

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "game", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true) // MapStruct lo actualizará por @UpdateTimestamp
    @Mapping(target = "deletedAt", ignore = true)
    void updateFromDto(UserGameDataDTO dto, @MappingTarget UserGame entity);
}
