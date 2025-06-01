<template>
  <div class="request-reset-container">
    <h2>Restablecer Contraseña</h2>
    <p>Ingresa tu dirección de correo electrónico y te enviaremos un enlace para restablecer tu contraseña.</p>
    <form @submit.prevent="handleRequestReset">
      <div class="form-group">
        <label for="email">Email:</label>
        <input type="email" id="email" v-model="email" required />
      </div>

      <button type="submit" :disabled="isLoading">
        {{ isLoading ? 'Enviando...' : 'Enviar Enlace de Restablecimiento' }}
      </button>
    </form>

    <div v-if="message" :class="isError ? 'error-message' : 'success-message'">
      {{ message }}
    </div>
    <div class="extra-links">
      <router-link to="/login">Volver a Iniciar Sesión</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { requestPasswordReset } from '@/services/apiInstances.js'; // Importamos la función

const email = ref('');
const message = ref('');
const isError = ref(false);
const isLoading = ref(false);

const handleRequestReset = async () => {
  message.value = '';
  isError.value = false;
  isLoading.value = true;

  /** @type {import('@/api-client/index.js').ForgotPasswordDTO} */
  const forgotPasswordData = { //
    email: email.value, //
  };

  try {
    await requestPasswordReset(forgotPasswordData); //
    // La API siempre devuelve 200 OK para no revelar si un email existe.
    message.value = 'Si tu dirección de correo electrónico está registrada en nuestro sistema, recibirás un correo con instrucciones para restablecer tu contraseña en breve.';
    isError.value = false;
    email.value = ''; // Limpiar el campo
  } catch (error) {
    // Aunque la documentación dice que siempre devuelve 200,
    // manejamos errores por si hay problemas de validación del DTO (ej. email mal formado) o de red.
    if (error.response && error.response.data) {
      console.error("Datos del error:", error.response.data);
      // Para el endpoint forgotPassword, el status 400 sería por email inválido.
      if (error.response.status === 400 && error.response.data.errors) {
        message.value = error.response.data.errors.join(', ');
      } else {
        message.value = error.response.data.error || 'Ocurrió un error al procesar tu solicitud.';
      }
    } else if (error.request) {
      message.value = 'No se pudo conectar con el servidor. Inténtalo más tarde.';
    } else {
      message.value = 'Ocurrió un error inesperado.';
    }
    isError.value = true;
    console.error("Error en la solicitud de restablecimiento:", error);
  } finally {
    isLoading.value = false;
  }
};
</script>

<style src="./RequestPasswordResetView.css" scoped></style>