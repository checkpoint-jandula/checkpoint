package mp.tfg.mycheckpoint.mapper;

import mp.tfg.mycheckpoint.dto.user.UserCreateDTO;
import mp.tfg.mycheckpoint.dto.user.UserDTO;
import mp.tfg.mycheckpoint.dto.user.UserProfileUpdateDTO;
import mp.tfg.mycheckpoint.entity.User;
import org.mapstruct.*;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, // No actualizar campos si son null en el DTO
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS) // Considerar nulls explícitos en DTO
public interface UserMapper {

    UserDTO toDto(User user); // Mapea User.publicId a UserDTO.publicId, User.nombreUsuario a UserDTO.nombreUsuario, etc.

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true) // Correcto: Hibernate lo generará, no viene del DTO
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "contraseña", ignore = true)
    @Mapping(target = "emailVerified", ignore = true) // Asegúrate de ignorar esto en la creación
    User toEntity(UserCreateDTO userCreateDTO);


    // --- NUEVO MÉTODO PARA ACTUALIZAR ---
    // Este método actualiza una entidad 'User' existente con los valores de 'UserProfileUpdateDTO'.
    // Los campos que sean null en 'userProfileUpdateDTO' NO se mapearán a la entidad
    // gracias a nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE.
    // No queremos ignorar los campos que no son PK, así que no usamos ignore = true aquí.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProfileFromDto(UserProfileUpdateDTO userProfileUpdateDTO, @MappingTarget User userEntity);
    // --- FIN NUEVO MÉTODO ---
}