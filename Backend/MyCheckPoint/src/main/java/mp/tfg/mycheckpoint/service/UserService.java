package mp.tfg.mycheckpoint.service;

import mp.tfg.mycheckpoint.dto.user.UserCreateDTO;
import mp.tfg.mycheckpoint.dto.user.UserDTO;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserDTO createUser(UserCreateDTO userCreateDTO);
    Optional<UserDTO> getUserById(Long id); // Para uso interno si es necesario
    Optional<UserDTO> getUserByPublicId(UUID publicId); // Para API pública
}