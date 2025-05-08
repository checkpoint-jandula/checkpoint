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
    private final PasswordEncoder passwordEncoder; // Asumiendo que ya has añadido BCrypt

    @Autowired
    // Añadir PasswordEncoder al constructor para la inyección de dependencias
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;  // Asignar el bean inyectado
    }

    @Override
    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        // Validaciones de duplicados (igual que antes)
        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new DuplicateEntryException("El email '" + userCreateDTO.getEmail() + "' ya está registrado.");
        }
        if (userRepository.existsByNombreUsuario(userCreateDTO.getNombreUsuario())) {
            throw new DuplicateEntryException("El nombre de usuario '" + userCreateDTO.getNombreUsuario() + "' ya está en uso.");
        }

        // Mapear DTO a Entidad. Los campos tema, notificaciones, visibilidadPerfil
        // tomarán los defaults de la entidad User.
        User userEntity = userMapper.toEntity(userCreateDTO);

        // --- Hashear la contraseña (IMPORTANTE - Módulo 2) ---
        userEntity.setContraseña(passwordEncoder.encode(userCreateDTO.getContraseña()));
        // ----------------------------------------------------

        // --- LÓGICA ELIMINADA ---
        // Ya no es necesario asignar defaults aquí, se hace en la entidad
        // if (userEntity.getTema() == null) { ... }
        // if (userEntity.getNotificaciones() == null) { ... }
        // if (userEntity.getVisibilidadPerfil() == null) { ... }
        // --- FIN LÓGICA ELIMINADA ---

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
