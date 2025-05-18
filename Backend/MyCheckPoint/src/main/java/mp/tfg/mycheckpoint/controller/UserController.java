package mp.tfg.mycheckpoint.controller;

import jakarta.validation.Valid;
import mp.tfg.mycheckpoint.dto.user.*; // Asegúrate que UserSearchResultDTO esté aquí o importa su paquete
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.security.UserDetailsImpl;
import mp.tfg.mycheckpoint.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // IMPORTAR para el nuevo endpoint
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List; // IMPORTAR
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /usuarios - Registrar un nuevo usuario
    @PostMapping
    public ResponseEntity<UserDTO> registrarUsuario(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserDTO createdUser = userService.createUser(userCreateDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // GET /usuarios/{id} - Obtener usuario por ID (interno)
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUsuarioById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID interno: " + id));
    }

    // GET /usuarios/public/{publicId} - Obtener perfil público por UUID
    @GetMapping("/public/{publicId}")
    public ResponseEntity<UserDTO> getUsuarioByPublicId(@PathVariable UUID publicId) {
        return userService.getUserByPublicId(publicId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con Public ID: " + publicId));
    }

    // GET /api/v1/usuarios/me - Obtener detalles del usuario autenticado actual
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

    // PUT /api/v1/usuarios/me - Actualizar perfil del usuario autenticado actual
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUserProfile(@Valid @RequestBody UserProfileUpdateDTO profileUpdateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userEmail = userDetails.getEmail();
        UserDTO updatedUser = userService.updateUserProfile(userEmail, profileUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping(value = "/me/password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> changeMyPassword(@Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userEmail = userDetails.getEmail();
        // ... (código existente)
        try {
            userService.changePassword(userEmail, passwordChangeDTO);
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
    public ResponseEntity<Object> deleteMyAccount(@Valid @RequestBody AccountDeleteDTO accountDeleteDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userEmail = userDetails.getEmail();
        // ... (código existente)
        try {
            userService.softDeleteUserAccount(userEmail, accountDeleteDTO);
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

    // MODIFICACIÓN: Endpoint para buscar usuarios por nombre de usuario
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResultDTO>> searchUsersByUsername(
            @RequestParam("username") String usernameQuery,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        // Se necesita el usuario actual para excluirlo de los resultados de su propia búsqueda
        List<UserSearchResultDTO> users = userService.searchUsersByUsername(usernameQuery, currentUser.getEmail());
        return ResponseEntity.ok(users);
    }
}