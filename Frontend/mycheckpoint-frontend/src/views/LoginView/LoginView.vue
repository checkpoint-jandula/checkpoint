<template>
  <div class="login-container">
    <h2>Iniciar Sesión</h2>
    <form @submit.prevent="handleLogin">
      <div class="form-group">
        <label for="identifier">Email o Nombre de Usuario:</label>
        <input type="text" id="identifier" v-model="identifier" required />
      </div>
      <div class="form-group">
        <label for="password">Contraseña:</label>
        <input type="password" id="password" v-model="password" required />
      </div>

      <button type="submit" :disabled="isLoading">
        {{ isLoading ? 'Iniciando sesión...' : 'Iniciar Sesión' }}
      </button>
    </form>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    
    <div class="extra-links">
      <router-link to="/request-password-reset">¿Olvidaste tu contraseña?</router-link>
      <br>
      <router-link to="/register">¿No tienes cuenta? Regístrate</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore.js';
import { loginUser } from '@/services/apiInstances.js'; // Importamos la función para login

const identifier = ref('');
const password = ref('');
const errorMessage = ref('');
const isLoading = ref(false);

const authStore = useAuthStore();
const router = useRouter();

const handleLogin = async () => {
  errorMessage.value = '';
  isLoading.value = true;

  /** @type {import('@/api-client/index.js').LoginRequestDTO} */
  const loginData = { //
    identificador: identifier.value, //
    contraseña: password.value, //
  };

  try {
    const response = await loginUser(loginData); //
    // El backend devuelve JwtResponseDTO en caso de éxito (200)
    // La acción loginSuccess en authStore se encargará de guardar el token
    // y luego llamar a fetchCurrentUser para obtener los datos del usuario.
    await authStore.loginSuccess(response.data); //

    // Redirigir a la página de inicio o dashboard
    router.push('/');

  } catch (error) {
    if (error.response) {
      console.error("Datos del error:", error.response.data);
      console.error("Estado del error:", error.response.status);

      const status = error.response.status;
      const data = error.response.data;

      if (status === 400) { //
        // Errores de validación de campos del DTO
        if (data && data.errors && data.errors.length > 0) { //
            errorMessage.value = data.errors.join(', ');
        } else {
            errorMessage.value = 'Los datos proporcionados son incorrectos.';
        }
      } else if (status === 401) { //
        // Credenciales incorrectas o fallo general de autenticación
         errorMessage.value = data.message || 'Credenciales incorrectas. Por favor, inténtalo de nuevo.'; //
      } else if (status === 403) { //
        // Cuenta deshabilitada (ej. email no verificado) o eliminada
        errorMessage.value = data.error || 'Acceso prohibido. Tu cuenta podría estar deshabilitada o necesitar verificación.'; //
      } else {
        errorMessage.value = `Error del servidor (${status}): ${data.error || 'Ocurrió un problema al intentar iniciar sesión.'}`;
      }
    } else if (error.request) {
      console.error("Error de red:", error.request);
      errorMessage.value = 'No se pudo conectar con el servidor. Por favor, inténtalo más tarde.';
    } else {
      console.error('Error de configuración:', error.message);
      errorMessage.value = `Error: ${error.message}`;
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style src="./LoginView.css" scoped></style>