package mp.tfg.mycheckpoint.service;

import mp.tfg.mycheckpoint.dto.usergame.GameDetailDTO;
import mp.tfg.mycheckpoint.dto.usergame.UserGameDataDTO;
import mp.tfg.mycheckpoint.dto.usergame.UserGameResponseDTO;

import java.util.List;

public interface UserGameLibraryService {

    UserGameResponseDTO addOrUpdateGameInLibrary(String userEmail, Long igdbId, UserGameDataDTO userGameDataDTO);

    List<UserGameResponseDTO> getUserGameLibrary(String userEmail);

    UserGameResponseDTO getUserGameFromLibrary(String userEmail, Long igdbId);

    GameDetailDTO getGameDetailsForUser(Long igdbId, String userEmail); // userEmail puede ser null si el usuario no está logueado

    void removeGameFromLibrary(String userEmail, Long igdbId);
}
