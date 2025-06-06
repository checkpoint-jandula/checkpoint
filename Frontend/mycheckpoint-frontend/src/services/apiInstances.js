import apiClient from './apiService';
import { Configuration } from '@/api-client/configuration'; //
import {
    AutenticacinControllerApi,
    UsuariosApi,
    GameControllerApi,
    UserGameLibraryControllerApi,
    GameListControllerApi,
    TierListControllerApi,
    FriendshipControllerApi
    // Importa aquí todos los demás ControllerApi que necesites de @/api-client
} from '@/api-client'; // Asume que api.ts exporta estas clases

const config = new Configuration(); // Puedes dejarla vacía si la baseURL y el token se manejan globalmente en apiClient

// Instancia para Autenticación
const authApi = new AutenticacinControllerApi(config, undefined, apiClient); //

// Instancia para Usuarios
const usersApi = new UsuariosApi(config, undefined, apiClient); //

// Instancia para Juegos (IGDB)
const gamesApi = new GameControllerApi(config, undefined, apiClient); //

// Instancia para la Biblioteca de Juegos del Usuario
const userGameLibraryApi = new UserGameLibraryControllerApi(config, undefined, apiClient); //

// Instancia para Listas de Juegos Personalizadas
const gameListsApi = new GameListControllerApi(config, undefined, apiClient); //

// Instancia para Tier Lists
const tierListsApi = new TierListControllerApi(config, undefined, apiClient); //

// Instancia para Amistades
const friendshipApi = new FriendshipControllerApi(config, undefined, apiClient); //


// Exportaciones que usarás en tus componentes/vistas para llamar a la API
// Estas son las funciones de los *ApiFactory o *ApiFp, o directamente los métodos de las instancias de clase.
// Para simplificar, exportaremos directamente los métodos que necesites o las instancias completas.

// Ejemplo de cómo podrías exportar funciones específicas para facilitar su uso:

// Autenticación
export const loginUser = (loginRequestDTO) => {
  return authApi.authenticateUser(loginRequestDTO); //
};
export const confirmAccount = (token) => { //
  return authApi.confirmUserAccount(token); //
};
export const requestPasswordReset = (forgotPasswordDTO) => { //
  return authApi.forgotPassword(forgotPasswordDTO); //
};
export const resetPasswordWithToken = (resetPasswordDTO) => { //
  return authApi.resetPassword(resetPasswordDTO); //
};


// Usuarios
export const getCurrentAuthenticatedUser = () => {
  return usersApi.getCurrentAuthenticatedUser(); //
};
export const registerUser = (userCreateDTO) => { //
  return usersApi.registrarUsuario(userCreateDTO); //
};
export const updateUserProfile = (userProfileUpdateDTO) => { //
  return usersApi.updateCurrentUserProfile(userProfileUpdateDTO); //
};
export const uploadUserProfilePicture = (file) => {
  return usersApi.uploadProfilePicture(file); //
};
export const changeUserPassword = (passwordChangeDTO) => { //
  return usersApi.changeMyPassword(passwordChangeDTO); //
};
export const deleteUserAccount = (accountDeleteDTO) => { //
  return usersApi.deleteMyAccount(accountDeleteDTO); //
};
export const getUserByPublicId = (publicId) => { //
  return usersApi.getUsuarioByPublicId(publicId); //
};
export const searchUsers = (username) => {
  return usersApi.searchUsersByUsername(username); //
};

//Amistades
export const getMyFriends = () => {
  return friendshipApi.getMyFriends(); // OperationId: getMyFriends
};

//Bibliotecas / Buscar Juego
export const fetchGameDetailsByIgdbId = (igdbId) => {
  return userGameLibraryApi.getGameDetails(igdbId); //
};

export const getMyUserGameLibrary = () => {
  return userGameLibraryApi.getMyGameLibrary(); //
};

export const getPublicUserLibrary = (publicId) => {
  return userGameLibraryApi.getPublicUserLibrary(publicId); //
}

export const addOrUpdateGameInUserLibrary = (igdbId, userGameDataDTO) => {
  return userGameLibraryApi.addOrUpdateGameInMyLibrary(igdbId, userGameDataDTO); //
};

export const removeGameFromUserLibrary = (igdbId) => { // Usado en GameDetailsView
    return userGameLibraryApi.removeGameFromMyLibrary(igdbId); //
};

//Listas de Juegos
export const fetchAllPublicGameLists = () => {
  return gameListsApi.viewAllPublicGameLists(); //
};

export const getMyGameLists = () => {
  return gameListsApi.getMyGameLists(); // OperationId: getMyGameLists
};

export const createMyGameList = (gameListRequestDTO) => {
  return gameListsApi.createMyGameList(gameListRequestDTO); 
};

export const deleteMyGameList = (listPublicId) => {
  return gameListsApi.deleteMyGameList(listPublicId); //
};

export const getMySpecificGameListDetails = (listPublicId) => {
  return gameListsApi.getMySpecificGameList(listPublicId); 
};

export const viewPublicGameListDetails = (listPublicId) => {
  return gameListsApi.viewPublicGameList(listPublicId); 
};

export const updateMyUserGameList = (listPublicId, gameListRequestDTO) => {
  return gameListsApi.updateMyGameList(listPublicId, gameListRequestDTO); 
};

export const addGameToMyGameList = (listPublicId, addGameToCustomListRequestDTO) => {
  return gameListsApi.addGameToMyCustomList(listPublicId, addGameToCustomListRequestDTO); 
};

export const removeGameFromMyGameList = (listPublicId, userGameInternalId) => {
  return gameListsApi.removeGameFromMyCustomList(listPublicId, userGameInternalId); 
};

export const getOrCreateTierListFromGameList = (gameListPublicId) => {
  // Llama al método específico generado, no a un .get() genérico
  return tierListsApi.getOrCreateTierListForGameList(gameListPublicId);
};

//Tier Lists
export const fetchAllPublicTierLists = () => {
  return tierListsApi.getAllPublicTierLists(); 
};

export const getMyProfileTierLists = () => {
  return tierListsApi.getAllProfileTierListsForCurrentUser(); 
};

export const getTierListDetailsByPublicId = (tierListPublicId) => {
  return tierListsApi.getTierListByPublicId(tierListPublicId); 
};

export const createMyProfileTierList = (tierListCreateRequestDTO) => { 
  return tierListsApi.createProfileTierList(tierListCreateRequestDTO); 
};

export const updateMyTierListMetadata = (tierListPublicId, tierListUpdateRequestDTO) => {
  return tierListsApi.updateTierListMetadata(tierListPublicId, tierListUpdateRequestDTO); 
};

export const deleteMyTierList = (tierListPublicId) => {
  return tierListsApi.deleteTierList(tierListPublicId); //
};

export const addSectionToMyTierList = (tierListPublicId, tierSectionRequestDTO) => {
  return tierListsApi.addSectionToTierList(tierListPublicId, tierSectionRequestDTO); //
};

export const updateMySectionName = (tierListPublicId, sectionInternalId, tierSectionRequestDTO) => {
  // tierListsApi es tu instancia de TierListControllerApi
  // updateSectionName es el método generado que corresponde a PUT /api/v1/tierlists/{tierListPublicId}/sections/{sectionInternalId}
  return tierListsApi.updateSectionName(tierListPublicId, sectionInternalId, tierSectionRequestDTO); //
};

export const removeSectionFromMyTierList = (tierListPublicId, sectionInternalId) => {
  return tierListsApi.removeSectionFromTierList(tierListPublicId, sectionInternalId); //
};

export const addItemToMyUnclassifiedSection = (tierListPublicId, tierListItemAddRequestDTO) => {
  return tierListsApi.addItemToUnclassifiedSection(tierListPublicId, tierListItemAddRequestDTO); //
};

export const removeItemFromMyTierList = (tierListPublicId, tierListItemInternalId) => {
  return tierListsApi.removeItemFromTierList(tierListPublicId, tierListItemInternalId); //
};

export const moveItemInMyTierList = (tierListPublicId, tierListItemInternalId, tierListItemMoveRequestDTO) => {
  return tierListsApi.moveItemInTierList(tierListPublicId, tierListItemInternalId, tierListItemMoveRequestDTO); //
};




