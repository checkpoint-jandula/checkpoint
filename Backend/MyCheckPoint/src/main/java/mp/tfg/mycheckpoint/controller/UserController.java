package mp.tfg.mycheckpoint.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import mp.tfg.mycheckpoint.service.FileStorageService;
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
                            schema = @Schema(ref = "#/components/schemas/DuplicatedResourceResponse"))), // Refiere al esquema global de error genérico
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

    @Hidden
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUsuarioById(
            @PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID interno: " + id));
    }

    @GetMapping("/public/{publicId}")
    @Operation(summary = "Obtener un usuario por su ID público",
            description = "Recupera los detalles de un usuario específico utilizando su ID público (UUID). Este endpoint es público y no requiere autenticación.",
            operationId = "getUsuarioByPublicId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado y devuelto exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado. No existe ningún usuario con el ID público proporcionado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<UserDTO> getUsuarioByPublicId(
            @Parameter(name = "publicId",
                    description = "ID público (UUID) del usuario a obtener.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "123e4567-e89b-12d3-a456-426614174000",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID publicId) {
        return userService.getUserByPublicId(publicId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con Public ID: " + publicId));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener los datos del usuario autenticado actualmente",
            description = "Recupera los detalles del perfil y preferencias del usuario que ha iniciado sesión. Requiere un token JWT válido en la cabecera de autorización.",
            operationId = "getCurrentAuthenticatedUser",
            security = { @SecurityRequirement(name = "bearerAuth") }) // Indica que este endpoint requiere el esquema de seguridad 'bearerAuth'
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos del usuario autenticado devueltos exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            // Aunque el método tiene una comprobación que podría llevar a 401,
            // si el token es válido pero el usuario no se encuentra en la BBDD, se lanza ResourceNotFoundException (404).
            @ApiResponse(responseCode = "404", description = "No encontrado. El usuario autenticado (identificado por el token) no pudo ser encontrado en la base de datos.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
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
    @Operation(summary = "Actualizar el perfil del usuario autenticado actualmente",
            description = "Permite al usuario autenticado modificar los detalles de su perfil, como el nombre de usuario, tema, foto de perfil, preferencias de notificación y visibilidad del perfil. Requiere un token JWT válido.",
            operationId = "updateCurrentUserProfile",
            security = { @SecurityRequirement(name = "bearerAuth") },
            // AQUÍ se define el RequestBody para la documentación OpenAPI
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody( // Usamos el FQN para evitar ambigüedad si también importas la de Spring aquí, aunque es mejor no hacerlo.
                    description = "Datos del perfil del usuario a actualizar. Solo se actualizarán los campos proporcionados.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserProfileUpdateDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE // Opcional, ya que es el default para JSON
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil de usuario actualizado exitosamente. Devuelve los datos actualizados del usuario.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos. Ocurre si los datos proporcionados en `UserProfileUpdateDTO` no pasan las validaciones (ej. nombre de usuario demasiado corto/largo).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. El usuario autenticado (identificado por el token) no pudo ser encontrado en la base de datos para la actualización.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "409", description = "Conflicto. El nuevo nombre de usuario elegido ya está en uso por otro usuario.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/DuplicatedResourceResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<UserDTO> updateCurrentUserProfile(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @org.springframework.web.bind.annotation.RequestBody UserProfileUpdateDTO profileUpdateDTO) {
        UserDTO updatedUser = userService.updateUserProfile(currentUser.getEmail(), profileUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping(value = "/me/password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cambiar la contraseña del usuario autenticado actualmente",
            description = "Permite al usuario autenticado cambiar su contraseña actual por una nueva. Se requiere la contraseña actual para la verificación. Requiere un token JWT válido.",
            operationId = "changeMyPassword",
            security = { @SecurityRequirement(name = "bearerAuth") },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "DTO con la contraseña actual y la nueva contraseña.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PasswordChangeDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            // Define el esquema en línea para el mensaje de éxito simple
                            schema = @Schema(type = "object", properties = {
                                    // La forma de definir propiedades aquí es usando el atributo 'properties'
                                    // de @Schema, que toma un array de @StringToClassMapItem o similar,
                                    // o más comúnmente, crear una pequeña clase DTO como SuccessMessageDTO
                                    // y referenciarla con implementation = SuccessMessageDTO.class
                            }, example = "{\"message\": \"Contraseña actualizada correctamente.\"}"), // El ejemplo es muy útil aquí
                            examples = @ExampleObject(name = "RespuestaExitosaCambioPass", value = "{\"message\": \"Contraseña actualizada correctamente.\"}")
                    )),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o solicitud incorrecta (ej. nueva contraseña igual a la actual).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationPasswordErrorResponse")
                    )),
            @ApiResponse(responseCode = "401", description = "No autorizado. La contraseña actual proporcionada es incorrecta, o el token JWT es inválido/expirado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. El usuario autenticado no pudo ser encontrado en la base de datos (caso anómalo).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<Object> changeMyPassword(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @org.springframework.web.bind.annotation.RequestBody PasswordChangeDTO passwordChangeDTO) {
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
    @Operation(summary = "Programar la eliminación de la cuenta del usuario autenticado",
            description = "Permite al usuario autenticado solicitar la eliminación de su cuenta. Se requiere la contraseña actual para confirmación. " +
                    "La cuenta se marcará para eliminación y se borrará permanentemente después de un período de gracia. " +
                    "Tras esta operación, la sesión actual del usuario se invalidará. Requiere un token JWT válido.",
            operationId = "deleteMyAccount",
            security = { @SecurityRequirement(name = "bearerAuth") },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "DTO que contiene la contraseña actual del usuario para confirmar la eliminación de la cuenta.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AccountDeleteDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitud de eliminación de cuenta procesada. La cuenta ha sido programada para eliminación y la sesión actual invalidada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            // Para el Map devuelto: {"message": "string"}
                            schema = @Schema(type = "object"), // Un objeto genérico
                            examples = @ExampleObject(
                                    name = "AccountDeletionScheduled",
                                    summary = "Ejemplo de respuesta exitosa",
                                    value = "{\"message\": \"Tu cuenta ha sido programada para eliminación vuelve a iniciar sesion si quieres mantenerla.\"}"
                            )
                    )),
            @ApiResponse(responseCode = "401", description = "No autorizado. La contraseña actual proporcionada es incorrecta, o el token JWT es inválido/expirado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))
            ),
            @ApiResponse(responseCode = "404", description = "No encontrado. El usuario autenticado no pudo ser encontrado en la base de datos (caso anómalo).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))
            ),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<Object> deleteMyAccount(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @org.springframework.web.bind.annotation.RequestBody AccountDeleteDTO accountDeleteDTO) {
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
    @Operation(summary = "Buscar usuarios por nombre de usuario",
            description = "Permite a un usuario autenticado buscar otros usuarios en el sistema por su nombre de usuario. " +
                    "La búsqueda es parcial (contiene) e ignora mayúsculas/minúsculas. " +
                    "El propio usuario que realiza la búsqueda será excluido de los resultados. " +
                    "Se requiere un término de búsqueda de al menos 2 caracteres. Requiere autenticación.",
            operationId = "searchUsersByUsername",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda exitosa. Devuelve una lista de usuarios que coinciden con el criterio.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = UserSearchResultDTO.class))
                    )),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta. El parámetro 'username' es obligatorio y debe tener al menos 2 caracteres.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/RequiredErrorResponse")
                    )),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. No se encontraron usuarios con el nombre de usuario proporcionado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<List<UserSearchResultDTO>> searchUsersByUsername(
            @Parameter(name = "username",
                    description = "Término de búsqueda para el nombre de usuario. Debe tener al menos 2 caracteres.",
                    required = true,
                    in = ParameterIn.QUERY, // Es un parámetro de query
                    example = "jua",
                    schema = @Schema(type = "string", minLength = 2)) // minLength aquí es para documentación
            @RequestParam("username") String usernameQuery,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<UserSearchResultDTO> users = userService.searchUsersByUsername(usernameQuery, currentUser.getEmail());

        return ResponseEntity.ok(users);
    }

    // Endpoint para subir/actualizar foto de perfil
    @PostMapping(value="/me/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir o actualizar la foto de perfil del usuario autenticado",
            description = "Permite al usuario autenticado subir un nuevo archivo de imagen para su foto de perfil. " +
                    "El archivo debe ser de un formato permitido (JPEG, PNG, GIF) y no exceder el tamaño máximo configurado. " +
                    "Si ya existe una foto de perfil, será reemplazada. Requiere autenticación.",
            operationId = "uploadProfilePicture",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto de perfil subida y perfil actualizado exitosamente. Devuelve los datos actualizados del usuario.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta. El archivo proporcionado está vacío, tiene un formato no permitido, o hay un problema con el nombre del archivo.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. El usuario autenticado no pudo ser encontrado en la base de datos (caso anómalo).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "413", description = "Payload Too Large. El archivo excede el tamaño máximo permitido para fotos de perfil o el límite general de subida.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/TooLargeResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor. No se pudo crear el directorio de almacenamiento, guardar el archivo, o ocurrió otro error inesperado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<UserDTO> uploadProfilePicture(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(
                    name = "file", // Este nombre debe coincidir con @RequestParam("file")
                    description = "El archivo de imagen a subir como foto de perfil.",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
                    // Springdoc usualmente infiere el `schema: {type: string, format: binary}`
                    // para un MultipartFile a partir del tipo del parámetro.
                    // No necesitas un @Schema complejo aquí para el @Parameter.
            )
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