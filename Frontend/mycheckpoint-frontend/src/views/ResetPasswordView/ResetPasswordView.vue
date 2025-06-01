<template>
  <div class="reset-password-container">
    <h2>Establecer Nueva Contraseña</h2>
    <form @submit.prevent="handleResetPassword" v-if="token">
      <div class="form-group">
        <label for="newPassword">Nueva Contraseña:</label>
        <input type="password" id="newPassword" v-model="newPassword" required />
         <small v-if="newPassword && newPassword.length > 0 && newPassword.length < 8" class="password-hint">
          La contraseña debe tener al menos 8 caracteres.
        </small>
      </div>
      <div class="form-group">
        <label for="confirmPassword">Confirmar Nueva Contraseña:</label>
        <input type="password" id="confirmPassword" v-model="confirmPassword" required />
      </div>

      <div v-if="passwordMismatch" class="error-message-inline">
        Las contraseñas no coinciden.
      </div>

      <button type="submit" :disabled="isLoading || passwordMismatch || newPassword.length < 8">
        {{ isLoading ? 'Guardando...' : 'Guardar Nueva Contraseña' }}
      </button>
    </form>
    <div v-if="!token && !message" class="error-message">
      Token de restablecimiento no encontrado o inválido.
    </div>
    <div v-if="message" :class="isError ? 'error-message' : 'success-message'">
      {{ message }}
    </div>
     <div v-if="!isError && message" class="extra-links">
      <router-link to="/login">Ir a Iniciar Sesión</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { resetPasswordWithToken } from '@/services/apiInstances.js'; // Importamos la función

const route = useRoute();
const router = useRouter();

const token = ref(null);
const newPassword = ref('');
const confirmPassword = ref('');
const message = ref('');
const isError = ref(false);
const isLoading = ref(false);

const passwordMismatch = computed(() => {
  return newPassword.value && confirmPassword.value && newPassword.value !== confirmPassword.value;
});

onMounted(() => {
  token.value = route.query.token || null;
  if (!token.value) {
    message.value = 'El enlace de restablecimiento es inválido o ha expirado. Por favor, solicita uno nuevo.';
    isError.value = true;
  }
});

const handleResetPassword = async () => {
  message.value = '';
  isError.value = false;
  isLoading.value = true;

  if (newPassword.value !== confirmPassword.value) {
    message.value = 'Las contraseñas no coinciden.';
    isError.value = true;
    isLoading.value = false;
    return;
  }

  if (newPassword.value.length < 8) {
    message.value = 'La nueva contraseña debe tener al menos 8 caracteres.';
    isError.value = true;
    isLoading.value = false;
    return;
  }

  /** @type {import('@/api-client/index.js').ResetPasswordDTO} */
  const resetData = { //
    token: token.value, //
    nueva_contraseña: newPassword.value, //
  };

  try {
    await resetPasswordWithToken(resetData); //
    // Respuesta 200 OK si es exitoso
    message.value = '¡Tu contraseña ha sido restablecida exitosamente! Ahora puedes iniciar sesión con tu nueva contraseña.';
    isError.value = false;
    // Opcional: redirigir al login después de un momento
    setTimeout(() => {
      router.push('/login');
    }, 3000);
  } catch (error) {
    if (error.response && error.response.data) {
      console.error("Datos del error:", error.response.data);
       // Para resetPassword, 400 puede ser token inválido/expirado, contraseña no cumple requisitos, etc.
       // 404 token no encontrado
      if (error.response.status === 400 || error.response.status === 404) {
        if (error.response.data.errors && Array.isArray(error.response.data.errors)) { //
          message.value = error.response.data.errors.join(' ');
        } else {
          message.value = error.response.data.error || error.response.data.message || 'El token es inválido, ha expirado o la nueva contraseña no cumple los requisitos.';
        }
      } else {
        message.value = error.response.data.error || 'Ocurrió un error al restablecer tu contraseña.';
      }
    } else if (error.request) {
      message.value = 'No se pudo conectar con el servidor. Inténtalo más tarde.';
    } else {
      message.value = 'Ocurrió un error inesperado.';
    }
    isError.value = true;
    console.error("Error al restablecer contraseña:", error);
  } finally {
    isLoading.value = false;
  }
};
</script>

<style src="./ResetPasswordView.css" scoped></style>