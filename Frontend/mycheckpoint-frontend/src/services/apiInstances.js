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
//Bibliotecas / Buscar Juego
export const fetchGameDetailsByIgdbId = (igdbId) => {
  return userGameLibraryApi.getGameDetails(igdbId); //
};

export const getMyUserGameLibrary = () => {
  return userGameLibraryApi.getMyGameLibrary(); //
};

export const addOrUpdateGameInUserLibrary = (igdbId, userGameDataDTO) => {
  return userGameLibraryApi.addOrUpdateGameInMyLibrary(igdbId, userGameDataDTO); //
};

export const removeGameFromUserLibrary = (igdbId) => { // Usado en GameDetailsView
    return userGameLibraryApi.removeGameFromMyLibrary(igdbId); //
};

