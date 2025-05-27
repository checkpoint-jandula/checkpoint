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
import mp.tfg.mycheckpoint.dto.gameList.AddGameToCustomListRequestDTO;
import mp.tfg.mycheckpoint.dto.gameList.GameListRequestDTO;
import mp.tfg.mycheckpoint.dto.gameList.GameListResponseDTO;
import mp.tfg.mycheckpoint.security.UserDetailsImpl;
import mp.tfg.mycheckpoint.service.GameListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "GameList Controller", description = "API para la gestión de listas de juegos personalizadas por el usuario")
@RestController
@RequestMapping("/api/v1")
public class GameListController {

    private final GameListService gameListService;

    @Autowired
    public GameListController(GameListService gameListService) {
        this.gameListService = gameListService;
    }

    // --- Endpoints para listas del usuario autenticado ---

    @PostMapping("/users/me/gamelists")
    @Operation(summary = "Crear una nueva lista de juegos para el usuario autenticado",
            description = "Permite al usuario autenticado crear una nueva lista de juegos personalizada. " +
                    "Se requiere un nombre para la lista y se puede especificar si es pública o privada. Requiere autenticación.",
            operationId = "createMyGameList",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lista de juegos creada exitosamente. Devuelve los detalles de la lista recién creada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GameListResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos. Ocurre si los datos en `GameListRequestDTO` no pasan las validaciones (ej. nombre vacío).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
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
    public ResponseEntity<GameListResponseDTO> createMyGameList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @org.springframework.web.bind.annotation.RequestBody GameListRequestDTO requestDTO) {
        GameListResponseDTO createdList = gameListService.createGameList(currentUser.getEmail(), requestDTO);
        return new ResponseEntity<>(createdList, HttpStatus.CREATED);
    }

    @GetMapping("/users/me/gamelists")
    @Operation(summary = "Obtener todas las listas de juegos del usuario autenticado",
            description = "Recupera una lista de todas las listas de juegos personalizadas creadas por el usuario actualmente autenticado. " +
                    "Las listas se devuelven ordenadas por la fecha de última actualización de forma descendente. Requiere autenticación.",
            operationId = "getMyGameLists",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listas de juegos recuperadas exitosamente. La lista puede estar vacía si el usuario no ha creado ninguna.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = GameListResponseDTO.class))
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
    public ResponseEntity<List<GameListResponseDTO>> getMyGameLists(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<GameListResponseDTO> lists = gameListService.getAllGameListsForUser(currentUser.getEmail());
        return ResponseEntity.ok(lists);
    }

    @GetMapping("/users/me/gamelists/{listPublicId}")
    @Operation(summary = "Obtener una lista de juegos específica del usuario autenticado por su ID público",
            description = "Recupera los detalles y los juegos contenidos en una lista de juegos específica, identificada por su ID público (UUID), " +
                    "que pertenezca al usuario actualmente autenticado. Requiere autenticación.",
            operationId = "getMySpecificGameList",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de juegos específica recuperada exitosamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GameListResponseDTO.class)
                    )),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. La lista de juegos con el ID público especificado no fue encontrada para el usuario actual, o el usuario autenticado no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<GameListResponseDTO> getMySpecificGameList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "listPublicId",
                    description = "ID público (UUID) de la lista de juegos a obtener.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "123e4567-e89b-12d3-a456-426614174000",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID listPublicId) {
        GameListResponseDTO list = gameListService.getGameListByPublicIdForUser(currentUser.getEmail(), listPublicId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/users/me/gamelists/{listPublicId}")
    @Operation(summary = "Actualizar una lista de juegos existente del usuario autenticado",
            description = "Permite al usuario autenticado modificar los detalles (nombre, descripción, visibilidad) de una de sus listas de juegos existentes, " +
                    "identificada por su ID público (UUID). Solo los campos proporcionados en el cuerpo de la solicitud serán actualizados. Requiere autenticación.",
            operationId = "updateMyGameList",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de juegos actualizada exitosamente. Devuelve los detalles actualizados de la lista.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GameListResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos. Ocurre si los datos en `GameListRequestDTO` no pasan las validaciones (ej. nombre en blanco si se modifica, descripción demasiado larga).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. La lista de juegos con el ID público especificado no fue encontrada para el usuario actual, o el usuario autenticado no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<GameListResponseDTO> updateMyGameList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "listPublicId",
                    description = "ID público (UUID) de la lista de juegos a actualizar.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "123e4567-e89b-12d3-a456-426614174000",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID listPublicId,
            @Valid @org.springframework.web.bind.annotation.RequestBody GameListRequestDTO requestDTO) {
        GameListResponseDTO updatedList = gameListService.updateGameList(currentUser.getEmail(), listPublicId, requestDTO);
        return ResponseEntity.ok(updatedList);
    }

    @DeleteMapping("/users/me/gamelists/{listPublicId}")
    @Operation(summary = "Eliminar una lista de juegos del usuario autenticado",
            description = "Permite al usuario autenticado eliminar una de sus listas de juegos existentes, identificada por su ID público (UUID). " +
                    "Esto no elimina los juegos de la biblioteca del usuario, solo la lista en sí. Requiere autenticación.",
            operationId = "deleteMyGameList",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Lista de juegos eliminada exitosamente. No hay contenido en la respuesta."),
            // No se especifica 'content' para 204
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. La lista de juegos con el ID público especificado no fue encontrada para el usuario actual, o el usuario autenticado no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<Void> deleteMyGameList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "listPublicId",
                    description = "ID público (UUID) de la lista de juegos a eliminar.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "123e4567-e89b-12d3-a456-426614174000",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID listPublicId) {
        gameListService.deleteGameList(currentUser.getEmail(), listPublicId);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints para gestionar juegos DENTRO de una lista del usuario autenticado ---

    @PostMapping("/users/me/gamelists/{listPublicId}/games")
    @Operation(summary = "Añadir un juego de la biblioteca del usuario a una de sus listas de juegos",
            description = "Permite al usuario autenticado añadir una entrada de juego existente en su biblioteca personal (identificada por su `user_game_id` interno) " +
                    "a una de sus listas de juegos personalizadas (identificada por `listPublicId`). " +
                    "El juego no se añade si ya está presente en la lista. Requiere autenticación.",
            operationId = "addGameToMyCustomList",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Juego añadido a la lista exitosamente (o ya estaba presente). Devuelve la lista actualizada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GameListResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos. El `user_game_id` en el cuerpo de la solicitud es nulo o inválido.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El juego que se intenta añadir no pertenece a la biblioteca del usuario autenticado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. La lista de juegos especificada (`listPublicId`) o la entrada de juego de la biblioteca (`user_game_id`) no fueron encontradas, o el usuario autenticado no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<GameListResponseDTO> addGameToMyCustomList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "listPublicId",
                    description = "ID público (UUID) de la lista de juegos a la que se añadirá el juego.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "123e4567-e89b-12d3-a456-426614174000",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID listPublicId,
            @Valid @org.springframework.web.bind.annotation.RequestBody AddGameToCustomListRequestDTO requestDTO) {
        GameListResponseDTO updatedList = gameListService.addGameToCustomList(
                currentUser.getEmail(), listPublicId, requestDTO.getUserGameId());
        return ResponseEntity.ok(updatedList);
    }

    // Se usa el userGameInternalId como PathVariable aquí para la eliminación.
    // Podrías optar por usar gameIgdbId si es más conveniente para el frontend,
    // pero entonces el servicio necesitaría buscar UserGame basado en user + gameIgdbId.
    @DeleteMapping("/users/me/gamelists/{listPublicId}/games/{userGameInternalId}")
    @Operation(summary = "Eliminar un juego de una lista de juegos personalizada del usuario autenticado",
            description = "Permite al usuario autenticado eliminar un juego específico (identificado por su `userGameInternalId`) de una de sus listas de juegos " +
                    "(identificada por `listPublicId`). Esto no elimina el juego de la biblioteca general del usuario, solo de esta lista en particular. Requiere autenticación.",
            operationId = "removeGameFromMyCustomList",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Juego eliminado de la lista exitosamente (o no se encontraba en ella). No hay contenido en la respuesta."),
            // No se especifica 'content' para 204
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El juego que se intenta eliminar de la lista no pertenece a la biblioteca del usuario autenticado (si esta verificación se realiza antes de intentar la eliminación de la lista).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. La lista de juegos (`listPublicId`) o la entrada de juego (`userGameInternalId`) no fueron encontradas, o el usuario actual no pudo ser verificado. " +
                    "También podría ocurrir si el juego especificado no estaba en la lista para ser eliminado (aunque el servicio actual no lanza error por esto).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<Void> removeGameFromMyCustomList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "listPublicId",
                    description = "ID público (UUID) de la lista de juegos de la cual se eliminará el juego.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "123e4567-e89b-12d3-a456-426614174000",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID listPublicId,
            @Parameter(name = "userGameInternalId",
                    description = "ID interno de la entrada 'UserGame' (juego en la biblioteca del usuario) a eliminar de la lista.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "101",
                    schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long userGameInternalId) {
        gameListService.removeGameFromCustomList(currentUser.getEmail(), listPublicId, userGameInternalId);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints públicos para visualización de listas ---

    @GetMapping("/gamelists/public")
    @Operation(summary = "Obtener todas las listas de juegos públicas",
            description = "Recupera una lista de todas las listas de juegos que han sido marcadas como públicas por sus creadores. " +
                    "Las listas se devuelven ordenadas por la fecha de última actualización de forma descendente. Este endpoint es público y no requiere autenticación.",
            operationId = "viewAllPublicGameLists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listas de juegos públicas recuperadas exitosamente. La lista puede estar vacía si no hay ninguna.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = GameListResponseDTO.class))
                    )),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
            // No se documentan 401, 403, 404 como errores primarios ya que el endpoint es público y simplemente devuelve lo que hay.
    })
    public ResponseEntity<List<GameListResponseDTO>> viewAllPublicGameLists() {
        List<GameListResponseDTO> publicLists = gameListService.getAllPublicGameLists();
        return ResponseEntity.ok(publicLists);
    }


    @GetMapping("/gamelists/{listPublicId}/public")
    @Operation(summary = "Obtener una lista de juegos pública específica por su ID público",
            description = "Recupera los detalles y los juegos contenidos en una lista de juegos específica que haya sido marcada como pública, " +
                    "identificada por su ID público (UUID). Este endpoint es público y no requiere autenticación.",
            operationId = "viewPublicGameList")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de juegos pública recuperada exitosamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GameListResponseDTO.class)
                    )),
            @ApiResponse(responseCode = "404", description = "No encontrado. La lista de juegos pública con el ID especificado no fue encontrada o no es pública.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
            // No se documentan 401 o 403 ya que el endpoint es público.
    })
    public ResponseEntity<GameListResponseDTO> viewPublicGameList(
            @Parameter(name = "listPublicId",
                    description = "ID público (UUID) de la lista de juegos pública a obtener.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID listPublicId) {
        GameListResponseDTO publicList = gameListService.getPublicGameListByPublicId(listPublicId);
        return ResponseEntity.ok(publicList);
    }
}
