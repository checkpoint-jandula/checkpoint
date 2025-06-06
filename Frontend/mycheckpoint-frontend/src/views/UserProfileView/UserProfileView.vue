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
// MODIFICADO: Añadir getMyFriends a las importaciones
import { getUserByPublicId, getPublicUserLibrary, getMyFriends } from '@/services/apiInstances';
import { BASE_PATH } from '@/api-client/base';
import defaultAvatar from '@/assets/img/default-avatar.png';
import defaultLibraryCover from '@/assets/img/default-game-cover.png';

// Componentes para las pestañas del perfil propio
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

// NUEVO: Estado para la lista de amigos del usuario actual
const friendsList = ref([]);
const isLoadingFriends = ref(false);

const fetchUserProfile = async (publicId) => {
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

// NUEVO: Función para obtener la lista de amigos
const fetchFriendsList = async () => {
  if (!authStore.isAuthenticated) return;
  isLoadingFriends.value = true;
  try {
    const response = await getMyFriends();
    friendsList.value = response.data;
  } catch (error) {
    console.error("Error cargando la lista de amigos:", error);
    // No mostraremos un error visible, simplemente la lista estará vacía
  } finally {
    isLoadingFriends.value = false;
  }
};

const isOwnProfile = computed(() => {
  return authStore.isAuthenticated && viewedUser.value && authStore.currentUser?.public_id === viewedUser.value.public_id;
});

// NUEVO: Propiedad computada para saber si son amigos
const areFriends = computed(() => {
  if (!viewedUser.value || !friendsList.value || friendsList.value.length === 0) {
    return false;
  }
  // Comprueba si algún amigo en la lista tiene el mismo public_id que el usuario del perfil visitado
  return friendsList.value.some(friend => friend.user_public_id === viewedUser.value.public_id);
});

// MODIFICADO: Actualizar canViewLibrary para incluir la lógica de 'SOLO_AMIGOS'
const canViewLibrary = computed(() => {
    if (!viewedUser.value) return false;
    if (isOwnProfile.value) return true;
    if (viewedUser.value.visibilidad_perfil === 'PUBLICO') return true;
    // Si la visibilidad es 'SOLO_AMIGOS', se permite ver si 'areFriends' es verdadero
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
    return `${baseApiUrl}/profile-pictures/${relativeImagePath}`;
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

onMounted(() => {
  fetchUserProfile(route.params.publicId);
  // NUEVO: Cargar la lista de amigos cuando el componente se monta
  fetchFriendsList();
});

watch(() => route.params.publicId, (newId) => {
  if (newId) {
    activeTab.value = 'library'; 
    fetchUserProfile(newId);
  }
});
</script>

<style src="./UserProfileView.css" scoped>

</style>