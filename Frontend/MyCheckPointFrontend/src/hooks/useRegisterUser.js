// src/hooks/useRegisterUser.js
import { useMutation } from '@tanstack/react-query';
import { usuariosApiInstance } from '../api/apiInstances'; // Importa tu instancia

export const useRegisterUser = () => {
  return useMutation({
    mutationFn: async (userData) => {
      // userData debe tener la estructura de UserCreateDTO
      // La función del cliente generado se llamará algo como 'registrarUsuario'
      // o similar, dependiendo de tu operationId.
      // Revisa el archivo generado en src/api/UsuariosApi.js para el nombre exacto.
      // Asumamos que es 'registrarUsuario'.
      const response = await usuariosApiInstance.registrarUsuario(userData);
      return response.data; // El UserDTO devuelto por la API en caso de éxito (201)
    },
    // onSuccess y onError se pueden manejar aquí o en el componente que usa el hook.
    // Por simplicidad, los mensajes básicos se pueden manejar en el componente
    // usando registerMutation.isError, registerMutation.error, etc.
    // pero para lógica más compleja (ej. analíticas, redirecciones globales),
    // puedes ponerlos aquí.
  });
};