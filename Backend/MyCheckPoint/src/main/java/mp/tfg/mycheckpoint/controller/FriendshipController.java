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
import mp.tfg.mycheckpoint.dto.friendship.FriendshipResponseDTO;
import mp.tfg.mycheckpoint.security.UserDetailsImpl;
import mp.tfg.mycheckpoint.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Friendship Controller", description = "API para la gestión de amistades y solicitudes de amistad")
@RestController
@RequestMapping("/api/v1/friends") // Base path para la funcionalidad de amigos
public class FriendshipController {

    private final FriendshipService friendshipService;

    @Autowired
    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    // Enviar una solicitud de amistad
    @PostMapping("/requests/send/{receiverUserPublicId}")
    @Operation(summary = "Enviar una solicitud de amistad",
            description = "Permite al usuario autenticado enviar una solicitud de amistad a otro usuario especificado por su ID público. " +
                    "Si ya existe una solicitud pendiente del receptor hacia el emisor, la amistad se aceptará automáticamente. " +
                    "Requiere autenticación.",
            operationId = "sendFriendRequest",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitud de amistad enviada o amistad auto-aceptada exitosamente. Devuelve el estado de la amistad/solicitud.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FriendshipResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta. El usuario no puede enviarse una solicitud a sí mismo.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. El usuario receptor especificado por `receiverUserPublicId` no existe, o el usuario emisor no pudo ser verificado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "409", description = "Conflicto. Ya existe una amistad o una solicitud de amistad pendiente con este usuario.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/DuplicatedResourceResponse"))), // Usando tu esquema
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<FriendshipResponseDTO> sendFriendRequest(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "receiverUserPublicId",
                    description = "ID público (UUID) del usuario al que se le envía la solicitud de amistad.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID receiverUserPublicId) {
        FriendshipResponseDTO response = friendshipService.sendFriendRequest(currentUser.getEmail(), receiverUserPublicId);
        // El status code podría variar dependiendo de si se creó una nueva solicitud (CREATED) o se auto-aceptó una existente (OK)
        // Por simplicidad, devolveremos OK en ambos casos exitosos desde la perspectiva del cliente que hizo la acción.
        return ResponseEntity.ok(response);
    }

    // Aceptar una solicitud de amistad
    @PutMapping("/requests/accept/{requesterUserPublicId}")
    @Operation(summary = "Aceptar una solicitud de amistad pendiente",
            description = "Permite al usuario autenticado (que es el receptor de la solicitud) aceptar una solicitud de amistad pendiente de otro usuario. " +
                    "La solicitud debe estar en estado PENDIENTE. Requiere autenticación.",
            operationId = "acceptFriendRequest",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitud de amistad aceptada exitosamente. Devuelve el estado actualizado de la amistad.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FriendshipResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El usuario autenticado no es el receptor de la solicitud de amistad pendiente o no tiene permisos para realizar esta acción.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))), // Asumiendo que UnauthorizedOperationException usa ErrorResponse
            @ApiResponse(responseCode = "404", description = "No encontrado. No se encontró una solicitud de amistad pendiente del usuario especificado, o el usuario solicitante/actual no existe.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<FriendshipResponseDTO> acceptFriendRequest(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "requesterUserPublicId",
                    description = "ID público (UUID) del usuario que envió la solicitud de amistad.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "c4d5e6f7-g8h9-0123-4567-890abcdef12",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID requesterUserPublicId) {
        FriendshipResponseDTO response = friendshipService.acceptFriendRequest(currentUser.getEmail(), requesterUserPublicId);
        return ResponseEntity.ok(response);
    }

    // Rechazar/Cancelar una solicitud de amistad
    @DeleteMapping("/requests/decline/{requesterUserPublicId}")
    @Operation(summary = "Rechazar o cancelar una solicitud de amistad pendiente",
            description = "Permite al usuario autenticado (que es el receptor de la solicitud) rechazar una solicitud de amistad pendiente. " +
                    "Alternativamente, si el usuario autenticado fue quien envió la solicitud y esta aún está pendiente, puede usar este endpoint para cancelarla (aunque semánticamente esto último podría ser un endpoint diferente, la lógica actual del servicio elimina la solicitud PENDIENTE). " +
                    "La solicitud de amistad es eliminada de la base de datos. Requiere autenticación.",
            operationId = "declineOrCancelFriendRequest", // Nombre más genérico dado el comportamiento
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Solicitud de amistad rechazada/cancelada y eliminada exitosamente. No hay contenido en la respuesta."),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El usuario autenticado no es el receptor de la solicitud de amistad pendiente que intenta rechazar (o no tiene permisos para cancelarla si fuera el emisor y el endpoint se usara así).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. No se encontró una solicitud de amistad pendiente del usuario especificado, o el usuario solicitante/actual no existe.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<Void> declineFriendRequest(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "requesterUserPublicId",
                    description = "ID público (UUID) del usuario que originalmente envió la solicitud de amistad que se desea rechazar/cancelar.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "d7e8f9a0-b1c2-3456-7890-abcdef123456",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID requesterUserPublicId) {
        friendshipService.declineFriendRequest(currentUser.getEmail(), requesterUserPublicId);
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    // Eliminar un amigo
    @DeleteMapping("/{friendUserPublicId}")
    @Operation(summary = "Eliminar un amigo",
            description = "Permite al usuario autenticado eliminar una amistad existente con otro usuario, especificado por su ID público. " +
                    "La relación de amistad es eliminada de la base de datos. Requiere autenticación.",
            operationId = "removeFriend",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Amigo eliminado exitosamente. No hay contenido en la respuesta."),
            // No se especifica 'content' para 204
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. No se encontró una amistad con el usuario especificado, o el amigo a eliminar no existe, o el usuario actual no pudo ser verificado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<Void> removeFriend(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "friendUserPublicId",
                    description = "ID público (UUID) del amigo que se desea eliminar.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "e0f1a2b3-c4d5-6789-0123-abcdef123456",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID friendUserPublicId) {
        friendshipService.removeFriend(currentUser.getEmail(), friendUserPublicId);
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    // Listar todos los amigos aceptados del usuario actual
    @GetMapping
    @Operation(summary = "Obtener la lista de amigos del usuario autenticado",
            description = "Recupera una lista de todos los usuarios que son amigos del usuario actualmente autenticado (es decir, aquellas relaciones con estado 'ACCEPTED'). " +
                    "Requiere autenticación.",
            operationId = "getMyFriends",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de amigos recuperada exitosamente. La lista puede estar vacía si el usuario no tiene amigos.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = FriendshipResponseDTO.class))
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
    public ResponseEntity<List<FriendshipResponseDTO>> getMyFriends(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<FriendshipResponseDTO> friends = friendshipService.getFriends(currentUser.getEmail());
        return ResponseEntity.ok(friends);
    }

    // Listar solicitudes de amistad pendientes recibidas por el usuario actual
    @GetMapping("/requests/received")
    @Operation(summary = "Obtener las solicitudes de amistad pendientes recibidas por el usuario autenticado",
            description = "Recupera una lista de todas las solicitudes de amistad que el usuario actualmente autenticado ha recibido y aún están pendientes de acción (aceptar o rechazar). " +
                    "Requiere autenticación.",
            operationId = "getPendingRequestsReceived",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de solicitudes pendientes recibidas recuperada exitosamente. La lista puede estar vacía.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = FriendshipResponseDTO.class))
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
    public ResponseEntity<List<FriendshipResponseDTO>> getPendingRequestsReceived(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<FriendshipResponseDTO> requests = friendshipService.getPendingRequestsReceived(currentUser.getEmail());
        return ResponseEntity.ok(requests);
    }

    // Listar solicitudes de amistad pendientes enviadas por el usuario actual
    @GetMapping("/requests/sent")
    @Operation(summary = "Obtener las solicitudes de amistad pendientes enviadas por el usuario autenticado",
            description = "Recupera una lista de todas las solicitudes de amistad que el usuario actualmente autenticado ha enviado y que aún están pendientes de respuesta por parte de los destinatarios. " +
                    "Requiere autenticación.",
            operationId = "getPendingRequestsSent",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de solicitudes pendientes enviadas recuperada exitosamente. La lista puede estar vacía.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = FriendshipResponseDTO.class))
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
    public ResponseEntity<List<FriendshipResponseDTO>> getPendingRequestsSent(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<FriendshipResponseDTO> requests = friendshipService.getPendingRequestsSent(currentUser.getEmail());
        return ResponseEntity.ok(requests);
    }
}
