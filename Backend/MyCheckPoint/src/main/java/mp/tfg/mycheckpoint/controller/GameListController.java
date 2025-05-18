package mp.tfg.mycheckpoint.controller;

import jakarta.validation.Valid;
import mp.tfg.mycheckpoint.dto.gameList.AddGameToCustomListRequestDTO;
import mp.tfg.mycheckpoint.dto.gameList.GameListRequestDTO;
import mp.tfg.mycheckpoint.dto.gameList.GameListResponseDTO;
import mp.tfg.mycheckpoint.security.UserDetailsImpl;
import mp.tfg.mycheckpoint.service.GameListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<GameListResponseDTO> createMyGameList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody GameListRequestDTO requestDTO) {
        GameListResponseDTO createdList = gameListService.createGameList(currentUser.getEmail(), requestDTO);
        return new ResponseEntity<>(createdList, HttpStatus.CREATED);
    }

    @GetMapping("/users/me/gamelists")
    public ResponseEntity<List<GameListResponseDTO>> getMyGameLists(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<GameListResponseDTO> lists = gameListService.getAllGameListsForUser(currentUser.getEmail());
        return ResponseEntity.ok(lists);
    }

    @GetMapping("/users/me/gamelists/{listPublicId}")
    public ResponseEntity<GameListResponseDTO> getMySpecificGameList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID listPublicId) {
        GameListResponseDTO list = gameListService.getGameListByPublicIdForUser(currentUser.getEmail(), listPublicId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/users/me/gamelists/{listPublicId}")
    public ResponseEntity<GameListResponseDTO> updateMyGameList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID listPublicId,
            @Valid @RequestBody GameListRequestDTO requestDTO) {
        GameListResponseDTO updatedList = gameListService.updateGameList(currentUser.getEmail(), listPublicId, requestDTO);
        return ResponseEntity.ok(updatedList);
    }

    @DeleteMapping("/users/me/gamelists/{listPublicId}")
    public ResponseEntity<Void> deleteMyGameList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID listPublicId) {
        gameListService.deleteGameList(currentUser.getEmail(), listPublicId);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints para gestionar juegos DENTRO de una lista del usuario autenticado ---

    @PostMapping("/users/me/gamelists/{listPublicId}/games")
    public ResponseEntity<GameListResponseDTO> addGameToMyCustomList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID listPublicId,
            @Valid @RequestBody AddGameToCustomListRequestDTO requestDTO) {
        GameListResponseDTO updatedList = gameListService.addGameToCustomList(
                currentUser.getEmail(), listPublicId, requestDTO.getUserGameId());
        return ResponseEntity.ok(updatedList);
    }

    // Se usa el userGameInternalId como PathVariable aquí para la eliminación.
    // Podrías optar por usar gameIgdbId si es más conveniente para el frontend,
    // pero entonces el servicio necesitaría buscar UserGame basado en user + gameIgdbId.
    @DeleteMapping("/users/me/gamelists/{listPublicId}/games/{userGameInternalId}")
    public ResponseEntity<Void> removeGameFromMyCustomList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID listPublicId,
            @PathVariable Long userGameInternalId) {
        gameListService.removeGameFromCustomList(currentUser.getEmail(), listPublicId, userGameInternalId);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints públicos para visualización de listas ---

    @GetMapping("/gamelists/public")
    public ResponseEntity<List<GameListResponseDTO>> viewAllPublicGameLists() {
        List<GameListResponseDTO> publicLists = gameListService.getAllPublicGameLists();
        return ResponseEntity.ok(publicLists);
    }

    @GetMapping("/gamelists/{listPublicId}/public")
    public ResponseEntity<GameListResponseDTO> viewPublicGameList(@PathVariable UUID listPublicId) {
        GameListResponseDTO publicList = gameListService.getPublicGameListByPublicId(listPublicId);
        return ResponseEntity.ok(publicList);
    }
}
