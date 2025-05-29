import axios from 'axios';
import { useAuthStore } from '@/stores/authStore'; // Lo crearemos en el siguiente paso
import { BASE_PATH } from '@/api-client/base'; //

const apiClient = axios.create({
  baseURL: BASE_PATH //
});

// Interceptor de solicitud para añadir el token JWT
apiClient.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    const token = authStore.token_acceso; // Asumiendo que así se llama en tu store
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// (Opcional) Interceptor de respuesta para manejar errores comunes como 401
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    const authStore = useAuthStore();
    if (error.response && error.response.status === 401) {
      // Token inválido o expirado
      console.error("Error 401: No autorizado. Deslogueando...");
      authStore.logout(); // Llama a una acción de logout en tu store
      // Podrías redirigir al login aquí si tu router está disponible
      // import router from '@/router'; // Asegúrate que la importación sea correcta y no cause ciclos
      // router.push('/login');
    }
    return Promise.reject(error);
  }
);

export default apiClient;