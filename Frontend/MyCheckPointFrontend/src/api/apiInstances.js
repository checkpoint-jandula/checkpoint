// src/api/apiInstances.js
import {
  Configuration,
  UsuariosApi,
  AutenticacinControllerApi, // <--- NOMBRE CORREGIDO
  GameControllerApi,
  GameListControllerApi,
  UserGameLibraryControllerApi,
  TierListControllerApi,
  FriendshipControllerApi
  // Asegúrate de que todos los nombres de tus APIs coincidan con los generados en src/api/api.ts
} from './index'; // Esto toma el index.ts de la carpeta api

import apiClient from '../lib/axios.js'; // <--- Ruta correcta a tu instancia de Axios

const generatedApiConfig = new Configuration({
  basePath: 'http://localhost:8080', // Tu URL base
  // Como vimos, tu configuration.ts no tiene un campo para 'axios',
  // así que pasaremos apiClient como tercer argumento a los constructores de Api.
});

// Pasamos apiClient como el TERCER argumento.
// Constructor de BaseAPI: (configuration?, basePath?, axios?)
export const usuariosApiInstance = new UsuariosApi(generatedApiConfig, generatedApiConfig.basePath, apiClient);
export const authApiInstance = new AutenticacinControllerApi(generatedApiConfig, generatedApiConfig.basePath, apiClient); // <--- NOMBRE CORREGIDO
export const gameControllerApiInstance = new GameControllerApi(generatedApiConfig, generatedApiConfig.basePath, apiClient);
export const gameListControllerApiInstance = new GameListControllerApi(generatedApiConfig, generatedApiConfig.basePath, apiClient);
export const userGameLibraryControllerApiInstance = new UserGameLibraryControllerApi(generatedApiConfig, generatedApiConfig.basePath, apiClient);
export const tierListControllerApiInstance = new TierListControllerApi(generatedApiConfig, generatedApiConfig.basePath, apiClient);
export const friendshipControllerApiInstance = new FriendshipControllerApi(generatedApiConfig, generatedApiConfig.basePath, apiClient);