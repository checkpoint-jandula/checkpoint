<template>
  <div class="user-settings-container">
    <h2>Ajustes del Perfil</h2>

    <section class="settings-section">
      <h3>Actualizar Información del Perfil</h3>
      <form @submit.prevent="handleUpdateProfile">
        <div class="form-group">
          <label for="username">Nombre de Usuario:</label>
          <input type="text" id="username" v-model="profileForm.nombre_usuario" />
          <small v-if="profileForm.nombre_usuario && profileForm.nombre_usuario.length > 0 && profileForm.nombre_usuario.length < 3" class="input-hint">
            Debe tener al menos 3 caracteres.
          </small>
        </div>
        <div class="form-group">
          <label for="theme">Tema Preferido:</label>
          <select id="theme" v-model="profileForm.tema">
            <option value="CLARO">Claro</option>
            <option value="OSCURO">Oscuro</option>
          </select>
        </div>
        <div class="form-group">
          <label for="notifications">Recibir Notificaciones:</label>
          <input type="checkbox" id="notifications" v-model="profileForm.notificaciones" />
        </div>
        <div class="form-group">
          <label for="profileVisibility">Visibilidad del Perfil:</label>
          <select id="profileVisibility" v-model="profileForm.visibilidad_perfil">
            <option value="PUBLICO">Público</option>
            <option value="PRIVADO">Privado</option>
            <option value="SOLO_AMIGOS">Solo Amigos</option>
          </select>
        </div>
        <button type="submit" :disabled="isLoadingProfile">
          {{ isLoadingProfile ? 'Guardando...' : 'Guardar Cambios de Perfil' }}
        </button>
        <div v-if="profileMessage" :class="profileError ? 'error-message' : 'success-message'">
          {{ profileMessage }}
        </div>
      </form>
    </section>

    <section class="settings-section">
      <h3>Foto de Perfil</h3>
      <div class="profile-picture-area">
        <p>Foto Actual:</p>
        <img :src="currentProfilePictureUrl" alt="Foto de perfil actual" class="profile-picture-current" />
        <div class="form-group">
          <label for="profilePictureInput">Seleccionar nueva foto:</label>
          <input type="file" id="profilePictureInput" @change="onFileSelected" accept="image/png, image/jpeg, image/gif" />
        </div>
        <div v-if="imagePreviewUrl" class="image-preview-container">
          <p>Vista Previa:</p>
          <img :src="imagePreviewUrl" alt="Vista previa de nueva foto" class="profile-picture-preview" />
        </div>
        <button @click="handleUploadPhoto" :disabled="!selectedFile || isLoadingPhoto" class="upload-button">
          {{ isLoadingPhoto ? 'Subiendo...' : 'Subir Nueva Foto' }}
        </button>
        <div v-if="photoMessage" :class="photoError ? 'error-message' : 'success-message'">
          {{ photoMessage }}
        </div>
      </div>
    </section>

    <section class="settings-section">
      <h3>Cambiar Contraseña</h3>
      <form @submit.prevent="handleChangePassword">
         <div class="form-group">
          <label for="currentPassword">Contraseña Actual:</label>
          <input type="password" id="currentPassword" v-model="passwordChangeForm.currentPassword" required />
        </div>
        <div class="form-group">
          <label for="newPasswordForChange">Nueva Contraseña:</label>
          <input type="password" id="newPasswordForChange" v-model="passwordChangeForm.newPassword" required />
          <small v-if="passwordChangeForm.newPassword && passwordChangeForm.newPassword.length > 0 && passwordChangeForm.newPassword.length < 8" class="input-hint">
            Debe tener al menos 8 caracteres.
          </small>
        </div>
        <div class="form-group">
          <label for="confirmNewPassword">Confirmar Nueva Contraseña:</label>
          <input type="password" id="confirmNewPassword" v-model="passwordChangeForm.confirmNewPassword" required />
        </div>
        <div v-if="passwordChangeForm.newPassword && passwordChangeForm.confirmNewPassword && passwordChangeForm.newPassword !== passwordChangeForm.confirmNewPassword" class="error-message-inline">
          Las nuevas contraseñas no coinciden.
        </div>
        <button type="submit" :disabled="isLoadingChangePassword || passwordChangeForm.newPassword !== passwordChangeForm.confirmNewPassword || passwordChangeForm.newPassword.length < 8">
          {{ isLoadingChangePassword ? 'Cambiando...' : 'Cambiar Contraseña' }}
        </button>
        <div v-if="changePasswordMessage" :class="changePasswordError ? 'error-message' : 'success-message'">
          {{ changePasswordMessage }}
        </div>
      </form>
    </section>

    <section class="settings-section delete-account-section">
      <h3>Eliminar Cuenta (Zona Peligrosa)</h3>
      <p>
        Esta acción programará la eliminación permanente de tu cuenta después de un período de gracia.
        Perderás acceso a todos tus datos. Esta acción no se puede deshacer.
      </p>
      <button @click="requestAccountDeletion" v-if="!showDeleteConfirmation" class="delete-button-request">
        Solicitar Eliminación de Cuenta
      </button>

      <div v-if="showDeleteConfirmation" class="delete-confirmation-area">
        <p>Para confirmar la eliminación de tu cuenta, por favor ingresa tu contraseña actual.</p>
        <div class="form-group">
          <label for="passwordForDelete">Contraseña Actual:</label>
          <input type="password" id="passwordForDelete" v-model="passwordForDelete" required />
        </div>
        <button @click="handleDeleteAccount" :disabled="isLoadingDeleteAccount || !passwordForDelete" class="delete-button-confirm">
          {{ isLoadingDeleteAccount ? 'Eliminando...' : 'Eliminar Mi Cuenta Permanentemente' }}
        </button>
      </div>
      <div v-if="deleteAccountMessage" :class="deleteAccountError ? 'error-message' : 'success-message'">
        {{ deleteAccountMessage }}
      </div>
    </section>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue'; // Añadido watch
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'vue-router';
import {
  updateUserProfile,
  getCurrentAuthenticatedUser,
  uploadUserProfilePicture,
  changeUserPassword,
  deleteUserAccount
} from '@/services/apiInstances';
import { BASE_PATH } from '@/api-client/base';
import defaultAvatar from '@/assets/default-avatar.png';

const authStore = useAuthStore();
const router = useRouter();

// --- Estados (sin cambios) ---
const profileForm = reactive({
  nombre_usuario: '',
  tema: 'CLARO',
  notificaciones: true,
  visibilidad_perfil: 'PUBLICO',
});
const isLoadingProfile = ref(false);
const profileMessage = ref('');
const profileError = ref(false);

const selectedFile = ref(null);
const imagePreviewUrl = ref(null);
const isLoadingPhoto = ref(false);
const photoMessage = ref('');
const photoError = ref(false);

const passwordChangeForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmNewPassword: '',
});
const isLoadingChangePassword = ref(false);
const changePasswordMessage = ref('');
const changePasswordError = ref(false);

const showDeleteConfirmation = ref(false);
const passwordForDelete = ref('');
const isLoadingDeleteAccount = ref(false);
const deleteAccountMessage = ref('');
const deleteAccountError = ref(false);

// --- Computed Property para URL de Foto de Perfil (CON CACHE BUSTER) ---
const currentProfilePictureUrl = computed(() => {
  const fotoPerfilValue = authStore.currentUser?.foto_perfil;
  console.log('Computed currentProfilePictureUrl: fotoPerfilValue del store es:', fotoPerfilValue); // LOG

  if (fotoPerfilValue) {
    let finalUrl = '';
    if (fotoPerfilValue.startsWith('http://') || fotoPerfilValue.startsWith('https://')) {
      finalUrl = fotoPerfilValue;
    } else {
      const baseApiUrl = BASE_PATH.endsWith('/') ? BASE_PATH.slice(0, -1) : BASE_PATH;
      const relativeImagePath = fotoPerfilValue.startsWith('/') ? fotoPerfilValue.substring(1) : fotoPerfilValue;
      finalUrl = `${baseApiUrl}/profile-pictures/${relativeImagePath}`;
    }
    // Añadir un parámetro 'timestamp' para evitar problemas de caché del navegador
    const cacheBuster = `?t=${new Date().getTime()}`;
    console.log('Computed currentProfilePictureUrl: URL final construida:', `${finalUrl}${cacheBuster}`); // LOG
    return `${finalUrl}${cacheBuster}`;
  }
  console.log('Computed currentProfilePictureUrl: Devolviendo defaultAvatar'); // LOG
  return defaultAvatar;
});

// --- Watcher para depurar cambios en currentProfilePictureUrl (OPCIONAL PARA DEPURAR) ---
watch(currentProfilePictureUrl, (newValue, oldValue) => {
  console.log(`currentProfilePictureUrl cambió de: ${oldValue} \na: ${newValue}`);
});
watch(() => authStore.currentUser?.foto_perfil, (newVal, oldVal) => {
    console.log(`authStore.currentUser.foto_perfil cambió de ${oldVal} a ${newVal}`);
});


// --- Cargar datos del usuario al montar (sin cambios) ---
onMounted(async () => {
  if (authStore.isAuthenticated && authStore.currentUser) {
    populateProfileForm(authStore.currentUser);
  } else if (authStore.isAuthenticated) {
    try {
      isLoadingProfile.value = true;
      const response = await getCurrentAuthenticatedUser();
      authStore.user = response.data; // Esto debería hacer reactivo a currentUser
      localStorage.setItem('user', JSON.stringify(response.data));
      populateProfileForm(response.data);
    } catch (error) {
      profileMessage.value = 'Error al cargar los datos del perfil.';
      profileError.value = true;
      console.error("Error cargando datos del usuario para ajustes:", error);
    } finally {
      isLoadingProfile.value = false;
    }
  }
});

// --- Función para poblar formulario de perfil (sin cambios) ---
const populateProfileForm = (userData) => {
  if (!userData) return;
  profileForm.nombre_usuario = userData.nombre_usuario || '';
  profileForm.tema = userData.tema || 'CLARO';
  profileForm.notificaciones = userData.notificaciones === undefined ? true : userData.notificaciones;
  profileForm.visibilidad_perfil = userData.visibilidad_perfil || 'PUBLICO';
};

// --- Lógica para Actualizar Perfil (sin cambios) ---
const handleUpdateProfile = async () => { /* ... tu lógica existente ... */ };

// --- Lógica para Foto de Perfil (con log añadido) ---
const onFileSelected = (event) => {
  // ... (tu lógica existente sin cambios) ...
  photoMessage.value = '';
  photoError.value = false;
  const file = event.target.files[0];
  if (file) {
    const allowedTypes = ['image/png', 'image/jpeg', 'image/gif'];
    if (!allowedTypes.includes(file.type)) {
      photoMessage.value = 'Tipo de archivo no permitido. Sube PNG, JPEG o GIF.';
      photoError.value = true;
      selectedFile.value = null;
      imagePreviewUrl.value = null;
      event.target.value = '';
      return;
    }
    selectedFile.value = file;
    imagePreviewUrl.value = URL.createObjectURL(file);
  } else {
    selectedFile.value = null;
    imagePreviewUrl.value = null;
  }
};

const handleUploadPhoto = async () => {
  if (!selectedFile.value) {
    photoMessage.value = 'Por favor, selecciona un archivo primero.';
    photoError.value = true;
    return;
  }

  photoMessage.value = '';
  photoError.value = false;
  isLoadingPhoto.value = true;

  try {
    const response = await uploadUserProfilePicture(selectedFile.value);
    
    console.log('handleUploadPhoto: Respuesta de API al subir foto:', response.data); // LOG
    
    // Punto clave: Actualizar el store. Pinia debería manejar la reactividad.
    authStore.user = response.data;
    localStorage.setItem('user', JSON.stringify(response.data));

    // Forzar la actualización del store o de un componente específico es usualmente un anti-patrón en Vue/Pinia
    // si la reactividad está bien configurada. El cambio en authStore.user debería ser suficiente.
    // authStore.$patch({ user: response.data }); // Otra forma de actualizar, pero la asignación directa debería funcionar.

    console.log('handleUploadPhoto: authStore.user actualizado. Nuevo foto_perfil:', authStore.currentUser?.foto_perfil); // LOG
    
    photoMessage.value = 'Foto de perfil actualizada correctamente.';
    photoError.value = false;
    selectedFile.value = null;
    imagePreviewUrl.value = null; // Limpiar la vista previa
    const fileInput = document.getElementById('profilePictureInput');
    if (fileInput) {
      fileInput.value = '';
    }

  } catch (error) {
    // ... (tu manejo de errores existente) ...
    photoError.value = true;
    if (error.response) {
      console.error("Error subiendo foto:", error.response);
      if (error.response.status === 400 && error.response.data.errors) {
        photoMessage.value = error.response.data.errors.join(', ');
      } else if (error.response.status === 413 && error.response.data.errors) {
        photoMessage.value = error.response.data.errors.join(', ');
      } else {
        photoMessage.value = error.response.data.error || error.response.data.message || 'Error al subir la foto.';
      }
    } else {
      photoMessage.value = 'Error de red al subir la foto.';
    }
  } finally {
    isLoadingPhoto.value = false;
  }
};

// --- Lógica para Cambiar Contraseña (sin cambios) ---
const handleChangePassword = async () => { /* ... tu lógica existente ... */ };

// --- Lógica para Eliminar Cuenta (sin cambios) ---
const requestAccountDeletion = () => { /* ... tu lógica existente ... */ };
const handleDeleteAccount = async () => { /* ... tu lógica existente ... */ };

</script>

<style scoped>
/* ... (estilos existentes) ... */
.user-settings-container {
  max-width: 600px;
  margin: 2rem auto;
  padding: 1rem;
}

.settings-section {
  margin-bottom: 2rem;
  padding: 1.5rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background-color: var(--color-background-soft);
}

.settings-section h3 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: var(--color-heading);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 0.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.form-group input[type="text"],
.form-group input[type="email"],
.form-group input[type="password"],
.form-group input[type="file"],
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  box-sizing: border-box;
  background-color: var(--color-background);
  color: var(--color-text);
}
.form-group input[type="file"] {
  padding: 0.3rem;
}


.form-group input[type="checkbox"] {
  margin-right: 0.5rem;
  vertical-align: middle;
}

.input-hint {
  font-size: 0.8rem;
  color: var(--vt-c-text-light-2);
  display: block;
  margin-top: 0.25rem;
}
.error-message-inline { /* Para el mensaje de contraseñas no coinciden */
  color: red;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}


button {
  padding: 0.75rem 1.5rem;
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
  margin-top: 0.5rem; 
}

button:hover {
  background-color: hsla(160, 100%, 30%, 1);
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
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
  background-color: rgba(0, 128, 0, 0.1);
  border: 1px solid rgba(0, 128, 0, 0.3);
  color: green;
}

.profile-picture-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}
.profile-picture-current,
.profile-picture-preview {
  max-width: 150px;
  max-height: 150px;
  border-radius: 50%; 
  object-fit: cover;
  border: 2px solid var(--color-border);
}
.image-preview-container {
  text-align: center;
}
.upload-button {
  width: auto; 
}
.delete-account-section {
  border-color: red; /* Destacar la zona peligrosa */
  background-color: rgba(255,0,0,0.05);
}

.delete-account-section h3 {
  color: red;
}

.delete-account-section p {
  margin-bottom: 1rem;
}

.delete-button-request,
.delete-button-confirm {
  background-color: red;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.delete-button-request:hover,
.delete-button-confirm:hover {
  background-color: darkred;
}

.delete-button-confirm:disabled {
  background-color: #ff8080; /* Un rojo más claro para deshabilitado */
  cursor: not-allowed;
}

.delete-confirmation-area {
  margin-top: 1rem;
  padding: 1rem;
  border: 1px dashed red;
  border-radius: 4px;
}
</style>