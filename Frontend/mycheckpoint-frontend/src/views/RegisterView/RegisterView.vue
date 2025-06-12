<template>
  <div class="register-container">
    
    <div class="register-content">
      <h2>Crear Cuenta</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">Nombre de Usuario:</label>
          <input type="text" id="username" v-model="username" required />
        </div>
        <div class="form-group">
          <label for="email">Email:</label>
          <input type="email" id="email" v-model="email" required />
        </div>
        <div class="form-group">
          <label for="password">Contraseña:</label>
          <input type="password" id="password" v-model="password" required />
          <small v-if="password && password.length > 0 && password.length < 8" class="password-hint">
            La contraseña debe tener al menos 8 caracteres.
          </small>
        </div>

        <button type="submit" :disabled="isLoading">
          {{ isLoading ? 'Registrando...' : 'Registrar' }}
        </button>
      </form>

      <div v-if="errorMessages.length > 0" class="error-messages">
        <h4>Error al registrar:</h4>
        <ul>
          <li v-for="(error, index) in errorMessages" :key="index">{{ error }}</li>
        </ul>
      </div>

      <div v-if="successMessage" class="success-message">
        {{ successMessage }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { registerUser } from '@/services/apiInstances.js'; // Importamos la función para registrar
// El UserCreateDTO se define por las propiedades que enviamos,
// no es necesario importarlo explícitamente aquí a menos que sea para tipado estricto con TS.

const username = ref('');
const email = ref('');
const password = ref('');
const errorMessages = ref([]);
const successMessage = ref('');
const isLoading = ref(false);

const router = useRouter();

const handleRegister = async () => {
  errorMessages.value = [];
  successMessage.value = '';
  isLoading.value = true;

  if (password.value.length < 8) {
    errorMessages.value = ['La contraseña debe tener al menos 8 caracteres.'];
    isLoading.value = false;
    return;
  }
  if (username.value.length < 3) {
    errorMessages.value = ['El nombre de usuario debe tener al menos 3 caracteres.'];
    isLoading.value = false;
    return;
  }

  /** @type {import('@/api-client/index.js').UserCreateDTO} */
  const userCreateData = { //
    nombre_usuario: username.value, //
    email: email.value, //
    contraseña: password.value, //
  };

  try {
    const response = await registerUser(userCreateData); //
    // Según UsuariosApi.md, un 201 devuelve UserDTO
    successMessage.value = `¡Registro exitoso para ${response.data.nombre_usuario}! Se ha enviado un correo de confirmación a ${response.data.email}. Por favor, verifica tu cuenta.`;
    // Opcionalmente, redirigir o limpiar formulario
    username.value = '';
    email.value = '';
    password.value = '';
    // Considera redirigir al login o a una página de "revisa tu email"
    // setTimeout(() => router.push('/login'), 5000); // Ejemplo de redirección tras 5 segundos
  } catch (error) {
    if (error.response) {
      // El servidor respondió con un código de estado fuera del rango 2xx
      console.error("Datos del error:", error.response.data);
      console.error("Estado del error:", error.response.status);

      if (error.response.status === 400) { //
        // Error de validación desde el backend
        if (error.response.data && error.response.data.errors) { //
          errorMessages.value = error.response.data.errors;
        } else {
          errorMessages.value = ['Los datos proporcionados no son válidos. Por favor, revisa los campos.'];
        }
      } else if (error.response.status === 409) { //
        // Conflicto (email o nombre de usuario ya existen)
        if (error.response.data && error.response.data.message) { //
          errorMessages.value = [error.response.data.message];
        } else {
          errorMessages.value = ['El nombre de usuario o el email ya están registrados.'];
        }
      } else {
        // Otros errores del servidor
        errorMessages.value = [`Error del servidor (${error.response.status}): ${error.response.data.error || 'Ocurrió un problema.'}`];
      }
    } else if (error.request) {
      // La solicitud se hizo pero no se recibió respuesta
      console.error("Error de red:", error.request);
      errorMessages.value = ['No se pudo conectar con el servidor. Por favor, inténtalo más tarde.'];
    } else {
      // Algo sucedió al configurar la solicitud que provocó un error
      console.error('Error de configuración:', error.message);
      errorMessages.value = [`Error: ${error.message}`];
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style src="./RegisterView.css" scoped></style>