package mp.tfg.mycheckpoint.controller;

import jakarta.validation.Valid;
import mp.tfg.mycheckpoint.dto.user.*;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.security.UserDetailsImpl; // Importar para obtener detalles del Principal
import mp.tfg.mycheckpoint.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication; // Importar para obtener el principal
import org.springframework.security.core.context.SecurityContextHolder; // Importar para obtener el contexto
// import org.springframework.security.access.prepost.PreAuthorize; // Opcional para autorización a nivel de método
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios") // Base path de tu OpenAPI
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /usuarios - Registrar un nuevo usuario
    // operationId: registrarUsuario
    @PostMapping
    public ResponseEntity<UserDTO> registrarUsuario(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserDTO createdUser = userService.createUser(userCreateDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // GET /usuarios/{id} - Obtener usuario por ID (interno)
    // operationId: getUsuarioById
    // Nota: El OpenAPI dice 'id' es Long (interno). Si quieres exponer esto, asegúrate de su propósito.
    // Por ahora, lo implementaremos como un endpoint interno.
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUsuarioById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID interno: " + id));
    }

    // GET /usuarios/public/{publicId} - Obtener perfil público por UUID
    // operationId: getUsuarioByPublicId
    @GetMapping("/public/{publicId}")
    public ResponseEntity<UserDTO> getUsuarioByPublicId(@PathVariable UUID publicId) {
        return userService.getUserByPublicId(publicId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con Public ID: " + publicId));
    }

    // GET /api/v1/usuarios/me - Obtener detalles del usuario autenticado actual
    // Este endpoint debe estar protegido y solo accesible si hay un token JWT válido
    @GetMapping("/me")
    // @PreAuthorize("isAuthenticated()") // Otra forma de asegurar que está autenticado, si @EnableMethodSecurity está activo
    public ResponseEntity<UserDTO> getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            // Esto no debería pasar si el JwtAuthenticationFilter funciona y SecurityConfig protege el endpoint.
            // Pero es una comprobación defensiva.
            // Devolver 401 si no hay un usuario autenticado correctamente (aunque el filtro debería haberlo hecho).
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userEmail = userDetails.getEmail(); // O getUsername() si es lo que usas como identificador principal

        // Usamos el servicio para obtener el UserDTO completo basado en el email (o el ID si lo prefieres)
        return userService.getUserByEmail(userEmail)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado en la base de datos con email: " + userEmail));
    }



    // --- NUEVO ENDPOINT PUT /me ---
    // PUT /api/v1/usuarios/me - Actualizar perfil del usuario autenticado actual
    @PutMapping("/me")
    // @PreAuthorize("isAuthenticated()") // Ya protegido por .anyRequest().authenticated() en SecurityConfig
    public ResponseEntity<UserDTO> updateCurrentUserProfile(@Valid @RequestBody UserProfileUpdateDTO profileUpdateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // No es estrictamente necesario volver a verificar authentication si el endpoint ya está protegido
        // y JwtAuthenticationFilter funciona, pero no hace daño.
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Debería ser manejado por el filtro/entrypoint
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userEmail = userDetails.getEmail(); // O getUsername()

        UserDTO updatedUser = userService.updateUserProfile(userEmail, profileUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    // NUEVO ENDPOINT PARA CAMBIAR CONTRASEÑA
    @PutMapping(value = "/me/password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> changeMyPassword(@Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userEmail = userDetails.getEmail();

        try {
            userService.changePassword(userEmail, passwordChangeDTO);
            // Devolver una respuesta simple de éxito
            return ResponseEntity.ok().body(java.util.Collections.singletonMap("message", "Contraseña actualizada correctamente."));
        } catch (BadCredentialsException e) { // O tu excepción personalizada
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401 si la contraseña actual es incorrecta
                    .body(java.util.Collections.singletonMap("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() // 400 para otros errores de validación (ej. nueva contraseña igual a la antigua)
                    .body(java.util.Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            // Captura genérica para otros posibles errores
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Collections.singletonMap("error", "Ocurrió un error inesperado al cambiar la contraseña."));
        }
    }

    @DeleteMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteMyAccount(@Valid @RequestBody AccountDeleteDTO accountDeleteDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userEmail = userDetails.getEmail();

        try {
            userService.softDeleteUserAccount(userEmail, accountDeleteDTO);
            // Después de eliminar la cuenta, la sesión actual debería invalidarse.
            // El frontend debería manejar esto borrando el token JWT y redirigiendo.
            // Spring Security no invalida el token JWT automáticamente aquí.
            SecurityContextHolder.clearContext(); // Limpiar el contexto de seguridad del servidor para la petición actual.
            return ResponseEntity.ok().body(java.util.Collections.singletonMap("message", "Tu cuenta ha sido programada para eliminación y ya no es accesible."));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401 si la contraseña actual es incorrecta
                    .body(java.util.Collections.singletonMap("error", e.getMessage()));
        } catch (ResourceNotFoundException e) { // Si el usuario de alguna manera ya no existe
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Collections.singletonMap("error", e.getMessage()));
        } catch (IllegalStateException e) { // Ej. si la cuenta ya estaba eliminada
            return ResponseEntity.badRequest()
                    .body(java.util.Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            // Captura genérica
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Collections.singletonMap("error", "Ocurrió un error inesperado al eliminar la cuenta."));
        }
    }
}