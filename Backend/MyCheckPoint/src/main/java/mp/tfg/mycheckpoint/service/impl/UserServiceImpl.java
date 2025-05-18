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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder; // Importar para Módulo 2
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import mp.tfg.mycheckpoint.dto.user.UserSearchResultDTO; // Asegúrate que esté bien importado
import java.util.Collections; // Para lista vacía
import java.util.List; // IMPORTAR
import java.util.stream.Collectors; // IMPORTAR
import org.springframework.util.StringUtils; // Para StringUtils.hasText

import java.time.OffsetDateTime;
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
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class); // <-- AÑADIR ESTA LÍNEA
    private static final int ACCOUNT_DELETION_GRACE_PERIOD_DAYS = 10; // Período de gracia de 10 días


    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           VerificationTokenRepository verificationTokenRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           EmailService emailService,
                           ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        // Validaciones de duplicados (igual que antes)
        if (userRepository.existsByEmailAndFechaEliminacionIsNull(userCreateDTO.getEmail())) {
            throw new DuplicateEntryException("El email '" + userCreateDTO.getEmail() + "' ya está registrado.");
        }
        if (userRepository.existsByNombreUsuarioAndFechaEliminacionIsNull(userCreateDTO.getNombreUsuario())) {
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
    @Transactional(readOnly = true)
    public List<UserSearchResultDTO> searchUsersByUsername(String usernameQuery, String currentUserEmail) {
        if (!StringUtils.hasText(usernameQuery) || usernameQuery.trim().length() < 2) { // Evitar búsquedas vacías o muy cortas
            return Collections.emptyList();
        }

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario actual no encontrado: " + currentUserEmail));


        List<User> foundUsers = userRepository.searchByNombreUsuarioContainingIgnoreCaseAndNotSelf(
                usernameQuery.trim(),
                currentUser.getId()
        );

        return foundUsers.stream()
                .map(userMapper::toSearchResultDto) // Usa el método del mapper
                .collect(Collectors.toList());
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

    @Override
    @Transactional
    public void changePassword(String userEmail, PasswordChangeDTO passwordChangeDTO) {
        User userEntity = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        // 1. Verificar la contraseña actual
        if (!passwordEncoder.matches(passwordChangeDTO.getContraseñaActual(), userEntity.getContraseña())) {
            // Puedes usar BadCredentialsException de Spring o una personalizada.
            // BadCredentialsException suele usarse más en el AuthenticationManager.
            // Una excepción personalizada podría ser más clara aquí.
            throw new BadCredentialsException("La contraseña actual proporcionada es incorrecta.");
            // o throw new InvalidCredentialsException("La contraseña actual proporcionada es incorrecta.");
        }

        // 2. (Opcional) Verificar si la nueva contraseña y su confirmación coinciden
        /*
        if (StringUtils.hasText(passwordChangeDTO.getConfirmarNuevaContraseña()) &&
            !passwordChangeDTO.getNuevaContraseña().equals(passwordChangeDTO.getConfirmarNuevaContraseña())) {
            throw new IllegalArgumentException("La nueva contraseña y su confirmación no coinciden.");
        }
        */

        // 3. Verificar si la nueva contraseña es diferente a la actual (opcional, pero buena práctica)
        if (passwordEncoder.matches(passwordChangeDTO.getNuevaContraseña(), userEntity.getContraseña())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la contraseña actual.");
        }

        // 4. Hashear y establecer la nueva contraseña
        userEntity.setContraseña(passwordEncoder.encode(passwordChangeDTO.getNuevaContraseña()));

        // 5. Guardar los cambios
        userRepository.save(userEntity);

        // Consideraciones adicionales:
        // - Invalidar sesiones/tokens JWT existentes para este usuario.
        //   Esto es más avanzado y a menudo implica una blacklist de tokens o mecanismos similares.
        //   Por simplicidad, se suele confiar en la expiración natural del token.
        // - Enviar un correo de notificación al usuario indicando que su contraseña ha sido cambiada.
        //   (Podrías usar tu EmailService para esto).
    }

    @Override
    @Transactional
    public void processForgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        Optional<User> userOptional = userRepository.findByEmail(forgotPasswordDTO.getEmail());

        if (userOptional.isEmpty()) {
            // No revelar si el email existe o no por seguridad. Simplemente loguear.
            logger.warn("Solicitud de reseteo de contraseña para email no existente: {}", forgotPasswordDTO.getEmail());
            // Podrías optar por no hacer nada o enviar una respuesta genérica si esto fuera un endpoint que devuelve algo.
            // Como es void y el controlador responderá, aquí solo retornamos.
            return;
        }

        User user = userOptional.get();

        // Opcional: Invalidar tokens de reseteo anteriores para este usuario
        passwordResetTokenRepository.findByUserAndUsedFalseAndExpiryDateAfter(user, OffsetDateTime.now())
                .ifPresent(existingToken -> {
                    existingToken.setUsed(true); // Marcar como usado o eliminarlo
                    passwordResetTokenRepository.save(existingToken);
                    // o passwordResetTokenRepository.delete(existingToken);
                });


        PasswordResetToken passwordResetToken = new PasswordResetToken(user);
        passwordResetTokenRepository.save(passwordResetToken);

        // Considera usar un evento también aquí si el envío de email puede fallar y no quieres revertir
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
            passwordResetTokenRepository.delete(passwordResetToken); // Limpiar token expirado
            throw new IllegalStateException("El token de restablecimiento ha expirado. Por favor, solicita uno nuevo.");
        }

        User user = passwordResetToken.getUser();

        // (Opcional) Verificar si la nueva contraseña y su confirmación coinciden
    /*
    if (StringUtils.hasText(resetPasswordDTO.getConfirmarNuevaContraseña()) &&
        !resetPasswordDTO.getNuevaContraseña().equals(resetPasswordDTO.getConfirmarNuevaContraseña())) {
        throw new IllegalArgumentException("La nueva contraseña y su confirmación no coinciden.");
    }
    */

        // Verificar que la nueva contraseña no sea la misma que la actual (si el usuario recuerda la actual por casualidad)
        if (passwordEncoder.matches(resetPasswordDTO.getNuevaContraseña(), user.getContraseña())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la contraseña actual.");
        }

        user.setContraseña(passwordEncoder.encode(resetPasswordDTO.getNuevaContraseña()));
        userRepository.save(user);

        passwordResetToken.setUsed(true);
        passwordResetTokenRepository.save(passwordResetToken); // Marcar como usado (o eliminarlo)

        // Opcional: Enviar email de confirmación de cambio de contraseña
        // emailService.sendPasswordChangedConfirmationEmail(user);

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

        // 1. Verificar si la cuenta ya tiene una eliminación programada activa
        if (userEntity.getFechaEliminacion() != null && userEntity.getFechaEliminacion().isAfter(OffsetDateTime.now())) {
            logger.warn("La cuenta para el usuario: {} ya tiene una eliminación programada para {}.", userEmail, userEntity.getFechaEliminacion());
            throw new IllegalStateException("Tu cuenta ya está programada para eliminación el " +
                    userEntity.getFechaEliminacion().toLocalDate() +
                    ". Si deseas cancelar esta acción, simplemente inicia sesión.");
        }

        // 2. Verificar la contraseña actual
        if (!passwordEncoder.matches(accountDeleteDTO.getContraseñaActual(), userEntity.getContraseña())) {
            logger.warn("Contraseña actual incorrecta al intentar programar eliminación de cuenta para el usuario: {}", userEmail);
            throw new BadCredentialsException("La contraseña actual proporcionada es incorrecta.");
        }

        // 3. Establecer la fecha de eliminación futura
        OffsetDateTime futureDeletionDate = OffsetDateTime.now().plusDays(ACCOUNT_DELETION_GRACE_PERIOD_DAYS);
        userEntity.setFechaEliminacion(futureDeletionDate);
        logger.info("Eliminación de cuenta programada para el usuario: {} en la fecha: {}", userEmail, futureDeletionDate);

        // 4. NO cambiar emailVerified
        // userEntity.setEmailVerified(false); // Esta línea se elimina o comenta

        // 5. Limpiar tokens de verificación y reseteo de contraseña (buena práctica)
        verificationTokenRepository.findByUser(userEntity).ifPresent(verificationTokenRepository::delete);
        passwordResetTokenRepository.findByUser(userEntity).ifPresent(passwordResetTokenRepository::delete);

        // 6. Guardar los cambios
        userRepository.save(userEntity);
        logger.info("Solicitud de eliminación de cuenta procesada y programada exitosamente para el usuario: {}", userEmail);

        // 7. Opcional: Enviar un email al usuario informando sobre la programación
        // emailService.sendAccountScheduledForDeletionEmail(userEntity, futureDeletionDate);
    }
}
