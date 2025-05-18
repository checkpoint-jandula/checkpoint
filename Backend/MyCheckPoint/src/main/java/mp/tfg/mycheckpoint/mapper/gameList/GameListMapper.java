package mp.tfg.mycheckpoint.mapper.gameList;

import mp.tfg.mycheckpoint.dto.gameList.GameListRequestDTO;
import mp.tfg.mycheckpoint.dto.gameList.GameListResponseDTO;
import mp.tfg.mycheckpoint.entity.GameList;
import mp.tfg.mycheckpoint.mapper.UserGameMapper; // Asumiendo que tienes este mapper
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserGameMapper.class}) // Asegúrate que UserGameMapper esté disponible
public interface GameListMapper {

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "publicId", ignore = true) // Se genera en @PrePersist o se maneja en servicio
    @Mapping(target = "owner", ignore = true) // Se establece en el servicio
    @Mapping(target = "userGames", ignore = true) // Se maneja por separado
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    GameList toEntity(GameListRequestDTO requestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "userGames", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromDto(GameListRequestDTO dto, @MappingTarget GameList entity);


    @Mapping(source = "owner.nombreUsuario", target = "ownerUsername")
    @Mapping(source = "userGames", target = "gamesInList")
    @Mapping(target = "gameCount", expression = "java(gameList.getUserGames() != null ? gameList.getUserGames().size() : 0)")
    GameListResponseDTO toResponseDto(GameList gameList);

    // Método helper para mapear Set<UserGame> a List<UserGameResponseDTO>
    // MapStruct usará UserGameMapper.toResponseDto para cada elemento.
    // Este método es implícitamente usado por el mapeo de "userGames" a "gamesInList"
    // si UserGameMapper está en la cláusula 'uses'.
    // Si no, puedes definirlo explícitamente:
    // default List<UserGameResponseDTO> mapUserGamesToResponse(Set<UserGame> userGames) {
    // if (userGames == null) {
    // return java.util.Collections.emptyList();
    // }
    // UserGameMapper mapper = Mappers.getMapper(UserGameMapper.class); // O inyectarlo
    // return userGames.stream().map(mapper::toResponseDto).collect(Collectors.toList());
    // }
}
