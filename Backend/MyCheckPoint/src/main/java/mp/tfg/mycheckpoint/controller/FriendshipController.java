package mp.tfg.mycheckpoint.controller;

import mp.tfg.mycheckpoint.dto.friendship.FriendshipResponseDTO;
import mp.tfg.mycheckpoint.security.UserDetailsImpl;
import mp.tfg.mycheckpoint.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<FriendshipResponseDTO> sendFriendRequest(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID receiverUserPublicId) {
        FriendshipResponseDTO response = friendshipService.sendFriendRequest(currentUser.getEmail(), receiverUserPublicId);
        // El status code podría variar dependiendo de si se creó una nueva solicitud (CREATED) o se auto-aceptó una existente (OK)
        // Por simplicidad, devolveremos OK en ambos casos exitosos desde la perspectiva del cliente que hizo la acción.
        return ResponseEntity.ok(response);
    }

    // Aceptar una solicitud de amistad
    @PutMapping("/requests/accept/{requesterUserPublicId}")
    public ResponseEntity<FriendshipResponseDTO> acceptFriendRequest(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID requesterUserPublicId) {
        FriendshipResponseDTO response = friendshipService.acceptFriendRequest(currentUser.getEmail(), requesterUserPublicId);
        return ResponseEntity.ok(response);
    }

    // Rechazar/Cancelar una solicitud de amistad
    @DeleteMapping("/requests/decline/{requesterUserPublicId}")
    public ResponseEntity<Void> declineFriendRequest(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID requesterUserPublicId) {
        friendshipService.declineFriendRequest(currentUser.getEmail(), requesterUserPublicId);
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    // Eliminar un amigo
    @DeleteMapping("/{friendUserPublicId}")
    public ResponseEntity<Void> removeFriend(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID friendUserPublicId) {
        friendshipService.removeFriend(currentUser.getEmail(), friendUserPublicId);
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    // Listar todos los amigos aceptados del usuario actual
    @GetMapping
    public ResponseEntity<List<FriendshipResponseDTO>> getMyFriends(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<FriendshipResponseDTO> friends = friendshipService.getFriends(currentUser.getEmail());
        return ResponseEntity.ok(friends);
    }

    // Listar solicitudes de amistad pendientes recibidas por el usuario actual
    @GetMapping("/requests/received")
    public ResponseEntity<List<FriendshipResponseDTO>> getPendingRequestsReceived(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<FriendshipResponseDTO> requests = friendshipService.getPendingRequestsReceived(currentUser.getEmail());
        return ResponseEntity.ok(requests);
    }

    // Listar solicitudes de amistad pendientes enviadas por el usuario actual
    @GetMapping("/requests/sent")
    public ResponseEntity<List<FriendshipResponseDTO>> getPendingRequestsSent(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<FriendshipResponseDTO> requests = friendshipService.getPendingRequestsSent(currentUser.getEmail());
        return ResponseEntity.ok(requests);
    }
}
