package mp.tfg.mycheckpoint.controller;

import jakarta.validation.Valid;
import mp.tfg.mycheckpoint.dto.usergame.GameDetailDTO;
import mp.tfg.mycheckpoint.dto.usergame.UserGameDataDTO;
import mp.tfg.mycheckpoint.dto.usergame.UserGameResponseDTO;
import mp.tfg.mycheckpoint.security.UserDetailsImpl;
import mp.tfg.mycheckpoint.service.UserGameLibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<UserGameResponseDTO> addOrUpdateGameInMyLibrary(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long igdbId,
            @Valid @RequestBody UserGameDataDTO userGameDataDTO) {
        UserGameResponseDTO response = userGameLibraryService.addOrUpdateGameInLibrary(currentUser.getEmail(), igdbId, userGameDataDTO);
        return ResponseEntity.ok(response);
    }

    // Endpoint para obtener toda la biblioteca del usuario autenticado
    @GetMapping("/users/me/library/games")
    public ResponseEntity<List<UserGameResponseDTO>> getMyGameLibrary(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<UserGameResponseDTO> library = userGameLibraryService.getUserGameLibrary(currentUser.getEmail());
        return ResponseEntity.ok(library);
    }

    // Endpoint para obtener un juego específico de la biblioteca del usuario autenticado
    @GetMapping("/users/me/library/games/{igdbId}")
    public ResponseEntity<UserGameResponseDTO> getSpecificGameFromMyLibrary(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
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
    public ResponseEntity<GameDetailDTO> getGameDetails(
            @PathVariable Long igdbId,
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
    public ResponseEntity<Void> removeGameFromMyLibrary(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long igdbId) {
        userGameLibraryService.removeGameFromLibrary(currentUser.getEmail(), igdbId);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
