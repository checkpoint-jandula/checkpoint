package mp.tfg.mycheckpoint.service.impl;

import mp.tfg.mycheckpoint.dto.user.*;
import mp.tfg.mycheckpoint.entity.PasswordResetToken;
import mp.tfg.mycheckpoint.entity.User;
import mp.tfg.mycheckpoint.entity.VerificationToken;
import mp.tfg.mycheckpoint.event.OnRegistrationCompleteEvent;
import mp.tfg.mycheckpoint.exception.DuplicateEntryException;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.mapper.UserMapper;
import mp.tfg.mycheckpoint.repository.PasswordResetTokenRepository;
import mp.tfg.mycheckpoint.repository.UserRepository;
import mp.tfg.mycheckpoint.repository.VerificationTokenRepository;
import mp.tfg.mycheckpoint.service.EmailService;
import mp.tfg.mycheckpoint.service.UserService;
import mp.tfg.mycheckpoint.service.FileStorageService; // ASEGÚRATE QUE ESTA IMPORTACIÓN ES CORRECTA
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final FileStorageService fileStorageService; // DECLARACIÓN DEL CAMPO

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final int ACCOUNT_DELETION_GRACE_PERIOD_DAYS = 10;


    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           VerificationTokenRepository verificationTokenRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           EmailService emailService,
                           ApplicationEventPublisher eventPublisher,
                           FileStorageService fileStorageService) { // PARÁMETRO EN CONSTRUCTOR
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.eventPublisher = eventPublisher;
        this.fileStorageService = fileStorageService; // ASIGNACIÓN EN CONSTRUCTOR
    }

    @Override
    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        if (userRepository.existsByEmailAndFechaEliminacionIsNull(userCreateDTO.getEmail())) {
            throw new DuplicateEntryException("El email '" + userCreateDTO.getEmail() + "' ya está registrado.");
        }
        if (userRepository.existsByNombreUsuarioAndFechaEliminacionIsNull(userCreateDTO.getNombreUsuario())) {
            throw new DuplicateEntryException("El nombre de usuario '" + userCreateDTO.getNombreUsuario() + "' ya está en uso.");
        }
        User userEntity = userMapper.toEntity(userCreateDTO);
        userEntity.setContraseña(passwordEncoder.encode(userCreateDTO.getContraseña()));
        User savedUser = userRepository.save(userEntity);
        VerificationToken verificationToken = new VerificationToken(savedUser);
        verificationTokenRepository.save(verificationToken);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(savedUser, verificationToken.getToken()));
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByPublicId(UUID publicId) {
        return userRepository.findByPublicId(publicId).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toDto);
    }


    @Override
    @Transactional
    public UserDTO updateUserProfile(String userEmail, UserProfileUpdateDTO profileUpdateDTO) {
        User userEntity = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail + " para actualizar perfil."));
        if (StringUtils.hasText(profileUpdateDTO.getNombreUsuario()) &&
                !profileUpdateDTO.getNombreUsuario().equals(userEntity.getNombreUsuario())) {
            Optional<User> existingUserWithNewUsername = userRepository.findByNombreUsuario(profileUpdateDTO.getNombreUsuario());
            if (existingUserWithNewUsername.isPresent() && !existingUserWithNewUsername.get().getEmail().equals(userEmail)) {
                throw new DuplicateEntryException("El nuevo nombre de usuario '" + profileUpdateDTO.getNombreUsuario() + "' ya está en uso.");
            }
        }
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
            return "Este enlace de verificación ya ha sido utilizado.";
        }
        if (verificationToken.isExpired()) {
            return "El enlace de verificación ha expirado. Por favor, solicita uno nuevo.";
        }
        User user = verificationToken.getUser();
        if (user.isEmailVerified()) {
            return "Tu correo electrónico ya ha sido verificado.";
        }
        user.setEmailVerified(true);
        userRepository.save(user);
        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
        return "¡Tu correo electrónico ha sido verificado exitosamente! Ahora puedes iniciar sesión.";
    }

    @Override
    @Transactional
    public void changePassword(String userEmail, PasswordChangeDTO passwordChangeDTO) {
        User userEntity = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));
        if (!passwordEncoder.matches(passwordChangeDTO.getContraseñaActual(), userEntity.getContraseña())) {
            throw new BadCredentialsException("La contraseña actual proporcionada es incorrecta.");
        }
        if (passwordEncoder.matches(passwordChangeDTO.getNuevaContraseña(), userEntity.getContraseña())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la contraseña actual.");
        }
        userEntity.setContraseña(passwordEncoder.encode(passwordChangeDTO.getNuevaContraseña()));
        userRepository.save(userEntity);
    }

    @Override
    @Transactional
    public void processForgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        Optional<User> userOptional = userRepository.findByEmail(forgotPasswordDTO.getEmail());
        if (userOptional.isEmpty()) {
            logger.warn("Solicitud de reseteo de contraseña para email no existente: {}", forgotPasswordDTO.getEmail());
            return;
        }
        User user = userOptional.get();
        passwordResetTokenRepository.findByUserAndUsedFalseAndExpiryDateAfter(user, OffsetDateTime.now())
                .ifPresent(existingToken -> {
                    existingToken.setUsed(true);
                    passwordResetTokenRepository.save(existingToken);
                });
        PasswordResetToken passwordResetToken = new PasswordResetToken(user);
        passwordResetTokenRepository.save(passwordResetToken);
        emailService.sendPasswordResetEmail(user, passwordResetToken.getToken());
    }

    @Override
    @Transactional
    public String processResetPassword(ResetPasswordDTO resetPasswordDTO) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(resetPasswordDTO.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Token de restablecimiento de contraseña inválido o no encontrado."));
        if (passwordResetToken.isUsed()) {
            throw new IllegalStateException("Este token de restablecimiento ya ha sido utilizado.");
        }
        if (passwordResetToken.isExpired()) {
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new IllegalStateException("El token de restablecimiento ha expirado. Por favor, solicita uno nuevo.");
        }
        User user = passwordResetToken.getUser();
        if (passwordEncoder.matches(resetPasswordDTO.getNuevaContraseña(), user.getContraseña())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la contraseña actual.");
        }
        user.setContraseña(passwordEncoder.encode(resetPasswordDTO.getNuevaContraseña()));
        userRepository.save(user);
        passwordResetToken.setUsed(true);
        passwordResetTokenRepository.save(passwordResetToken);
        return "Tu contraseña ha sido restablecida exitosamente. Ahora puedes iniciar sesión con tu nueva contraseña.";
    }

    @Override
    @Transactional
    public void softDeleteUserAccount(String userEmail, AccountDeleteDTO accountDeleteDTO) {
        logger.info("Solicitando programación de eliminación para la cuenta del usuario: {}", userEmail);
        User userEntity = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado con email: {} al intentar programar eliminación de cuenta.", userEmail);
                    return new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail);
                });
        if (userEntity.getFechaEliminacion() != null && userEntity.getFechaEliminacion().isAfter(OffsetDateTime.now())) {
            logger.warn("La cuenta para el usuario: {} ya tiene una eliminación programada para {}.", userEmail, userEntity.getFechaEliminacion());
            throw new IllegalStateException("Tu cuenta ya está programada para eliminación el " +
                    userEntity.getFechaEliminacion().toLocalDate() +
                    ". Si deseas cancelar esta acción, simplemente inicia sesión.");
        }
        if (!passwordEncoder.matches(accountDeleteDTO.getContraseñaActual(), userEntity.getContraseña())) {
            logger.warn("Contraseña actual incorrecta al intentar programar eliminación de cuenta para el usuario: {}", userEmail);
            throw new BadCredentialsException("La contraseña actual proporcionada es incorrecta.");
        }
        OffsetDateTime futureDeletionDate = OffsetDateTime.now().plusDays(ACCOUNT_DELETION_GRACE_PERIOD_DAYS);
        userEntity.setFechaEliminacion(futureDeletionDate);
        logger.info("Eliminación de cuenta programada para el usuario: {} en la fecha: {}", userEmail, futureDeletionDate);
        verificationTokenRepository.findByUser(userEntity).ifPresent(verificationTokenRepository::delete);
        passwordResetTokenRepository.findByUser(userEntity).ifPresent(passwordResetTokenRepository::delete);
        userRepository.save(userEntity);
        logger.info("Solicitud de eliminación de cuenta procesada y programada exitosamente para el usuario: {}", userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchResultDTO> searchUsersByUsername(String usernameQuery, String currentUserEmail) {
        if (!StringUtils.hasText(usernameQuery) || usernameQuery.trim().length() < 2) {
            return Collections.emptyList();
        }
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario actual no encontrado: " + currentUserEmail + " al realizar búsqueda."));

        List<User> foundUsers = userRepository.searchByNombreUsuarioContainingIgnoreCaseAndNotSelf(
                usernameQuery.trim(),
                currentUser.getId()
        );
        return foundUsers.stream()
                .map(userMapper::toSearchResultDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUserProfilePicture(String userEmail, String profilePictureFileName) {
        User userEntity = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        String oldProfilePicture = userEntity.getFotoPerfil();
        if (oldProfilePicture != null && !oldProfilePicture.isBlank() && !oldProfilePicture.equals(profilePictureFileName)) {
            // Aquí se podría tener un nombre de archivo por defecto que no se deba eliminar, ej "default.png"
            // if (!oldProfilePicture.equalsIgnoreCase("default.png")) {
            fileStorageService.deleteProfilePicture(oldProfilePicture); // USO DEL CAMPO
            // }
        }

        userEntity.setFotoPerfil(profilePictureFileName);
        User updatedUser = userRepository.save(userEntity);
        logger.info("Foto de perfil actualizada para el usuario {} a {}", userEmail, profilePictureFileName);
        return userMapper.toDto(updatedUser);
    }
}