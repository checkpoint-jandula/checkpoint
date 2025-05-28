package mp.tfg.mycheckpoint.mapper;

import mp.tfg.mycheckpoint.dto.user.UserCreateDTO;
import mp.tfg.mycheckpoint.dto.user.UserDTO;
import mp.tfg.mycheckpoint.dto.user.UserProfileUpdateDTO;
import mp.tfg.mycheckpoint.dto.user.UserSearchResultDTO; // IMPORTANTE: Añadir esta importación
import mp.tfg.mycheckpoint.entity.User;
import org.mapstruct.*;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List; // IMPORTANTE: Añadir esta importación

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface UserMapper {

    UserDTO toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "contraseña", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    User toEntity(UserCreateDTO userCreateDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProfileFromDto(UserProfileUpdateDTO userProfileUpdateDTO, @MappingTarget User userEntity);

    // MODIFICACIÓN: Nuevo método para mapear a DTO de resultado de búsqueda
    @Mapping(source = "publicId", target = "publicId")
    @Mapping(source = "nombreUsuario", target = "nombreUsuario")
    @Mapping(source = "fotoPerfil", target = "fotoPerfil")
    UserSearchResultDTO toSearchResultDto(User user);

    // MODIFICACIÓN: Para mapear listas (opcional, MapStruct puede inferirlo)
    List<UserSearchResultDTO> toSearchResultDtoList(List<User> users);
}