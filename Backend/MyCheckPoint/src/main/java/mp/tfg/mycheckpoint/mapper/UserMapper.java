package mp.tfg.mycheckpoint.mapper;

import mp.tfg.mycheckpoint.dto.user.UserCreateDTO;
import mp.tfg.mycheckpoint.dto.user.UserDTO;
import mp.tfg.mycheckpoint.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserDTO toDto(User user); // Mapea User.publicId a UserDTO.publicId, User.nombreUsuario a UserDTO.nombreUsuario, etc.

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true) // Correcto: Hibernate lo generará, no viene del DTO
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    User toEntity(UserCreateDTO userCreateDTO);
}