package mp.tfg.mycheckpoint.service.impl;

import mp.tfg.mycheckpoint.dto.user.UserCreateDTO;
import mp.tfg.mycheckpoint.dto.user.UserDTO;
import mp.tfg.mycheckpoint.entity.User;
import mp.tfg.mycheckpoint.exception.DuplicateEntryException;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.mapper.UserMapper;
import mp.tfg.mycheckpoint.repository.UserRepository;
import mp.tfg.mycheckpoint.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Importar para Módulo 2
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    // private final PasswordEncoder passwordEncoder; // Se inyectará en Módulo 2

    @Autowired
    // Ajustar constructor cuando se añada PasswordEncoder
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper /*, PasswordEncoder passwordEncoder */) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        // this.passwordEncoder = passwordEncoder; // Para Módulo 2
    }

    @Override
    @Transactional // Buena práctica para operaciones que modifican datos
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new DuplicateEntryException("El email '" + userCreateDTO.getEmail() + "' ya está registrado.");
        }
        if (userRepository.existsByNombreUsuario(userCreateDTO.getNombreUsuario())) {
            throw new DuplicateEntryException("El nombre de usuario '" + userCreateDTO.getNombreUsuario() + "' ya está en uso.");
        }

        User userEntity = userMapper.toEntity(userCreateDTO);

        // --- ESTO SE MODIFICARÁ EN EL MÓDULO 2 PARA HASHEAR LA CONTRASEÑA ---
        // userEntity.setContraseña(passwordEncoder.encode(userCreateDTO.getContraseña()));
        // Por ahora, para el Módulo 1, la guardamos tal cual (NO SEGURO PARA PRODUCCIÓN)
        userEntity.setContraseña(userCreateDTO.getContraseña()); // ¡TEMPORAL!
        // --------------------------------------------------------------------


        // Asignar defaults si son null en el DTO y no se mapearon con @Mapping(defaultValue) en MapStruct
        // o si los defaults de la entidad no se aplicaron correctamente (Lombok @Builder puede ayudar aquí)
        if (userEntity.getTema() == null && userCreateDTO.getTema() == null) {
            userEntity.setTema(mp.tfg.mycheckpoint.dto.enums.TemaEnum.CLARO); // Default de la entidad
        }
        if (userEntity.getNotificaciones() == null && userCreateDTO.getNotificaciones() == null) {
            userEntity.setNotificaciones(true); // Default de la entidad
        }
        if (userEntity.getVisibilidadPerfil() == null && userCreateDTO.getVisibilidadPerfil() == null) {
            userEntity.setVisibilidadPerfil(mp.tfg.mycheckpoint.dto.enums.VisibilidadEnum.PUBLICO); // Default de la entidad
        }


        User savedUser = userRepository.save(userEntity);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByPublicId(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .map(userMapper::toDto);
    }
}
