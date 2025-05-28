package mp.tfg.mycheckpoint.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mp.tfg.mycheckpoint.dto.usergame.GameDetailDTO;
import mp.tfg.mycheckpoint.dto.usergame.UserGameDataDTO;
import mp.tfg.mycheckpoint.dto.usergame.UserGameResponseDTO;
import mp.tfg.mycheckpoint.security.UserDetailsImpl;
import mp.tfg.mycheckpoint.service.UserGameLibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Game Library Controller", description = "API para gestionar la biblioteca de juegos personal de un usuario")
@RestController
@RequestMapping("/api/v1") // Base path general
public class UserGameLibraryController {

    private final UserGameLibraryService userGameLibraryService;

    @Autowired
    public UserGameLibraryController(UserGameLibraryService userGameLibraryService) {
        this.userGameLibraryService = userGameLibraryService;
    }

    // Endpoint para añadir o actualizar un juego en la biblioteca del usuario autenticado
    @PostMapping("/users/me/library/games/{igdbId}")
    @Operation(summary = "Añadir o actualizar un juego en la biblioteca del usuario autenticado",
            description = "Permite al usuario autenticado añadir un juego (identificado por su IGDB ID) a su biblioteca personal o actualizar una entrada existente. " +
                    "Si el juego no existe en la base de datos local, se intentará obtener de IGDB. " +
                    "Se proporcionan datos específicos del usuario para este juego (estado, puntuación, plataforma, etc.). Requiere autenticación.",
            operationId = "addOrUpdateGameInMyLibrary",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Juego añadido o actualizado en la biblioteca exitosamente. Devuelve la entrada de la biblioteca actualizada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserGameResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos. Ocurre si los datos en `UserGameDataDTO` no pasan las validaciones (ej. puntuación fuera de rango).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. El usuario autenticado no pudo ser verificado, o el juego con el `igdbId` proporcionado no se encontró en IGDB.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor. Podría ocurrir si hay problemas al contactar IGDB o al guardar los datos.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<UserGameResponseDTO> addOrUpdateGameInMyLibrary(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "igdbId",
                    description = "ID de IGDB del juego a añadir o actualizar en la biblioteca.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1020",
                    schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long igdbId,
            @Valid @org.springframework.web.bind.annotation.RequestBody UserGameDataDTO userGameDataDTO){
        UserGameResponseDTO response = userGameLibraryService.addOrUpdateGameInLibrary(currentUser.getEmail(), igdbId, userGameDataDTO);
        return ResponseEntity.ok(response);
    }

    // Endpoint para obtener toda la biblioteca del usuario autenticado
    @GetMapping("/users/me/library/games")
    @Operation(summary = "Obtener la biblioteca completa de juegos del usuario autenticado",
            description = "Recupera todas las entradas de juegos que el usuario actualmente autenticado tiene en su biblioteca personal, " +
                    "incluyendo el estado, puntuación, plataforma y otros datos específicos del usuario para cada juego. Requiere autenticación.",
            operationId = "getMyGameLibrary",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Biblioteca de juegos recuperada exitosamente. La lista puede estar vacía si el usuario no tiene juegos añadidos.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = UserGameResponseDTO.class))
                    )),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. El usuario autenticado no pudo ser verificado en la base de datos (caso anómalo).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<List<UserGameResponseDTO>> getMyGameLibrary(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<UserGameResponseDTO> library = userGameLibraryService.getUserGameLibrary(currentUser.getEmail());
        return ResponseEntity.ok(library);
    }

    // Endpoint para obtener un juego específico de la biblioteca del usuario autenticado
    @GetMapping("/users/me/library/games/{igdbId}")
    @Operation(summary = "Obtener un juego específico de la biblioteca del usuario autenticado",
            description = "Recupera los detalles de un juego específico (identificado por su IGDB ID) tal como existe en la biblioteca personal del usuario autenticado. " +
                    "Esto incluye el estado, puntuación, y otros datos que el usuario haya registrado para ese juego. Requiere autenticación.",
            operationId = "getSpecificGameFromMyLibrary",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Juego específico de la biblioteca recuperado exitosamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserGameResponseDTO.class)
                    )),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. El juego con el IGDB ID especificado no se encontró en la biblioteca del usuario, o el usuario autenticado no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<UserGameResponseDTO> getSpecificGameFromMyLibrary(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "igdbId",
                    description = "ID de IGDB del juego a obtener de la biblioteca del usuario.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1020", // Ejemplo de IGDB ID
                    schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long igdbId) {
        UserGameResponseDTO gameFromLibrary = userGameLibraryService.getUserGameFromLibrary(currentUser.getEmail(), igdbId);
        return ResponseEntity.ok(gameFromLibrary);
    }

    // Endpoint para la vista detallada de un juego (puede ser público o específico de usuario)
    // Este es el endpoint que mencionaste para la vista detallada.
    // Lo pongo en GameController porque lógicamente es sobre "un juego", pero lo puede llamar UserGameLibraryService.
    // O puede estar aquí si prefieres mantener todo lo que toque UserGameData junto.
    // Por ahora, lo mantenemos aquí para ilustrar, pero podría moverse.
    @GetMapping("/games/{igdbId}/details")
    @Operation(summary = "Obtener detalles completos de un juego",
            description = "Recupera información detallada sobre un juego específico, identificado por su IGDB ID. " +
                    "Este endpoint es público. Si se proporciona un token JWT de autenticación válido, la respuesta incluirá " +
                    "adicionalmente los datos específicos del usuario para ese juego (si existen en su biblioteca), como su estado, puntuación, etc. " +
                    "Si no se proporciona autenticación o el token es inválido, solo se devolverá la información pública del juego y los comentarios públicos.",
            operationId = "getGameDetails"
            // No se añade @SecurityRequirement global aquí porque el endpoint base es público.
            // La naturaleza opcional de la autenticación se explica en la descripción.
            // Si se quisiera ser más explícito sobre la seguridad opcional, se podría hacer con schemes vacíos o configuraciones avanzadas,
            // pero la descripción suele ser suficiente.
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalles del juego recuperados exitosamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GameDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado. El juego con el IGDB ID especificado no se encontró o el usuario (si está autenticado) no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor. Podría ocurrir si hay problemas al contactar IGDB.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
            // No se documenta 401 explícitamente como error principal porque el endpoint es público.
            // Si un token inválido es enviado, el filtro de seguridad podría devolver 401 antes de llegar aquí.
    })
    public ResponseEntity<GameDetailDTO> getGameDetails(
            @Parameter(name = "igdbId",
                    description = "ID de IGDB del juego para el cual se solicitan los detalles.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1020",
                    schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long igdbId,
            // Authentication es opcional y no se documenta como un @Parameter de API estándar.
            // Su presencia y uso se describen en la descripción general del endpoint.
            Authentication authentication) { // Authentication puede ser null si el endpoint es parcialmente público

        String userEmail = null;
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
            userEmail = currentUser.getEmail();
        }

        GameDetailDTO gameDetail = userGameLibraryService.getGameDetailsForUser(igdbId, userEmail);
        return ResponseEntity.ok(gameDetail);
    }


    // Endpoint para eliminar un juego de la biblioteca del usuario autenticado
    @DeleteMapping("/users/me/library/games/{igdbId}")
    @Operation(summary = "Eliminar un juego de la biblioteca del usuario autenticado",
            description = "Permite al usuario autenticado eliminar un juego específico (identificado por su IGDB ID) de su biblioteca personal. " +
                    "Requiere autenticación.",
            operationId = "removeGameFromMyLibrary",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Juego eliminado de la biblioteca exitosamente. No hay contenido en la respuesta."),
            // No se especifica 'content' para 204
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. El juego con el IGDB ID especificado no se encontró en la biblioteca del usuario para eliminar, o el usuario autenticado no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<Void> removeGameFromMyLibrary(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "igdbId",
                    description = "ID de IGDB del juego a eliminar de la biblioteca del usuario.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1020", // Ejemplo de IGDB ID
                    schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long igdbId) {
        userGameLibraryService.removeGameFromLibrary(currentUser.getEmail(), igdbId);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
