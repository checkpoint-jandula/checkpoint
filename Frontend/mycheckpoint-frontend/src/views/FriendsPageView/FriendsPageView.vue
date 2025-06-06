<template>
  <div class="friends-page-view">
    <h1>Amigos y Solicitudes</h1>

    <div class="tabs-navigation">
      <button @click="activeTab = 'friends'" :class="{ 'active-tab': activeTab === 'friends' }">
        Mis Amigos ({{ friends.length }})
      </button>
      <button @click="activeTab = 'received'" :class="{ 'active-tab': activeTab === 'received' }">
        Solicitudes Recibidas ({{ receivedRequests.length }})
      </button>
      <button @click="activeTab = 'sent'" :class="{ 'active-tab': activeTab === 'sent' }">
        Solicitudes Enviadas ({{ sentRequests.length }})
      </button>
    </div>

    <div v-if="isLoading" class="loading-message">Cargando...</div>
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

    <div class="tab-content" v-if="!isLoading && !errorMessage">
      <div v-if="activeTab === 'friends'">
        <div v-if="friends.length === 0" class="empty-message">
          Aún no tienes amigos. ¡Busca usuarios y envía solicitudes!
        </div>
        <div v-else class="user-list">
          <div v-for="friendship in friends" :key="friendship.friendship_id" class="user-list-item">
            <RouterLink :to="{ name: 'profile', params: { publicId: friendship.user_public_id } }" class="user-info">
              <img :src="getProfilePictureUrl(friendship.profile_picture_url)" alt="Avatar" class="user-avatar-small">
              <span>{{ friendship.username }}</span>
            </RouterLink>
            <div class="user-actions">
              <button @click="handleRemoveFriend(friendship.user_public_id)" class="action-button danger small">Eliminar Amigo</button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="activeTab === 'received'">
        <div v-if="receivedRequests.length === 0" class="empty-message">
          No tienes solicitudes de amistad pendientes.
        </div>
        <div v-else class="user-list">
          <div v-for="request in receivedRequests" :key="request.friendship_id" class="user-list-item">
            <RouterLink :to="{ name: 'profile', params: { publicId: request.user_public_id } }" class="user-info">
              <img :src="getProfilePictureUrl(request.profile_picture_url)" alt="Avatar" class="user-avatar-small">
              <span>{{ request.username }}</span>
            </RouterLink>
            <div class="user-actions">
              <button @click="handleAcceptRequest(request.user_public_id)" class="action-button primary small">Aceptar</button>
              <button @click="handleDeclineRequest(request.user_public_id)" class="action-button secondary small">Rechazar</button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="activeTab === 'sent'">
        <div v-if="sentRequests.length === 0" class="empty-message">
          No tienes solicitudes de amistad enviadas pendientes.
        </div>
        <div v-else class="user-list">
          <div v-for="request in sentRequests" :key="request.friendship_id" class="user-list-item">
            <RouterLink :to="{ name: 'profile', params: { publicId: request.user_public_id } }" class="user-info">
              <img :src="getProfilePictureUrl(request.profile_picture_url)" alt="Avatar" class="user-avatar-small">
              <span>{{ request.username }}</span>
            </RouterLink>
            <div class="user-actions">
              <button @click="handleDeclineRequest(request.user_public_id)" class="action-button danger small">Cancelar Solicitud</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import { 
  getMyFriends, 
  getPendingRequestsSent, 
  getPendingRequestsReceived,
  acceptFriendRequest,
  declineOrCancelFriendRequest,
  removeFriend 
} from '@/services/apiInstances';
import { BASE_PATH } from '@/api-client/base';
import defaultAvatar from '@/assets/img/default-avatar.png';

const activeTab = ref('friends');
const friends = ref([]);
const sentRequests = ref([]);
const receivedRequests = ref([]);

const isLoading = ref(true);
const errorMessage = ref('');

const fetchData = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    // Usamos Promise.all para ejecutar todas las peticiones en paralelo
    const [friendsRes, sentRes, receivedRes] = await Promise.all([
      getMyFriends(),
      getPendingRequestsSent(),
      getPendingRequestsReceived()
    ]);
    friends.value = friendsRes.data;
    sentRequests.value = sentRes.data;
    receivedRequests.value = receivedRes.data;
  } catch (error) {
    console.error("Error cargando los datos de amistad:", error);
    errorMessage.value = "No se pudo cargar la información de amigos. Inténtalo de nuevo más tarde.";
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchData);

// --- Manejadores de acciones ---

const handleAction = async (action) => {
  try {
    await action();
    fetchData(); // Recargar todos los datos después de cualquier acción exitosa
  } catch (error) {
    console.error("Error realizando la acción de amistad:", error);
    errorMessage.value = error.response?.data?.error || "Ocurrió un error al realizar la acción.";
  }
};

const handleAcceptRequest = (userId) => handleAction(() => acceptFriendRequest(userId));
const handleDeclineRequest = (userId) => handleAction(() => declineOrCancelFriendRequest(userId));
const handleRemoveFriend = (userId) => {
  if (window.confirm("¿Estás seguro de que quieres eliminar a este amigo?")) {
    handleAction(() => removeFriend(userId));
  }
};

// --- Funciones de Utilidad ---

const getProfilePictureUrl = (fotoPerfil) => {
  if (fotoPerfil) {
    if (fotoPerfil.startsWith('http')) return fotoPerfil;
    const baseApiUrl = BASE_PATH.endsWith('/') ? BASE_PATH.slice(0, -1) : BASE_PATH;
    const relativeImagePath = fotoPerfil.startsWith('/') ? fotoPerfil.substring(1) : fotoPerfil;
    return `${baseApiUrl}/profile-pictures/${relativeImagePath}`;
  }
  return defaultAvatar;
};
</script>

<style scoped>
.friends-page-view {
  max-width: 800px;
  margin: 1rem auto;
  padding: 1rem;
}

h1 {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--color-heading);
}

.tabs-navigation {
  display: flex;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 1.5rem;
}

.tabs-navigation button {
  padding: 0.8rem 1.5rem;
  font-size: 1rem;
  font-weight: 500;
  border: none;
  border-bottom: 3px solid transparent;
  background-color: transparent;
  color: var(--color-text-light-2);
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s;
}

.tabs-navigation button:hover {
  color: var(--color-text);
}

.tabs-navigation button.active-tab {
  color: var(--color-heading);
  border-bottom-color: hsla(160, 100%, 37%, 1);
}

.loading-message, .error-message, .empty-message {
  text-align: center;
  padding: 2rem;
  margin-top: 1rem;
  font-size: 1.2rem;
  background-color: var(--color-background-soft);
  border-radius: 8px;
}

.error-message {
  color: #d8000c;
  background-color: #ffbaba;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.user-list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-list-item:hover {
    background-color: var(--color-background-mute);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  text-decoration: none;
  color: var(--color-text);
  font-weight: 500;
}

.user-avatar-small {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-actions {
  display: flex;
  gap: 0.5rem;
}

.action-button.small {
  padding: 0.4em 0.8em;
  font-size: 0.85rem;
  border-radius: 4px;
}

.action-button.primary {
    background-color: hsla(160, 100%, 37%, 1); color: white;
    border-color: hsla(160, 100%, 37%, 1);
}
.action-button.primary:hover:not(:disabled) { background-color: hsla(160, 100%, 30%, 1); }

.action-button.secondary {
    background-color: var(--color-background-mute); color: var(--color-text);
    border: 1px solid var(--color-border-hover);
}
.action-button.secondary:hover:not(:disabled) { background-color: var(--color-border); }

.action-button.danger {
    background-color: #d9534f; color: white;
    border-color: #d43f3a;
}
.action-button.danger:hover:not(:disabled) { background-color: #c9302c; }
</style>