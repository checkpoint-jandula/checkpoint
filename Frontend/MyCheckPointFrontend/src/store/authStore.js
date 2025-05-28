// src/store/authStore.js
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

export const useAuthStore = create(
  persist(
    (set) => ({
      token_acceso: null,
      user: null, // Aquí guardarás el UserDTO
      isAuthenticated: false,

      setAuthData: (token, userData) => set({
        token_acceso: token,
        user: userData,
        isAuthenticated: !!token,
      }),

      clearAuthData: () => set({
        token_acceso: null,
        user: null,
        isAuthenticated: false,
      }),
    }),
    {
      name: 'auth-storage', // nombre de la clave en localStorage
      storage: createJSONStorage(() => localStorage),
    }
  )
);

// Esta exportación adicional es para poder usar getState() fuera de componentes React,
// como en tu interceptor de axios.
export const authStoreApi = useAuthStore; // O simplemente exporta useAuthStore y usa useAuthStore.getState()