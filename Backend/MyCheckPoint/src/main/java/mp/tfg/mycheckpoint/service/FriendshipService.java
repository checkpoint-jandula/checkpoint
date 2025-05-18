package mp.tfg.mycheckpoint.service;

import mp.tfg.mycheckpoint.dto.friendship.FriendshipResponseDTO;

import java.util.List;
import java.util.UUID;

public interface FriendshipService {

    /**
     * Envía una solicitud de amistad desde el usuario actual al usuario con el publicId proporcionado.
     * Si ya existe una solicitud pendiente del receptor al solicitante, la amistad se acepta automáticamente.
     *
     * @param senderUserEmail Email del usuario que envía la solicitud.
     * @param receiverUserPublicId UUID público del usuario que recibe la solicitud.
     * @return FriendshipResponseDTO representando el estado de la nueva solicitud o amistad.
     */
    FriendshipResponseDTO sendFriendRequest(String senderUserEmail, UUID receiverUserPublicId);

    /**
     * Acepta una solicitud de amistad pendiente.
     *
     * @param currentUserEmail Email del usuario que acepta la solicitud (debe ser el receptor).
     * @param requesterUserPublicId UUID público del usuario que envió la solicitud.
     * @return FriendshipResponseDTO representando la amistad aceptada.
     */
    FriendshipResponseDTO acceptFriendRequest(String currentUserEmail, UUID requesterUserPublicId);

    /**
     * Rechaza una solicitud de amistad pendiente.
     *
     * @param currentUserEmail Email del usuario que rechaza la solicitud (debe ser el receptor).
     * @param requesterUserPublicId UUID público del usuario que envió la solicitud.
     */
    void declineFriendRequest(String currentUserEmail, UUID requesterUserPublicId);

    /**
     * Elimina una amistad existente entre el usuario actual y otro usuario.
     *
     * @param currentUserEmail Email del usuario que inicia la eliminación.
     * @param friendUserPublicId UUID público del amigo a eliminar.
     */
    void removeFriend(String currentUserEmail, UUID friendUserPublicId);

    /**
     * Obtiene la lista de amigos (amistades aceptadas) del usuario actual.
     *
     * @param currentUserEmail Email del usuario actual.
     * @return Lista de DTOs representando a los amigos.
     */
    List<FriendshipResponseDTO> getFriends(String currentUserEmail);

    /**
     * Obtiene la lista de solicitudes de amistad pendientes recibidas por el usuario actual.
     *
     * @param currentUserEmail Email del usuario actual.
     * @return Lista de DTOs representando las solicitudes pendientes.
     */
    List<FriendshipResponseDTO> getPendingRequestsReceived(String currentUserEmail);

    /**
     * Obtiene la lista de solicitudes de amistad pendientes enviadas por el usuario actual.
     *
     * @param currentUserEmail Email del usuario actual.
     * @return Lista de DTOs representando las solicitudes enviadas.
     */
    List<FriendshipResponseDTO> getPendingRequestsSent(String currentUserEmail);

}