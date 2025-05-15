package mp.tfg.mycheckpoint.service.impl;

import mp.tfg.mycheckpoint.dto.user.UserCreateDTO;
import mp.tfg.mycheckpoint.dto.user.UserDTO;
import mp.tfg.mycheckpoint.dto.user.UserProfileUpdateDTO;
import mp.tfg.mycheckpoint.entity.User;
import mp.tfg.mycheckpoint.entity.VerificationToken;
import mp.tfg.mycheckpoint.event.OnRegistrationCompleteEvent;
import mp.tfg.mycheckpoint.exception.DuplicateEntryException;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.mapper.UserMapper;
import mp.tfg.mycheckpoint.repository.UserRepository;
import mp.tfg.mycheckpoint.repository.VerificationTokenRepository;
import mp.tfg.mycheckpoint.service.EmailService;
import mp.tfg.mycheckpoint.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder; // Importar para Módulo 2
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder; // Asumiendo que ya has añadido BCrypt
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher; // Para desacoplar el envío de email


    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           VerificationTokenRepository verificationTokenRepository, /* Añadir */
                           EmailService emailService,  /* Añadir */
                           ApplicationEventPublisher eventPublisher /* Añadir */) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository; // Asignar
        this.emailService = emailService; // Asignar
        this.eventPublisher = eventPublisher; // Asignar
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

        User userEntity = userMapper.toEntity(userCreateDTO);
        userEntity.setContraseña(passwordEncoder.encode(userCreateDTO.getContraseña()));
        // userEntity.setEmailVerified(false); // Ya es el valor por defecto en la entidad

        User savedUser = userRepository.save(userEntity);

        // Generar y guardar el token de verificación
        VerificationToken verificationToken = new VerificationToken(savedUser);
        verificationTokenRepository.save(verificationToken);

        // Publicar un evento para enviar el email.
        // Esto ayuda a desacoplar el envío de email de la transacción principal.
        // Si el envío de email falla, la transacción de creación de usuario no se revierte.
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(savedUser, verificationToken.getToken()));

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

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDto);
    }


    @Override
    @Transactional
    public UserDTO updateUserProfile(String userEmail, UserProfileUpdateDTO profileUpdateDTO) {
        User userEntity = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail + " para actualizar perfil."));

        // Lógica para actualizar nombre_usuario si se proporciona y es diferente
        if (StringUtils.hasText(profileUpdateDTO.getNombreUsuario()) &&
                !profileUpdateDTO.getNombreUsuario().equals(userEntity.getNombreUsuario())) {

            // Verificar si el nuevo nombre de usuario ya está en uso por OTRO usuario
            Optional<User> existingUserWithNewUsername = userRepository.findByNombreUsuario(profileUpdateDTO.getNombreUsuario());
            if (existingUserWithNewUsername.isPresent() && !existingUserWithNewUsername.get().getEmail().equals(userEmail)) {
                throw new DuplicateEntryException("El nuevo nombre de usuario '" + profileUpdateDTO.getNombreUsuario() + "' ya está en uso.");
            }
            // Si no hay conflicto, MapStruct lo actualizará gracias a la siguiente línea.
            // O podrías asignarlo explícitamente: userEntity.setNombreUsuario(profileUpdateDTO.getNombreUsuario());
            // pero es mejor dejar que el mapper lo haga si es posible para consistencia.
        }

        // Usar el mapper para aplicar los cambios del DTO a la entidad existente.
        // MapStruct solo actualizará los campos no nulos del DTO debido a NullValuePropertyMappingStrategy.IGNORE.
        // y ya hemos manejado la lógica especial para nombreUsuario.
        userMapper.updateProfileFromDto(profileUpdateDTO, userEntity);

        User updatedUser = userRepository.save(userEntity);
        return userMapper.toDto(updatedUser);
    }


    @Override
    @Transactional
    public String confirmEmailVerification(String tokenString) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new ResourceNotFoundException("Token de verificación inválido o no encontrado."));

        if (verificationToken.isUsed()) {
            return "Este enlace de verificación ya ha sido utilizado."; // O lanzar una excepción
        }
        if (verificationToken.isExpired()) {
            // Opcional: eliminar el token expirado
            // verificationTokenRepository.delete(verificationToken);
            return "El enlace de verificación ha expirado. Por favor, solicita uno nuevo."; // O lanzar excepción
        }

        User user = verificationToken.getUser();
        if (user.isEmailVerified()) {
            return "Tu correo electrónico ya ha sido verificado.";
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken); // Opcional: podrías eliminarlo en lugar de marcarlo como usado

        return "¡Tu correo electrónico ha sido verificado exitosamente! Ahora puedes iniciar sesión.";
    }
}
