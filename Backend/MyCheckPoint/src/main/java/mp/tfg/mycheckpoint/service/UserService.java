package mp.tfg.mycheckpoint.service;

import mp.tfg.mycheckpoint.dto.user.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserDTO createUser(UserCreateDTO userCreateDTO);
    Optional<UserDTO> getUserById(Long id);
    Optional<UserDTO> getUserByPublicId(UUID publicId);
    Optional<UserDTO> getUserByEmail(String email);
    UserDTO updateUserProfile(String userEmail, UserProfileUpdateDTO profileUpdateDTO);
    String confirmEmailVerification(String token);
    void changePassword(String userEmail, PasswordChangeDTO passwordChangeDTO);
    void processForgotPassword(ForgotPasswordDTO forgotPasswordDTO);
    String processResetPassword(ResetPasswordDTO resetPasswordDTO);
    void softDeleteUserAccount(String userEmail, AccountDeleteDTO accountDeleteDTO);

    // MODIFICACIÓN: Nuevo método para buscar usuarios
    List<UserSearchResultDTO> searchUsersByUsername(String usernameQuery, String currentUsernameToExclude);

    UserDTO updateUserProfilePicture(String userEmail, String profilePictureFileName);
}