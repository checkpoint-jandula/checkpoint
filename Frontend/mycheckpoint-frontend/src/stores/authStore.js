import { defineStore } from 'pinia';
import { getCurrentAuthenticatedUser } from '@/services/apiInstances'; // Lo crearemos en el siguiente paso
// Asegúrate que UserDTO se exporta correctamente desde tu api-client
// Si no es así, ajústalo. Podría estar en './../api-client' o './../api-client/api'
// import type { UserDTO } from '@/api-client'; // Si usas TS para el store
// Para JS, no necesitas la importación de tipo aquí, pero sí para las instancias de API.

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token_acceso: localStorage.getItem('token_acceso') || null,
    /** @type {import('@/api-client').UserDTO | null} */
    user: JSON.parse(localStorage.getItem('user')) || null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.token_acceso && !!state.user,
    /** @returns {import('@/api-client').UserDTO | null} */
    currentUser: (state) => state.user,
    getUserToken: (state) => state.token_acceso,
  },
  actions: {
    /**
     * Establece el token de acceso y guarda la información del usuario.
     * @param {import('@/api-client').JwtResponseDTO} authData - Los datos de autenticación, incluyendo el token.
     */
    async loginSuccess(authData) {
      this.token_acceso = authData.token_acceso;
      localStorage.setItem('token_acceso', authData.token_acceso);
      await this.fetchCurrentUser();
    },

    async fetchCurrentUser() {
      if (this.token_acceso) {
        try {
          // No necesitas pasar el token aquí si el interceptor de Axios ya está configurado
          const response = await getCurrentAuthenticatedUser(); // Esta función usará la instancia de UsuariosApi
          this.user = response.data;
          localStorage.setItem('user', JSON.stringify(response.data));
        } catch (error) {
          console.error("Error al obtener los datos del usuario:", error);
          // Podrías querer limpiar el estado si la obtención del usuario falla
          // this.logout();
        }
      }
    },

    logout() {
      this.token_acceso = null;
      this.user = null;
      localStorage.removeItem('token_acceso');
      localStorage.removeItem('user');
      // Opcional: Redirigir al login. Esto se puede hacer también desde el componente que llama a logout.
      // import router from '@/router'; // Cuidado con importaciones cíclicas si el router usa el store
      // router.push('/login');
    },

    // Acción para cargar el estado desde localStorage al iniciar la app (si es necesario)
    // Esto se puede hacer directamente en el state como se muestra arriba.
    // loadStateFromLocalStorage() {
    //   const token = localStorage.getItem('token_acceso');
    //   const userData = localStorage.getItem('user');
    //   if (token) {
    //     this.token_acceso = token;
    //   }
    //   if (userData) {
    //     this.user = JSON.parse(userData);
    //   }
    // }
  },
});