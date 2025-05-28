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
        // No es necesario un @Mapping explícito para isPublic aquí si los nombres coinciden
        // y MapStruct lo infiere correctamente para la creación.
        // GameList.builder().isPublic(requestDTO.getIsPublic()) se encargará.
    GameList toEntity(GameListRequestDTO requestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "userGames", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // MODIFICACIÓN: Añadir mapeo explícito para actualizar 'isPublic'.
    // El 'target' es "public" porque el setter en la entidad GameList (generado por Lombok @Data para boolean isPublic) es setPublic().
    // El 'source' es "isPublic" del DTO.
    @Mapping(source = "isPublic", target = "public")
    void updateFromDto(GameListRequestDTO dto, @MappingTarget GameList entity);


    @Mapping(source = "owner.nombreUsuario", target = "ownerUsername")
    @Mapping(source = "userGames", target = "gamesInList")
    @Mapping(target = "gameCount", expression = "java(gameList.getUserGames() != null ? gameList.getUserGames().size() : 0)")
    // MODIFICACIÓN: Añadir mapeo explícito para leer 'isPublic' al DTO de respuesta.
    // El 'source' es "public" porque el getter en la entidad GameList (generado por Lombok @Data para boolean isPublic) es isPublic().
    // MapStruct interpreta la propiedad de la entidad como "public".
    // El 'target' es "isPublic" en el DTO de respuesta.
    @Mapping(source = "public", target = "isPublic")
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