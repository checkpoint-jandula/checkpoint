<template>
  <div class="user-profile-view">
    <div v-if="isLoadingProfile" class="loading-message">Cargando perfil del usuario...</div>
    <div v-if="profileError" class="error-message">{{ profileError }}</div>

    <div v-if="viewedUser" class="profile-content-wrapper">
      <header class="profile-header section-block">
        <img 
          :src="profilePictureUrl" 
          alt="Foto de perfil" 
          class="profile-avatar"
          @error="onAvatarError"
        />
        <div class="profile-info">
          <h1>{{ viewedUser.nombre_usuario }}</h1>
          <p class="registration-date">Miembro desde: {{ formatReadableDate(viewedUser.fecha_registro) }}</p>
          
          <div v-if="!isOwnProfile" class="friend-actions">
            <button v-if="friendshipStatus === 'NONE'" @click="handleSendFriendRequest" :disabled="isLoadingFriendAction" class="action-button primary">
              {{ isLoadingFriendAction ? 'Enviando...' : 'Añadir Amigo' }}
            </button>

            <button v-if="friendshipStatus === 'SENT'" @click="handleDeclineOrCancelRequest" :disabled="isLoadingFriendAction" class="action-button secondary">
              {{ isLoadingFriendAction ? 'Cancelando...' : 'Cancelar Solicitud' }}
            </button>
            
            <div v-if="friendshipStatus === 'RECEIVED'" class="friend-actions-received">
              <span>{{ viewedUser.nombre_usuario }} te ha enviado una solicitud.</span>
              <button @click="handleAcceptFriendRequest" :disabled="isLoadingFriendAction" class="action-button primary small">Aceptar</button>
              <button @click="handleDeclineOrCancelRequest" :disabled="isLoadingFriendAction" class="action-button danger small">Rechazar</button>
            </div>

            <div v-if="friendshipStatus === 'FRIENDS'" class="friend-actions-friends">
              <span><i class="icon-friends"></i> Amigos</span>
              <button @click="handleRemoveFriend" :disabled="isLoadingFriendAction" class="action-button danger small">Eliminar</button>
            </div>

            <p v-if="friendActionError" class="action-feedback error">{{ friendActionError }}</p>
            <p v-if="friendActionSuccess" class="action-feedback success">{{ friendActionSuccess }}</p>
          </div>
        </div>
        <div v-if="isOwnProfile" class="profile-actions">
          <RouterLink :to="{ name: 'settings' }" class="action-button secondary">
            Ajustes de Mi Perfil
          </RouterLink>
        </div>
      </header>

      <nav class="tabs-navigation profile-tabs">
        <button @click="setActiveTab('library')" :class="{ 'active-tab': activeTab === 'library' }"
                v-if="canViewLibrary">
          Biblioteca
        </button>
        <button @click="setActiveTab('my-gamelists')" :class="{ 'active-tab': activeTab === 'my-gamelists' }"
                v-if="isOwnProfile">
          Mis Listas de Juegos
        </button>
        <button @click="setActiveTab('my-tierlists')" :class="{ 'active-tab': activeTab === 'my-tierlists' }"
                v-if="isOwnProfile">
          Mis Tier Lists
        </button>
      </nav>

      <div class="tab-content">
        <div v-if="activeTab === 'library'">
          <template v-if="isOwnProfile">
            <MyLibraryView />
          </template>
          <template v-else-if="canViewLibrary">
            <div class="section-block">
              <h2>Biblioteca de {{ viewedUser.nombre_usuario }}</h2>
              <div v-if="isLoadingPublicLibrary" class="loading-message">Cargando biblioteca...</div>
              <div v-else-if="publicLibraryError" class="error-message">{{ publicLibraryError }}</div>
              <div v-else-if="publicLibrary.length === 0" class="empty-library-message">
                La biblioteca de este usuario está vacía.
              </div>
              <div class="library-grid" v-else>
                <div v-for="gameEntry in publicLibrary" :key="gameEntry.game_igdb_id" class="library-game-card">
                  <RouterLink :to="{ name: 'game-details', params: { igdbId: gameEntry.game_igdb_id } }">
                    <img 
                      :src="getCoverUrl(gameEntry.game_cover)" 
                      :alt="`Carátula de ${gameEntry.game_name || 'Juego'}`" 
                      class="library-game-cover" 
                      @error="onLibraryCoverError" 
                    />
                    <div class="card-content">
                      <h3 class="game-title">{{ gameEntry.game_name || `Juego ID: ${gameEntry.game_igdb_id}` }}</h3>
                      <div class="user-game-info">
                        <div v-if="gameEntry.status" class="info-item">
                          <strong>Estado:</strong> {{ formatUserGameStatus(gameEntry.status) }}
                        </div>
                        <div v-if="gameEntry.score !== null && gameEntry.score !== undefined" class="info-item">
                          <strong>Puntuación:</strong> {{ gameEntry.score }}/10 
                        </div>
                      </div>
                    </div>
                  </RouterLink>
                </div>
              </div>
            </div>
          </template>
          <template v-else>
              <div class="section-block">
                  <p class="add-to-library-prompt">La biblioteca de este usuario es privada.</p>
              </div>
          </template>
        </div>
        <div v-if="activeTab === 'my-gamelists' && isOwnProfile" class="tab-pane">
          <MyGameListsView />
        </div>
        <div v-if="activeTab === 'my-tierlists' && isOwnProfile" class="tab-pane">
          <MyTierListsView/>
        </div>
      </div>
    </div>
    <div v-if="!isLoadingProfile && !viewedUser && !profileError" class="no-results-message">
      Perfil no encontrado.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, RouterLink } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { 
  getUserByPublicId, 
  getPublicUserLibrary, 
  getMyFriends, 
  getPendingRequestsSent, 
  getPendingRequestsReceived,
  sendFriendRequest,
  acceptFriendRequest,
  declineOrCancelFriendRequest,
  removeFriend
} from '@/services/apiInstances';
import { BASE_PATH } from '@/api-client/base';
import defaultAvatar from '@/assets/img/default-avatar.png';
import defaultLibraryCover from '@/assets/img/default-game-cover.svg';

import MyLibraryView from '../MyLibraryView/MyLibraryView.vue';
import MyGameListsView from '../MyGameListsView/MyGameListsView.vue';
import MyTierListsView from '../MyTierListsView/MyTierListsView.vue';

const route = useRoute();
const authStore = useAuthStore();

const viewedUser = ref(null);
const isLoadingProfile = ref(true);
const profileError = ref('');
const activeTab = ref('library');

const publicLibrary = ref([]);
const isLoadingPublicLibrary = ref(false);
const publicLibraryError = ref('');

// Estado para la lógica de amistad
const friendsList = ref([]);
const sentRequests = ref([]);
const receivedRequests = ref([]);
const isLoadingFriendAction = ref(false);
const friendActionError = ref('');
const friendActionSuccess = ref('');

const fetchUserProfile = async (publicId) => {
  // ... (código existente de fetchUserProfile sin cambios)
  if (!publicId) {
    profileError.value = "No se ha especificado un ID de perfil.";
    isLoadingProfile.value = false;
    return;
  }
  isLoadingProfile.value = true;
  profileError.value = '';
  viewedUser.value = null;
  publicLibrary.value = [];

  try {
    const response = await getUserByPublicId(publicId);
    viewedUser.value = response.data;
    
    if (canViewLibrary.value && !isOwnProfile.value) {
      fetchPublicLibrary(publicId);
    }
  } catch (err) {
    console.error("Error fetching user profile:", err);
    if (err.response && err.response.status === 404) {
      profileError.value = "Perfil de usuario no encontrado.";
    } else {
      profileError.value = "Error al cargar el perfil del usuario.";
    }
    viewedUser.value = null;
  } finally {
    isLoadingProfile.value = false;
  }
};

const fetchPublicLibrary = async (publicId) => {
  // ... (código existente de fetchPublicLibrary sin cambios)
  isLoadingPublicLibrary.value = true;
  publicLibraryError.value = '';
  try {
    const response = await getPublicUserLibrary(publicId);
    publicLibrary.value = response.data;
  } catch (error) {
    console.error("Error fetching public library:", error);
    if (error.response && (error.response.status === 403 || error.response.status === 401)) {
        publicLibraryError.value = "La biblioteca de este usuario es privada o solo para amigos.";
    } else {
        publicLibraryError.value = "No se pudo cargar la biblioteca de este usuario.";
    }
  } finally {
    isLoadingPublicLibrary.value = false;
  }
};

const fetchAllFriendshipData = async () => {
  if (!authStore.isAuthenticated) return;
  
  // Limpiar datos antiguos antes de volver a cargar
  friendsList.value = [];
  sentRequests.value = [];
  receivedRequests.value = [];

  try {
    const [friends, sent, received] = await Promise.all([
      getMyFriends(),
      getPendingRequestsSent(),
      getPendingRequestsReceived()
    ]);
    friendsList.value = friends.data;
    sentRequests.value = sent.data;
    receivedRequests.value = received.data;
  } catch (error) {
    console.error("Error cargando datos de amistad:", error);
    friendActionError.value = "No se pudo cargar la información de amigos.";
  }
};

const isOwnProfile = computed(() => {
  return authStore.isAuthenticated && viewedUser.value && authStore.currentUser?.public_id === viewedUser.value.public_id;
});

const areFriends = computed(() => {
  if (!viewedUser.value || !friendsList.value) return false;
  return friendsList.value.some(friend => friend.user_public_id === viewedUser.value.public_id);
});

const friendshipStatus = computed(() => {
  if (!viewedUser.value || !authStore.isAuthenticated || isOwnProfile.value) return null;
  if (areFriends.value) return 'FRIENDS';
  if (sentRequests.value.some(req => req.user_public_id === viewedUser.value.public_id)) return 'SENT';
  if (receivedRequests.value.some(req => req.user_public_id === viewedUser.value.public_id)) return 'RECEIVED';
  return 'NONE';
});

// --- Handlers para acciones de amistad ---

const handleFriendAction = async (action) => {
  isLoadingFriendAction.value = true;
  friendActionError.value = '';
  friendActionSuccess.value = '';
  try {
    await action();
    // Refrescar todos los datos de amistad para actualizar el estado de los botones
    await fetchAllFriendshipData(); 
  } catch(error) {
    console.error("Error en la acción de amistad:", error);
    friendActionError.value = error.response?.data?.error || error.response?.data?.message || 'Ocurrió un error.';
  } finally {
    isLoadingFriendAction.value = false;
  }
};

const handleSendFriendRequest = () => handleFriendAction(() => sendFriendRequest(viewedUser.value.public_id));
const handleAcceptFriendRequest = () => handleFriendAction(() => acceptFriendRequest(viewedUser.value.public_id));
const handleDeclineOrCancelRequest = () => handleFriendAction(() => declineOrCancelFriendRequest(viewedUser.value.public_id));
const handleRemoveFriend = () => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar a ${viewedUser.value.nombre_usuario} de tus amigos?`)) {
        handleFriendAction(() => removeFriend(viewedUser.value.public_id));
    }
};

// ... (resto de funciones computadas y helpers sin cambios) ...

onMounted(() => {
  fetchUserProfile(route.params.publicId);
  fetchAllFriendshipData();
});

watch(() => route.params.publicId, (newId) => {
  if (newId) {
    activeTab.value = 'library'; 
    fetchUserProfile(newId);
    // No es necesario recargar la lista de amigos aquí, ya que no depende del perfil visitado
  }
});

// ... (resto de funciones de formato, etc.) ...
const canViewLibrary = computed(() => {
    if (!viewedUser.value) return false;
    if (isOwnProfile.value) return true;
    if (viewedUser.value.visibilidad_perfil === 'PUBLICO') return true;
    return viewedUser.value.visibilidad_perfil === 'SOLO_AMIGOS' && areFriends.value;
});

const profilePictureUrl = computed(() => {
  const fotoPerfilValue = viewedUser.value?.foto_perfil;
  if (fotoPerfilValue) {
    if (fotoPerfilValue.startsWith('http')) {
      return fotoPerfilValue;
    }
    const baseApiUrl = BASE_PATH.endsWith('/') ? BASE_PATH.slice(0, -1) : BASE_PATH;
    const relativeImagePath = fotoPerfilValue.startsWith('/') ? fotoPerfilValue.substring(1) : fotoPerfilValue;
    const url = `${baseApiUrl}/profile-pictures/${relativeImagePath}`;
    // Añadir el cache buster solo si es el perfil del usuario actual
    return isOwnProfile.value ? `${url}?t=${authStore.imageUpdateTrigger}` : url;
  }
  return defaultAvatar;
});

const onAvatarError = (event) => {
  event.target.src = defaultAvatar;
};

const getCoverUrl = (coverData) => { 
  if (coverData && typeof coverData.url === 'string' && coverData.url.trim() !== '') { 
    let imageUrl = coverData.url;
    if (imageUrl.startsWith('//')) { 
      imageUrl = `https:${imageUrl}`;
    }
    if (imageUrl.includes('/t_thumb/')) {
      imageUrl = imageUrl.replace('/t_thumb/', '/t_cover_big/');
    } else if (imageUrl.includes('/t_cover_small/')) {
      imageUrl = imageUrl.replace('/t_cover_small/', '/t_cover_big/'); 
    }
    return imageUrl;
  }
  return defaultLibraryCover; 
};

const onLibraryCoverError = (event) => {
  event.target.src = defaultLibraryCover;
};

const formatReadableDate = (isoDateString) => {
  if (!isoDateString) return '';
  return new Date(isoDateString).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });
};

const formatUserGameStatus = (status) => { 
  if (!status) return 'No especificado';
  const statusMap = {
    'COMPLETED': 'Completado',
    'COMPLETED_MAIN_STORY': 'Historia Principal Completada',
    'COMPLETED_MAIN_AND_SIDES': 'Principal + Secundarias',
    'COMPLETED_100_PERCENT': 'Completado al 100%',
    'ARCHIVED': 'Archivado',
    'ARCHIVED_ABANDONED': 'Abandonado',
    'ARCHIVED_NOT_PLAYING': 'Archivado (Sin Jugar)',
    'WISHLIST': 'En Lista de Deseos',
    'PLAYING': 'Jugando',
    'PLAYING_PAUSED': 'En Pausa',
    'PLAYING_ENDLESS': 'Jugando (Sin Fin)'
  };
  return statusMap[status] || status; 
};

const setActiveTab = (tabName) => {
  activeTab.value = tabName;
};
</script>

<style src="./UserProfileView.css" scoped>

</style>