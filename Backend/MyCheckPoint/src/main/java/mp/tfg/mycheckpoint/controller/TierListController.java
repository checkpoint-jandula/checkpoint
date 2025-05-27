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
import mp.tfg.mycheckpoint.dto.tierlist.*;
import mp.tfg.mycheckpoint.security.UserDetailsImpl;
import mp.tfg.mycheckpoint.service.TierListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "TierList Controller", description = "API para la gestión de Tier Lists de juegos")
@RestController
@RequestMapping("/api/v1") // Usamos el base path general
public class TierListController {

    private final TierListService tierListService;

    @Autowired
    public TierListController(TierListService tierListService) {
        this.tierListService = tierListService;
    }

    // --- Endpoints para TierLists de Perfil (Generales) ---

    @PostMapping("/users/me/tierlists")
    @Operation(summary = "Crear una nueva Tier List de perfil para el usuario autenticado",
            description = "Permite al usuario autenticado crear una nueva Tier List de tipo 'PROFILE_GLOBAL'. " +
                    "Se requiere un nombre para la lista y opcionalmente una descripción y si es pública. " +
                    "Se crearán secciones por defecto (S, A, B, C, D y 'Juegos por Clasificar'). Requiere autenticación.",
            operationId = "createProfileTierList",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tier List de perfil creada exitosamente. Devuelve los detalles de la lista recién creada, incluyendo las secciones por defecto.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TierListResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos. Ocurre si los datos en `TierListCreateRequestDTO` no pasan las validaciones (ej. nombre vacío o demasiado largo).",
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
    public ResponseEntity<TierListResponseDTO> createProfileTierList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @org.springframework.web.bind.annotation.RequestBody TierListCreateRequestDTO createRequestDTO) {
        TierListResponseDTO createdTierList = tierListService.createProfileTierList(currentUser.getEmail(), createRequestDTO);
        return new ResponseEntity<>(createdTierList, HttpStatus.CREATED);
    }

    @GetMapping("/users/me/tierlists")
    @Operation(summary = "Obtener todas las Tier Lists de perfil del usuario autenticado",
            description = "Recupera una lista de todas las Tier Lists de tipo 'PROFILE_GLOBAL' creadas por el usuario actualmente autenticado. " +
                    "Cada Tier List incluye sus secciones y los ítems clasificados. Requiere autenticación.",
            operationId = "getAllProfileTierListsForCurrentUser",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tier Lists de perfil recuperadas exitosamente. La lista puede estar vacía si el usuario no ha creado ninguna.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TierListResponseDTO.class))
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
    public ResponseEntity<List<TierListResponseDTO>> getAllProfileTierListsForCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<TierListResponseDTO> tierLists = tierListService.getAllProfileTierListsForUser(currentUser.getEmail());
        return ResponseEntity.ok(tierLists);
    }

    // --- Endpoints para TierLists asociadas a GameLists ---

    @GetMapping("/gamelists/{gameListPublicId}/tierlist")
    @Operation(summary = "Obtener o crear la Tier List asociada a una GameList específica",
            description = "Recupera la Tier List de tipo 'FROM_GAMELIST' asociada a la GameList especificada por su ID público. " +
                    "Si no existe una Tier List para esa GameList, se crea una nueva automáticamente con secciones por defecto " +
                    "y se sincroniza con los juegos de la GameList (añadiéndolos a la sección 'Sin Clasificar').\n" +
                    "Este endpoint es público si la GameList y la TierList resultante son públicas. " +
                    "Si la GameList es privada, se requiere autenticación y ser el propietario para acceder o crear la TierList asociada. " +
                    "Si se proporciona un token JWT válido, la respuesta puede incluir información adicional si el usuario es el propietario.",
            operationId = "getOrCreateTierListForGameList"
            // La seguridad es opcional. Si se provee, se usa para validación de acceso a GameLists/TierLists privadas.
            // Se define un securityRequirement opcional, los esquemas de seguridad deben estar definidos globalmente.
            // security = { @SecurityRequirement(name = "bearerAuth", scopes = {}) } // O simplemente omitir para 'opcional' y explicar en la descripción
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tier List recuperada o creada y sincronizada exitosamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TierListResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado. Se proporcionó un token JWT inválido o expirado al intentar acceder a recursos que lo requerían (ej. GameList privada).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El usuario autenticado no tiene permiso para acceder a la GameList especificada (si es privada y no es el propietario) o a la TierList resultante.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. La GameList con el ID público especificado no existe, o el usuario (si está autenticado) no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<TierListResponseDTO> getOrCreateTierListForGameList(
            // @AuthenticationPrincipal es resuelto por Spring si hay un token válido, si no, es null.
            // No se documenta como un @Parameter de API estándar.
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "gameListPublicId",
                    description = "ID público (UUID) de la GameList para la cual se obtendrá o creará la Tier List.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "c4d5e6f7-g8h9-0123-4567-890abcdef12",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID gameListPublicId) {
        String userEmail = (currentUser != null) ? currentUser.getEmail() : null;
        TierListResponseDTO tierList = tierListService.getOrCreateTierListForGameList(userEmail, gameListPublicId);
        return ResponseEntity.ok(tierList);
    }

    // --- Endpoints Generales para TierLists (aplican a ambos tipos, con validación de permisos) ---

    @GetMapping("/tierlists/public")
    @Operation(summary = "Obtener todas las Tier Lists públicas",
            description = "Recupera una lista de todas las Tier Lists que han sido marcadas como públicas por sus creadores. " +
                    "Cada Tier List incluye sus secciones y los ítems clasificados. Las listas se devuelven ordenadas por la fecha de última actualización. " +
                    "Este endpoint es público y no requiere autenticación.",
            operationId = "getAllPublicTierLists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de Tier Lists públicas recuperada exitosamente. La lista puede estar vacía si no hay ninguna.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TierListResponseDTO.class))
                    )),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
            // No se documentan 401, 403, 404 como errores primarios ya que el endpoint es público y simplemente devuelve lo que hay o un 500 si falla internamente.
    })
    public ResponseEntity<List<TierListResponseDTO>> getAllPublicTierLists() {
        List<TierListResponseDTO> publicTierLists = tierListService.getAllPublicTierLists();
        return ResponseEntity.ok(publicTierLists);
    }

    @GetMapping("/tierlists/{tierListPublicId}")
    @Operation(summary = "Obtener una Tier List específica por su ID público",
            description = "Recupera los detalles completos de una Tier List (incluyendo secciones e ítems) utilizando su ID público (UUID). " +
                    "Si la Tier List es pública, cualquiera puede acceder a ella. " +
                    "Si la Tier List es privada, solo el propietario autenticado puede acceder. " +
                    "La autenticación (JWT) es opcional; si se proporciona un token válido y la lista es privada, se verificará la propiedad.",
            operationId = "getTierListByPublicId"
            // No se añade @SecurityRequirement aquí para indicar que el endpoint en sí es públicamente accesible,
            // la lógica de negocio interna maneja la autorización para listas privadas.
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tier List recuperada exitosamente.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TierListResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado. Se proporcionó un token JWT inválido o expirado al intentar acceder a una Tier List privada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. La Tier List es privada y el usuario (autenticado o anónimo) no tiene permiso para accederla.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. La Tier List con el ID público especificado no existe, o el usuario (si está autenticado) no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<TierListResponseDTO> getTierListByPublicId(
            @Parameter(name = "tierListPublicId",
                    description = "ID público (UUID) de la Tier List a obtener.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID tierListPublicId,
            Authentication authentication) {
        String userEmail = null;
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
            userEmail = currentUser.getEmail();
        }
        TierListResponseDTO tierList = tierListService.getTierListByPublicId(tierListPublicId, userEmail);
        return ResponseEntity.ok(tierList);
    }

    @PutMapping("/tierlists/{tierListPublicId}")
    @Operation(summary = "Actualizar los metadatos de una Tier List existente",
            description = "Permite al propietario autenticado de una Tier List modificar sus metadatos como el nombre, la descripción y el estado de visibilidad (pública/privada). " +
                    "Solo los campos proporcionados en el cuerpo de la solicitud serán actualizados. Requiere autenticación y ser el propietario de la Tier List.",
            operationId = "updateTierListMetadata",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadatos de la Tier List actualizados exitosamente. Devuelve la Tier List completa y actualizada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TierListResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos. Ocurre si los datos en `TierListUpdateRequestDTO` no pasan las validaciones (ej. nombre demasiado largo).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El usuario autenticado no es el propietario de la Tier List que intenta modificar.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. La Tier List con el ID público especificado no fue encontrada para el usuario actual, o el usuario autenticado no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<TierListResponseDTO> updateTierListMetadata(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "tierListPublicId",
                    description = "ID público (UUID) de la Tier List a actualizar.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID tierListPublicId,
            @Valid @org.springframework.web.bind.annotation.RequestBody TierListUpdateRequestDTO updateRequestDTO) {
        TierListResponseDTO updatedTierList = tierListService.updateTierListMetadata(currentUser.getEmail(), tierListPublicId, updateRequestDTO);
        return ResponseEntity.ok(updatedTierList);
    }

    @DeleteMapping("/tierlists/{tierListPublicId}")
    @Operation(summary = "Eliminar una Tier List existente",
            description = "Permite al propietario autenticado de una Tier List eliminarla permanentemente. " +
                    "Esto también eliminará todas las secciones y los ítems contenidos en ella. Requiere autenticación y ser el propietario de la Tier List.",
            operationId = "deleteTierList",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tier List eliminada exitosamente. No hay contenido en la respuesta."),
            // No se especifica 'content' para 204
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El usuario autenticado no es el propietario de la Tier List que intenta eliminar.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))), // Asumiendo que UnauthorizedOperationException se mapea a un ErrorResponse
            @ApiResponse(responseCode = "404", description = "No encontrado. La Tier List con el ID público especificado no fue encontrada para el usuario actual, o el usuario autenticado no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<Void> deleteTierList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "tierListPublicId",
                    description = "ID público (UUID) de la Tier List a eliminar.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID tierListPublicId) {
        tierListService.deleteTierList(currentUser.getEmail(), tierListPublicId);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints para Secciones (Tiers) dentro de una TierList ---

    @PostMapping("/tierlists/{tierListPublicId}/sections")
    @Operation(summary = "Añadir una nueva sección (tier) a una Tier List existente",
            description = "Permite al propietario autenticado de una Tier List añadir una nueva sección personalizada. " +
                    "Existe un límite en la cantidad de secciones personalizables que se pueden añadir. " +
                    "Requiere autenticación y ser el propietario de la Tier List.",
            operationId = "addSectionToTierList",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sección añadida exitosamente. Devuelve la Tier List completa y actualizada con la nueva sección.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TierListResponseDTO.class))),
            // Un 201 Created también sería válido si el foco es la creación de la sección.
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (ej. nombre de sección vacío o demasiado largo) o se ha alcanzado el límite máximo de secciones personalizables.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El usuario autenticado no es el propietario de la Tier List a la que intenta añadir una sección.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))), // Asumiendo que UnauthorizedOperationException se mapea a ErrorResponse
            @ApiResponse(responseCode = "404", description = "No encontrado. La Tier List con el ID público especificado no fue encontrada para el usuario actual, o el usuario autenticado no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<TierListResponseDTO> addSectionToTierList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "tierListPublicId",
                    description = "ID público (UUID) de la Tier List a la que se añadirá la nueva sección.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID tierListPublicId,
            @Valid @org.springframework.web.bind.annotation.RequestBody TierSectionRequestDTO sectionRequestDTO) {
        TierListResponseDTO updatedTierList = tierListService.addSectionToTierList(currentUser.getEmail(), tierListPublicId, sectionRequestDTO);
        return ResponseEntity.ok(updatedTierList);
    }

    @PutMapping("/tierlists/{tierListPublicId}/sections/{sectionInternalId}")
    @Operation(summary = "Actualizar el nombre de una sección (tier) específica en una Tier List",
            description = "Permite al propietario autenticado de una Tier List cambiar el nombre de una de sus secciones personalizadas. " +
                    "No se puede cambiar el nombre de la sección por defecto 'Juegos por Clasificar'. " +
                    "Requiere autenticación y ser el propietario de la Tier List.",
            operationId = "updateSectionName",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nombre de la sección actualizado exitosamente. Devuelve la Tier List completa y actualizada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TierListResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos. El nuevo nombre de la sección no cumple las validaciones (ej. vacío o demasiado largo).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El usuario autenticado no es el propietario de la Tier List o intenta modificar una sección no permitida (ej. la sección 'Sin Clasificar' si se implementara tal restricción aquí).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. La Tier List con el ID público especificado o la sección con el ID interno no fueron encontradas para el usuario actual, o el usuario autenticado no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<TierListResponseDTO> updateSectionName(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "tierListPublicId",
                    description = "ID público (UUID) de la Tier List que contiene la sección a actualizar.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID tierListPublicId,
            @Parameter(name = "sectionInternalId",
                    description = "ID interno (Long) de la sección (tier) cuyo nombre se va a actualizar.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "101",
                    schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long sectionInternalId,
            @Valid @org.springframework.web.bind.annotation.RequestBody TierSectionRequestDTO sectionRequestDTO) {
        TierListResponseDTO updatedTierList = tierListService.updateSectionName(currentUser.getEmail(), tierListPublicId, sectionInternalId, sectionRequestDTO);
        return ResponseEntity.ok(updatedTierList);
    }

    @DeleteMapping("/tierlists/{tierListPublicId}/sections/{sectionInternalId}")
    @Operation(summary = "Eliminar una sección (tier) de una Tier List",
            description = "Permite al propietario autenticado de una Tier List eliminar una de sus secciones personalizadas. " +
                    "La sección por defecto 'Juegos por Clasificar' no puede ser eliminada. " +
                    "Debe quedar al menos una sección personalizable tras la eliminación. " +
                    "Si la sección eliminada contenía ítems (juegos), estos serán movidos a la sección 'Juegos por Clasificar'. " +
                    "Requiere autenticación y ser el propietario de la Tier List.",
            operationId = "removeSectionFromTierList",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sección eliminada exitosamente (e ítems reubicados si aplicable). Devuelve la Tier List completa y actualizada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TierListResponseDTO.class))),
            // Un 204 No Content sería también una opción si no se devolviera el cuerpo
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta. No se puede eliminar la sección por defecto 'Juegos por Clasificar' o se intenta eliminar la última sección personalizable.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))), // Para InvalidOperationException
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El usuario autenticado no es el propietario de la Tier List.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))), // Asumiendo que UnauthorizedOperationException se mapea aquí
            @ApiResponse(responseCode = "404", description = "No encontrado. La Tier List o la sección especificada no fueron encontradas para el usuario actual, o el usuario no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor (ej. la sección 'Sin Clasificar' no se encontró al intentar mover ítems).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<TierListResponseDTO> removeSectionFromTierList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "tierListPublicId",
                    description = "ID público (UUID) de la Tier List de la cual se eliminará la sección.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID tierListPublicId,
            @Parameter(name = "sectionInternalId",
                    description = "ID interno (Long) de la sección (tier) a eliminar.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "102",
                    schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long sectionInternalId) {
        TierListResponseDTO updatedTierList = tierListService.removeSectionFromTierList(currentUser.getEmail(), tierListPublicId, sectionInternalId);
        return ResponseEntity.ok(updatedTierList);
    }

    // --- Endpoints para Items (Juegos) dentro de las Secciones de una TierList ---

    // Añadir a una sección específica (principalmente para PROFILE_GLOBAL TierLists)
    @PostMapping("/tierlists/{tierListPublicId}/sections/{sectionInternalId}/items")
    @Operation(summary = "Añadir o mover un ítem (juego) a una sección específica de una Tier List de perfil",
            description = "Permite al propietario autenticado añadir un juego de su biblioteca (UserGame) a una sección específica de una Tier List de tipo 'PROFILE_GLOBAL'. " +
                    "Si el juego ya está en otra sección de esta Tier List, se moverá a la nueva sección y posición. " +
                    "No se puede usar este endpoint para Tier Lists de tipo 'FROM_GAMELIST' ni para añadir a la sección 'Juegos por Clasificar'. " +
                    "Se puede especificar el orden del ítem dentro de la sección. Requiere autenticación y ser propietario.",
            operationId = "addItemToTierListSection",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ítem añadido o movido a la sección exitosamente. Devuelve la Tier List completa y actualizada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TierListResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta. El `user_game_id` es nulo, la Tier List es de tipo 'FROM_GAMELIST', o se intenta añadir a la sección 'Sin Clasificar' usando este endpoint.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))), // Para @Valid
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El usuario autenticado no es el propietario de la Tier List o el UserGame a añadir no le pertenece.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. La Tier List, la sección especificada o el UserGame a añadir no fueron encontrados, o el usuario no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<TierListResponseDTO> addItemToTierListSection(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "tierListPublicId",
                    description = "ID público (UUID) de la Tier List a la que se añadirá el ítem.",
                    required = true, in = ParameterIn.PATH, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID tierListPublicId,
            @Parameter(name = "sectionInternalId",
                    description = "ID interno (Long) de la sección (tier) destino dentro de la Tier List.",
                    required = true, in = ParameterIn.PATH, example = "102",
                    schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long sectionInternalId,
            @Valid @org.springframework.web.bind.annotation.RequestBody TierListItemAddRequestDTO itemAddRequestDTO) {
        TierListResponseDTO updatedTierList = tierListService.addItemToTierListSection(currentUser.getEmail(), tierListPublicId, sectionInternalId, itemAddRequestDTO);
        return ResponseEntity.ok(updatedTierList);
    }

    // Añadir a la sección "Sin Clasificar" (para PROFILE_GLOBAL TierLists)
    @PostMapping("/tierlists/{tierListPublicId}/items/unclassified")
    @Operation(summary = "Añadir o mover un ítem (juego) a la sección 'Sin Clasificar' de una Tier List de perfil",
            description = "Permite al propietario autenticado añadir un juego de su biblioteca (UserGame) directamente a la sección 'Juegos por Clasificar' de una Tier List de tipo 'PROFILE_GLOBAL'. " +
                    "Si el juego ya está en otra sección de esta Tier List, se moverá a la sección 'Juegos por Clasificar'. " +
                    "No se puede usar este endpoint para Tier Lists de tipo 'FROM_GAMELIST'. " +
                    "Se puede especificar el orden del ítem dentro de la sección. Requiere autenticación y ser propietario.",
            operationId = "addItemToUnclassifiedSection",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ítem añadido o movido a la sección 'Sin Clasificar' exitosamente. Devuelve la Tier List completa y actualizada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TierListResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta. El `user_game_id` es nulo, o la Tier List es de tipo 'FROM_GAMELIST'.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El usuario autenticado no es el propietario de la Tier List o el UserGame a añadir no le pertenece.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. La Tier List o el UserGame a añadir no fueron encontrados, o el usuario no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor (ej. la sección 'Sin Clasificar' no se encontró).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<TierListResponseDTO> addItemToUnclassifiedSection(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "tierListPublicId",
                    description = "ID público (UUID) de la Tier List a la que se añadirá el ítem en la sección 'Sin Clasificar'.",
                    required = true, in = ParameterIn.PATH, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID tierListPublicId,
            @Valid @org.springframework.web.bind.annotation.RequestBody TierListItemAddRequestDTO itemAddRequestDTO) {
        TierListResponseDTO updatedTierList = tierListService.addItemToUnclassifiedSection(currentUser.getEmail(), tierListPublicId, itemAddRequestDTO);
        return ResponseEntity.ok(updatedTierList);
    }

    // Mover un item dentro de una TierList (cambiar sección y/o orden)
    // Aplica a PROFILE_GLOBAL y también para reordenar en FROM_GAMELIST TierLists
    @PutMapping("/tierlists/{tierListPublicId}/items/{tierListItemInternalId}/move")
    @Operation(summary = "Mover un ítem (juego) dentro de una Tier List",
            description = "Permite al propietario autenticado mover un ítem existente (identificado por `tierListItemInternalId`) a una nueva sección (`target_section_internal_id`) " +
                    "y/o a una nueva posición (`new_order`) dentro de esa sección en una Tier List específica. " +
                    "Para Tier Lists de tipo 'FROM_GAMELIST', se verifica que el juego del ítem aún pertenezca a la GameList origen. " +
                    "Requiere autenticación y ser propietario de la Tier List.",
            operationId = "moveItemInTierList",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ítem movido exitosamente. Devuelve la Tier List completa y actualizada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TierListResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta. Los datos en `TierListItemMoveRequestDTO` son inválidos (ej. IDs nulos), o la operación es inválida para el tipo de Tier List (ej. juego ya no en GameList origen).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El usuario autenticado no es el propietario de la Tier List.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. La Tier List, el ítem a mover, o la sección destino no fueron encontrados, o el usuario no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<TierListResponseDTO> moveItemInTierList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "tierListPublicId",
                    description = "ID público (UUID) de la Tier List que contiene el ítem a mover.",
                    required = true, in = ParameterIn.PATH, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID tierListPublicId,
            @Parameter(name = "tierListItemInternalId",
                    description = "ID interno (Long) del TierListItem a mover.",
                    required = true, in = ParameterIn.PATH, example = "201",
                    schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long tierListItemInternalId,
            @Valid @org.springframework.web.bind.annotation.RequestBody TierListItemMoveRequestDTO itemMoveRequestDTO) {
        TierListResponseDTO updatedTierList = tierListService.moveItemInTierList(currentUser.getEmail(), tierListPublicId, tierListItemInternalId, itemMoveRequestDTO);
        return ResponseEntity.ok(updatedTierList);
    }

    // Quitar un item de una TierList (SOLO para PROFILE_GLOBAL TierLists)
    @DeleteMapping("/tierlists/{tierListPublicId}/items/{tierListItemInternalId}")
    @Operation(summary = "Eliminar un ítem (juego) de una Tier List de perfil",
            description = "Permite al propietario autenticado eliminar un ítem específico (identificado por `tierListItemInternalId`) de una de sus Tier Lists de tipo 'PROFILE_GLOBAL'. " +
                    "Esto no elimina el juego de la biblioteca general del usuario, solo de esta Tier List. " +
                    "No se puede usar este endpoint para Tier Lists de tipo 'FROM_GAMELIST'. Requiere autenticación y ser propietario.",
            operationId = "removeItemFromTierList",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ítem eliminado exitosamente de la Tier List. Devuelve la Tier List completa y actualizada.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TierListResponseDTO.class))),
            // Un 204 No Content sería una alternativa si no se devolviera el cuerpo de la TierList actualizada.
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta. No se pueden eliminar ítems de una Tier List de tipo 'FROM_GAMELIST' a través de este endpoint.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))), // Para InvalidOperationException
            @ApiResponse(responseCode = "401", description = "No autorizado. El token JWT es inválido, ha expirado o no se proporcionó.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/UnauthorizedResponse"))),
            @ApiResponse(responseCode = "403", description = "Prohibido. El usuario autenticado no es el propietario de la Tier List.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "No encontrado. La Tier List, o el ítem específico dentro de ella, no fueron encontrados para el usuario actual, o el usuario no pudo ser verificado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<TierListResponseDTO> removeItemFromTierList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(name = "tierListPublicId",
                    description = "ID público (UUID) de la Tier List de la cual se eliminará el ítem.",
                    required = true, in = ParameterIn.PATH, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID tierListPublicId,
            @Parameter(name = "tierListItemInternalId",
                    description = "ID interno (Long) del TierListItem a eliminar de la Tier List.",
                    required = true, in = ParameterIn.PATH, example = "201",
                    schema = @Schema(type = "integer", format = "int64"))
            @PathVariable Long tierListItemInternalId) {
        TierListResponseDTO updatedTierList = tierListService.removeItemFromTierList(currentUser.getEmail(), tierListPublicId, tierListItemInternalId);
        return ResponseEntity.ok(updatedTierList);
    }
}