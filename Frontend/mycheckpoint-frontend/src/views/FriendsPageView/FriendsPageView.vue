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
import { ref, onMounted, onUnmounted } from 'vue';
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

let pollingInterval = null;

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

onMounted(() => {
  fetchData(); 

  // Inicia el polling: llama a fetchData cada 15 segundos
  pollingInterval = setInterval(fetchData, 5000); 
});

onUnmounted(() => {
  // Es crucial limpiar el intervalo cuando el usuario sale del componente
  // para evitar peticiones innecesarias y fugas de memoria.
  if (pollingInterval) {
    clearInterval(pollingInterval);
  }
});


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

<style src="./FriendsPageView.css" scoped> </style>