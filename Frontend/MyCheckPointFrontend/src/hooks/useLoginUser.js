// src/hooks/useLoginUser.js
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { authApiInstance, usuariosApiInstance } from '../api/apiInstances';
import { useAuthStore } from '../store/authStore';

export const useLoginUser = () => {
  const queryClient = useQueryClient();
  const { setAuthData, clearAuthData } = useAuthStore(); // clearAuthData por si falla el segundo paso

  return useMutation({
    mutationFn: async (loginData) => {
      // Paso 1: Solo autenticar y obtener el token
      const authResponse = await authApiInstance.authenticateUser(loginData);
      // El authResponse.data es JwtResponseDTO según tu API
      return authResponse.data; // Devuelve solo JwtResponseDTO
    },
    onSuccess: async (jwtResponse, variables, context) => {
      // jwtResponse aquí es el JwtResponseDTO devuelto por mutationFn
      if (!jwtResponse?.token_acceso) {
        // Esto no debería pasar si mutationFn no lanzó error, pero es una doble verificación.
        console.error("Login success, pero no se recibió token_acceso en onSuccess.");
        throw new Error("Login success, pero no se recibió token_acceso en onSuccess.");
      }

      // Paso 2: Guardar el token en Zustand PRIMERO
      // Guardamos el usuario como null temporalmente hasta que lo obtengamos
      setAuthData(jwtResponse.token_acceso, null);

      try {
        // Paso 3: Ahora que el token está en Zustand (y el interceptor lo usará),
        // obtener los datos del usuario.
        const userProfileResponse = await usuariosApiInstance.getCurrentAuthenticatedUser();
        const userDTO = userProfileResponse.data; // UserDTO

        // Paso 4: Actualizar Zustand con los datos completos del usuario
        setAuthData(jwtResponse.token_acceso, userDTO);

        // Invalidar queries que podrían depender de los datos del usuario
        queryClient.invalidateQueries({ queryKey: ['userProfile'] }); // Si tienes una query para el perfil
        console.log('Login y obtención de perfil exitosos (desde onSuccess del hook):', userDTO);
        
        // El componente LoginPage se encargará de la redirección en su propio onSuccess
        // que se pasa a loginMutation.mutate()

      } catch (profileError) {
        console.error("Error al obtener el perfil del usuario después del login (en onSuccess del hook):", profileError.response?.data || profileError.message);
        // Si no se puede obtener el perfil, es un estado inconsistente.
        // Es mejor limpiar el token y que el usuario intente de nuevo.
        clearAuthData(); // Limpia el token guardado
        // Relanzamos el error para que el componente LoginPage lo maneje en su 'onError'
        throw profileError;
      }
    },
    onError: (error, variables, context) => {
      // Este onError se activa si mutationFn (authenticateUser) falla,
      // o si el re-lanzamiento de error desde onSuccess ocurre.
      console.error("Error en la mutación de login (hook useLoginUser):", error.response?.data || error.message);
      clearAuthData(); // Asegurarse de limpiar el estado de autenticación si el login falla
    },
  });
};