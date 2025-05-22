package mp.tfg.mycheckpoint.service;

import mp.tfg.mycheckpoint.dto.tierlist.*; // Asegúrate que todos los DTOs de tierlist estén importados
import java.util.List;
import java.util.UUID;

public interface TierListService {

    // --- Gestión General de TierLists ---
    TierListResponseDTO createProfileTierList(String userEmail, TierListCreateRequestDTO createRequestDTO);
    TierListResponseDTO getOrCreateTierListForGameList(String userEmail, UUID gameListPublicId);
    TierListResponseDTO getTierListByPublicId(UUID tierListPublicId, String userEmail); // userEmail para validación de acceso
    List<TierListResponseDTO> getAllProfileTierListsForUser(String userEmail);
    List<TierListResponseDTO> getAllPublicTierLists(); // Para una vista pública general de TierLists
    TierListResponseDTO updateTierListMetadata(String userEmail, UUID tierListPublicId, TierListUpdateRequestDTO updateRequestDTO);
    void deleteTierList(String userEmail, UUID tierListPublicId);

    // --- Gestión de Secciones dentro de una TierList ---
    TierListResponseDTO addSectionToTierList(String userEmail, UUID tierListPublicId, TierSectionRequestDTO sectionRequestDTO);
    TierListResponseDTO updateSectionName(String userEmail, UUID tierListPublicId, Long sectionInternalId, TierSectionRequestDTO sectionRequestDTO);
    TierListResponseDTO removeSectionFromTierList(String userEmail, UUID tierListPublicId, Long sectionInternalId);

    // --- Gestión de Items (Juegos) dentro de las Secciones ---
    // Para TierLists de Perfil
    TierListResponseDTO addItemToTierListSection(String userEmail, UUID tierListPublicId, Long sectionInternalId, TierListItemAddRequestDTO itemAddRequestDTO);
    TierListResponseDTO addItemToUnclassifiedSection(String userEmail, UUID tierListPublicId, TierListItemAddRequestDTO itemAddRequestDTO);
    TierListResponseDTO moveItemInTierList(String userEmail, UUID tierListPublicId, Long tierListItemInternalId, TierListItemMoveRequestDTO itemMoveRequestDTO);
    TierListResponseDTO removeItemFromTierList(String userEmail, UUID tierListPublicId, Long tierListItemInternalId);

    // Para TierLists derivadas de GameList (solo mover/reordenar, añadir/quitar es vía GameList)
    // El endpoint de mover es el mismo, pero la lógica interna puede variar o tener validaciones extra.
    // No se necesitan endpoints separados para añadir/quitar items aquí, ya que se sincroniza desde la GameList.
}