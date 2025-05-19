package mp.tfg.mycheckpoint.controller;

import jakarta.validation.Valid;
import mp.tfg.mycheckpoint.dto.user.*;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.security.UserDetailsImpl;
import mp.tfg.mycheckpoint.service.UserService;
import mp.tfg.mycheckpoint.service.FileStorageService; // ASEGÚRATE QUE ESTA IMPORTACIÓN ES CORRECTA
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // IMPORTAR

import java.util.List;
import java.util.UUID;
// Ya no necesitas ServletUriComponentsBuilder ni URI aquí si no construyes la URL de descarga en la respuesta.

@RestController
@RequestMapping("/api/v1/usuarios")
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService; // DECLARACIÓN DEL CAMPO

    @Autowired
    public UserController(UserService userService, FileStorageService fileStorageService) { // PARÁMETRO EN CONSTRUCTOR
        this.userService = userService;
        this.fileStorageService = fileStorageService; // ASIGNACIÓN EN CONSTRUCTOR
    }

    @PostMapping
    public ResponseEntity<UserDTO> registrarUsuario(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserDTO createdUser = userService.createUser(userCreateDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUsuarioById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID interno: " + id));
    }

    @GetMapping("/public/{publicId}")
    public ResponseEntity<UserDTO> getUsuarioByPublicId(@PathVariable UUID publicId) {
        return userService.getUserByPublicId(publicId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con Public ID: " + publicId));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userEmail = userDetails.getEmail();
        return userService.getUserByEmail(userEmail)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado en la base de datos con email: " + userEmail));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUserProfile(@AuthenticationPrincipal UserDetailsImpl currentUser, @Valid @RequestBody UserProfileUpdateDTO profileUpdateDTO) {
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // String userEmail = userDetails.getEmail();
        // Es más directo usar @AuthenticationPrincipal
        UserDTO updatedUser = userService.updateUserProfile(currentUser.getEmail(), profileUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping(value = "/me/password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> changeMyPassword(@AuthenticationPrincipal UserDetailsImpl currentUser, @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        // String userEmail = currentUser.getEmail();
        try {
            userService.changePassword(currentUser.getEmail(), passwordChangeDTO);
            return ResponseEntity.ok().body(java.util.Collections.singletonMap("message", "Contraseña actualizada correctamente."));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Collections.singletonMap("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Collections.singletonMap("error", "Ocurrió un error inesperado al cambiar la contraseña."));
        }
    }

    @DeleteMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteMyAccount(@AuthenticationPrincipal UserDetailsImpl currentUser, @Valid @RequestBody AccountDeleteDTO accountDeleteDTO) {
        // String userEmail = currentUser.getEmail();
        try {
            userService.softDeleteUserAccount(currentUser.getEmail(), accountDeleteDTO);
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok().body(java.util.Collections.singletonMap("message", "Tu cuenta ha sido programada para eliminación y ya no es accesible."));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Collections.singletonMap("error", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Collections.singletonMap("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Collections.singletonMap("error", "Ocurrió un error inesperado al eliminar la cuenta."));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResultDTO>> searchUsersByUsername(
            @RequestParam("username") String usernameQuery,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<UserSearchResultDTO> users = userService.searchUsersByUsername(usernameQuery, currentUser.getEmail());
        return ResponseEntity.ok(users);
    }

    // Endpoint para subir/actualizar foto de perfil
    @PostMapping("/me/profile-picture")
    public ResponseEntity<UserDTO> uploadProfilePicture(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            // Devolver un error más descriptivo
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null); // O un DTO de error: .body(new ErrorResponse("El archivo no puede estar vacío."))
        }
        String userPublicId = currentUser.getPublicId().toString();

        // 1. Guardar el archivo
        String fileName = fileStorageService.storeProfilePicture(file, userPublicId); // USO DEL CAMPO

        // 2. Actualizar la entidad User con el nombre del archivo
        UserDTO updatedUser = userService.updateUserProfilePicture(currentUser.getEmail(), fileName);

        return ResponseEntity.ok(updatedUser);
    }
}