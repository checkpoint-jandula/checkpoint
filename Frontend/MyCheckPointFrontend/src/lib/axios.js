// src/lib/axios.js
import axios from 'axios';
import { useAuthStore } from '../store/authStore'; // Asegúrate que la ruta a tu store sea correcta

const apiClient = axios.create({
  baseURL: 'http://localhost:8080', // O la URL base de tu spec. Ajusta si es necesario.
  // Puedes añadir otros defaults aquí
});

apiClient.interceptors.request.use(
  (config) => {
    // Intenta obtener el token desde el hook de Zustand
    // Esto solo funcionará si este código se ejecuta en un contexto donde los hooks son válidos
    // o si authStore.getState() está disponible y funciona como se espera.
    const token = useAuthStore.getState().token_acceso;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default apiClient; // Exporta la instancia configurada