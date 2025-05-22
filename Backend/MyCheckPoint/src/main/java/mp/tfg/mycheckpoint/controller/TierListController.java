package mp.tfg.mycheckpoint.controller;

import jakarta.validation.Valid;
import mp.tfg.mycheckpoint.dto.tierlist.*;
import mp.tfg.mycheckpoint.security.UserDetailsImpl;
import mp.tfg.mycheckpoint.service.TierListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<TierListResponseDTO> createProfileTierList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody TierListCreateRequestDTO createRequestDTO) {
        TierListResponseDTO createdTierList = tierListService.createProfileTierList(currentUser.getEmail(), createRequestDTO);
        return new ResponseEntity<>(createdTierList, HttpStatus.CREATED);
    }

    @GetMapping("/users/me/tierlists")
    public ResponseEntity<List<TierListResponseDTO>> getAllProfileTierListsForCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        List<TierListResponseDTO> tierLists = tierListService.getAllProfileTierListsForUser(currentUser.getEmail());
        return ResponseEntity.ok(tierLists);
    }

    // --- Endpoints para TierLists asociadas a GameLists ---

    @GetMapping("/gamelists/{gameListPublicId}/tierlist")
    public ResponseEntity<TierListResponseDTO> getOrCreateTierListForGameList(
            @AuthenticationPrincipal UserDetailsImpl currentUser, // Puede ser null si la GameList es pública
            @PathVariable UUID gameListPublicId) {
        String userEmail = (currentUser != null) ? currentUser.getEmail() : null;
        TierListResponseDTO tierList = tierListService.getOrCreateTierListForGameList(userEmail, gameListPublicId);
        return ResponseEntity.ok(tierList);
    }

    // --- Endpoints Generales para TierLists (aplican a ambos tipos, con validación de permisos) ---

    @GetMapping("/tierlists/public")
    public ResponseEntity<List<TierListResponseDTO>> getAllPublicTierLists() {
        List<TierListResponseDTO> publicTierLists = tierListService.getAllPublicTierLists();
        return ResponseEntity.ok(publicTierLists);
    }

    @GetMapping("/tierlists/{tierListPublicId}")
    public ResponseEntity<TierListResponseDTO> getTierListByPublicId(
            @PathVariable UUID tierListPublicId,
            Authentication authentication) { // Permite acceso anónimo si la TierList es pública
        String userEmail = null;
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
            userEmail = currentUser.getEmail();
        }
        TierListResponseDTO tierList = tierListService.getTierListByPublicId(tierListPublicId, userEmail);
        return ResponseEntity.ok(tierList);
    }

    @PutMapping("/tierlists/{tierListPublicId}")
    public ResponseEntity<TierListResponseDTO> updateTierListMetadata(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID tierListPublicId,
            @Valid @RequestBody TierListUpdateRequestDTO updateRequestDTO) {
        TierListResponseDTO updatedTierList = tierListService.updateTierListMetadata(currentUser.getEmail(), tierListPublicId, updateRequestDTO);
        return ResponseEntity.ok(updatedTierList);
    }

    @DeleteMapping("/tierlists/{tierListPublicId}")
    public ResponseEntity<Void> deleteTierList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID tierListPublicId) {
        tierListService.deleteTierList(currentUser.getEmail(), tierListPublicId);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints para Secciones (Tiers) dentro de una TierList ---

    @PostMapping("/tierlists/{tierListPublicId}/sections")
    public ResponseEntity<TierListResponseDTO> addSectionToTierList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID tierListPublicId,
            @Valid @RequestBody TierSectionRequestDTO sectionRequestDTO) {
        TierListResponseDTO updatedTierList = tierListService.addSectionToTierList(currentUser.getEmail(), tierListPublicId, sectionRequestDTO);
        return ResponseEntity.ok(updatedTierList);
    }

    @PutMapping("/tierlists/{tierListPublicId}/sections/{sectionInternalId}")
    public ResponseEntity<TierListResponseDTO> updateSectionName(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID tierListPublicId,
            @PathVariable Long sectionInternalId,
            @Valid @RequestBody TierSectionRequestDTO sectionRequestDTO) {
        TierListResponseDTO updatedTierList = tierListService.updateSectionName(currentUser.getEmail(), tierListPublicId, sectionInternalId, sectionRequestDTO);
        return ResponseEntity.ok(updatedTierList);
    }

    @DeleteMapping("/tierlists/{tierListPublicId}/sections/{sectionInternalId}")
    public ResponseEntity<TierListResponseDTO> removeSectionFromTierList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID tierListPublicId,
            @PathVariable Long sectionInternalId) {
        TierListResponseDTO updatedTierList = tierListService.removeSectionFromTierList(currentUser.getEmail(), tierListPublicId, sectionInternalId);
        return ResponseEntity.ok(updatedTierList);
    }

    // --- Endpoints para Items (Juegos) dentro de las Secciones de una TierList ---

    // Añadir a una sección específica (principalmente para PROFILE_GLOBAL TierLists)
    @PostMapping("/tierlists/{tierListPublicId}/sections/{sectionInternalId}/items")
    public ResponseEntity<TierListResponseDTO> addItemToTierListSection(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID tierListPublicId,
            @PathVariable Long sectionInternalId,
            @Valid @RequestBody TierListItemAddRequestDTO itemAddRequestDTO) {
        TierListResponseDTO updatedTierList = tierListService.addItemToTierListSection(currentUser.getEmail(), tierListPublicId, sectionInternalId, itemAddRequestDTO);
        return ResponseEntity.ok(updatedTierList);
    }

    // Añadir a la sección "Sin Clasificar" (para PROFILE_GLOBAL TierLists)
    @PostMapping("/tierlists/{tierListPublicId}/items/unclassified")
    public ResponseEntity<TierListResponseDTO> addItemToUnclassifiedSection(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID tierListPublicId,
            @Valid @RequestBody TierListItemAddRequestDTO itemAddRequestDTO) {
        TierListResponseDTO updatedTierList = tierListService.addItemToUnclassifiedSection(currentUser.getEmail(), tierListPublicId, itemAddRequestDTO);
        return ResponseEntity.ok(updatedTierList);
    }

    // Mover un item dentro de una TierList (cambiar sección y/o orden)
    // Aplica a PROFILE_GLOBAL y también para reordenar en FROM_GAMELIST TierLists
    @PutMapping("/tierlists/{tierListPublicId}/items/{tierListItemInternalId}/move")
    public ResponseEntity<TierListResponseDTO> moveItemInTierList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID tierListPublicId,
            @PathVariable Long tierListItemInternalId,
            @Valid @RequestBody TierListItemMoveRequestDTO itemMoveRequestDTO) {
        TierListResponseDTO updatedTierList = tierListService.moveItemInTierList(currentUser.getEmail(), tierListPublicId, tierListItemInternalId, itemMoveRequestDTO);
        return ResponseEntity.ok(updatedTierList);
    }

    // Quitar un item de una TierList (SOLO para PROFILE_GLOBAL TierLists)
    @DeleteMapping("/tierlists/{tierListPublicId}/items/{tierListItemInternalId}")
    public ResponseEntity<TierListResponseDTO> removeItemFromTierList(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable UUID tierListPublicId,
            @PathVariable Long tierListItemInternalId) {
        TierListResponseDTO updatedTierList = tierListService.removeItemFromTierList(currentUser.getEmail(), tierListPublicId, tierListItemInternalId);
        return ResponseEntity.ok(updatedTierList);
    }
}