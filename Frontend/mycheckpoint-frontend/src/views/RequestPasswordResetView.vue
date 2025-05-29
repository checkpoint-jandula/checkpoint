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
import { requestPasswordReset } from '@/services/apiInstances'; // Importamos la función

const email = ref('');
const message = ref('');
const isError = ref(false);
const isLoading = ref(false);

const handleRequestReset = async () => {
  message.value = '';
  isError.value = false;
  isLoading.value = true;

  /** @type {import('@/api-client').ForgotPasswordDTO} */
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

<style scoped>
.request-reset-container {
  max-width: 450px;
  margin: 2rem auto;
  padding: 2rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background-color: var(--color-background-soft);
}

.request-reset-container p {
  margin-bottom: 1.5rem;
  text-align: center;
  color: var(--color-text);
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--color-heading);
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  box-sizing: border-box;
  background-color: var(--color-background);
  color: var(--color-text);
}

button {
  width: 100%;
  padding: 0.75rem;
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

button:disabled {
  background-color: #ccc;
}

.error-message,
.success-message {
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 4px;
  text-align: center;
}

.error-message {
  background-color: rgba(255, 0, 0, 0.1);
  border: 1px solid rgba(255, 0, 0, 0.3);
  color: red;
}

.success-message {
  background-color: rgba(0, 128, 0, 0.1); /* Verde más sutil */
  border: 1px solid rgba(0, 128, 0, 0.3);
  color: green;
}
.extra-links {
  margin-top: 1.5rem;
  text-align: center;
}
.extra-links a {
  color: hsla(160, 100%, 37%, 1);
  text-decoration: none;
}
.extra-links a:hover {
  text-decoration: underline;
}
</style>