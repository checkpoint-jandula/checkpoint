package mp.tfg.mycheckpoint.service;

import mp.tfg.mycheckpoint.dto.user.*;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserDTO createUser(UserCreateDTO userCreateDTO);
    Optional<UserDTO> getUserById(Long id); // Para uso interno si es necesario
    Optional<UserDTO> getUserByPublicId(UUID publicId); // Para API pública
    Optional<UserDTO> getUserByEmail(String email);
    UserDTO updateUserProfile(String userEmail, UserProfileUpdateDTO profileUpdateDTO);
    String confirmEmailVerification(String token); // Devuelve un mensaje de éxito/error
    void changePassword(String userEmail, PasswordChangeDTO passwordChangeDTO); // <-- NUEVO MÉTODO
    void processForgotPassword(ForgotPasswordDTO forgotPasswordDTO);
    String processResetPassword(ResetPasswordDTO resetPasswordDTO);
}