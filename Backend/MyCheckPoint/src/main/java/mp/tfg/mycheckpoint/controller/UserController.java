package mp.tfg.mycheckpoint.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Usuarios", description = "API para la gestión de usuarios")
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService; // DECLARACIÓN DEL CAMPO

    @Autowired
    public UserController(UserService userService, FileStorageService fileStorageService) { // PARÁMETRO EN CONSTRUCTOR
        this.userService = userService;
        this.fileStorageService = fileStorageService; // ASIGNACIÓN EN CONSTRUCTOR
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo usuario",
            description = "Crea una nueva cuenta de usuario en el sistema. Tras el registro exitoso, se enviará un correo electrónico de verificación a la dirección proporcionada para activar la cuenta.",
            operationId = "registrarUsuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente. Devuelve los datos del usuario recién creado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos. Ocurre si los datos proporcionados en `UserCreateDTO` no pasan las validaciones (ej. email no válido, contraseña corta).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))), // Refiere al esquema global de error de validación
            @ApiResponse(responseCode = "409", description = "Conflicto. El email o el nombre de usuario proporcionado ya se encuentra registrado en el sistema.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))), // Refiere al esquema global de error genérico
            @ApiResponse(responseCode = "500", description = "Error interno del servidor. Ocurrió un problema inesperado durante el proceso de registro.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<UserDTO> registrarUsuario(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del nuevo usuario a registrar. Todos los campos son obligatorios.", required = true,
                    content = @Content(schema = @Schema(implementation = UserCreateDTO.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody UserCreateDTO userCreateDTO) {
        UserDTO createdUser = userService.createUser(userCreateDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario por su ID interno",
            description = "Recupera los detalles de un usuario específico utilizando su ID numérico interno. Este ID es el identificador primario en la base de datos.",
            operationId = "getUsuarioById",
            security = { @SecurityRequirement(name = "bearerAuth") }) // Indica que este endpoint requiere el esquema de seguridad 'bearerAuth'
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado y devuelto exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))), // Asumiendo que este es el esquema para errores 401 también
            @ApiResponse(responseCode = "403", description = "Prohibido. El usuario autenticado no tiene permisos para acceder a este recurso (aunque para un GET por ID podría no ser el caso a menos que haya reglas específicas).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. No existe ningún usuario con el ID interno proporcionado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<UserDTO> getUsuarioById(
            @Parameter(name = "id",
                    description = "ID numérico interno del usuario a obtener.",
                    required = true,
                    in = ParameterIn.PATH, // Especifica que es un parámetro de ruta
                    example = "1",
                    schema = @Schema(type = "integer", format = "int64")) // 'integer' con 'int64' es común para Long
            @PathVariable Long id) {
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
        UserDTO updatedUser = userService.updateUserProfile(currentUser.getEmail(), profileUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping(value = "/me/password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> changeMyPassword(@AuthenticationPrincipal UserDetailsImpl currentUser, @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
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

        // La validación de file.isEmpty() y tamaño ahora la hace fileStorageService.storeProfilePicture()
        // y lanza FileStorageException si falla, que será manejada por GlobalExceptionHandler.

        String userPublicId = currentUser.getPublicId().toString();

        // 1. Guardar el archivo (puede lanzar FileStorageException)
        String fileName = fileStorageService.storeProfilePicture(file, userPublicId);

        // 2. Actualizar la entidad User con el nombre del archivo
        UserDTO updatedUser = userService.updateUserProfilePicture(currentUser.getEmail(), fileName);

        return ResponseEntity.ok(updatedUser);
    }
}