package mp.tfg.mycheckpoint.service;

import mp.tfg.mycheckpoint.dto.gameList.GameListRequestDTO;
import mp.tfg.mycheckpoint.dto.gameList.GameListResponseDTO;

import java.util.List;
import java.util.UUID;

public interface GameListService {

    // CRUD para listas
    GameListResponseDTO createGameList(String userEmail, GameListRequestDTO requestDTO);
    List<GameListResponseDTO> getAllGameListsForUser(String userEmail);
    GameListResponseDTO getGameListByPublicIdForUser(String userEmail, UUID listPublicId); // Propietario ve su lista
    GameListResponseDTO updateGameList(String userEmail, UUID listPublicId, GameListRequestDTO requestDTO);
    void deleteGameList(String userEmail, UUID listPublicId);

    // Gestión de juegos en la lista
    GameListResponseDTO addGameToCustomList(String userEmail, UUID listPublicId, Long userGameInternalId);
    void removeGameFromCustomList(String userEmail, UUID listPublicId, Long userGameInternalId);

    // Para visualización pública
    List<GameListResponseDTO> getAllPublicGameLists();
    GameListResponseDTO getPublicGameListByPublicId(UUID listPublicId); // Cualquiera ve una lista pública
}
