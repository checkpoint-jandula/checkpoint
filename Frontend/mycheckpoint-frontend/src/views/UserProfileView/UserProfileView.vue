<template>
  <div class="user-profile-view">
    <div v-if="isLoadingProfile" class="loading-message">Cargando perfil del usuario...</div>
    <div v-if="profileError" class="error-message">{{ profileError }}</div>

    <div v-if="viewedUser" class="profile-content">
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
          <p class="profile-meta">Tema: {{ viewedUser.tema }} | Notificaciones: {{ viewedUser.notificaciones ? 'Activadas' : 'Desactivadas' }}</p>
          <p class="profile-meta">Visibilidad: {{ formatProfileVisibility(viewedUser.visibilidad_perfil) }}</p>
        </div>
        <div v-if="isOwnProfile && route.name !== 'settings'" class="profile-actions">
          <RouterLink :to="{ name: 'settings' }" class="action-button secondary">
            Ajustes de Mi Perfil
          </RouterLink>
        </div>
      </header>

    <nav class="tabs-navigation profile-tabs">
      <button @click="setActiveTab('library')" :class="{ 'active-tab': activeTab === 'library' }" 
              v-if="isOwnProfile || (viewedUser && (viewedUser.visibilidad_perfil === 'PUBLICO' || (viewedUser.visibilidad_perfil === 'SOLO_AMIGOS' /* && sonAmigosLogica */)))">
        Biblioteca
      </button>
      <button @click="setActiveTab('my-gamelists')" :class="{ 'active-tab': activeTab === 'my-gamelists' }"
              v-if="isOwnProfile">
        Mis Listas de Juegos
      </button>
      </nav>

      <div class="tab-content">
        <div v-if="activeTab === 'library'">
          <template v-if="isOwnProfile">
            <MyLibraryView />
          </template>
          <template v-else>
            <div class="section-block" v-if="viewedUser.visibilidad_perfil === 'PUBLICO' || (viewedUser.visibilidad_perfil === 'SOLO_AMIGOS' /* && sonAmigosLogica */)">
              <p>Viendo la biblioteca de {{ viewedUser.nombre_usuario }}. (Funcionalidad de biblioteca pública de otros usuarios pendiente)</p>
              </div>
            <div class="section-block" v-else>
                <p>La biblioteca de este usuario no es pública.</p>
            </div>
          </template>
        </div>

      <div v-if="activeTab === 'my-gamelists' && isOwnProfile" class="tab-pane">
        <MyGameListsView />
      </div>

        <div v-if="activeTab === 'tierlists'" class="tab-pane">
          <p>Tier Lists de {{ viewedUser.nombre_usuario }} (Pendiente).</p>
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
import { getUserByPublicId } from '@/services/apiInstances'; // Asume que existe
import { BASE_PATH } from '@/api-client/base';
import defaultAvatar from '@/assets/img/default-avatar.png'; // Placeholder para avatar

// Importar el componente de la biblioteca del usuario
import MyLibraryView from '../MyLibraryView/MyLibraryView.vue'; // Ajusta la ruta si es necesario
import MyGameListsView from '../MyGameListsView/MyGameListsView.vue';

const route = useRoute();
const authStore = useAuthStore();

const viewedUser = ref(null); // UserDTO del perfil que se está viendo
const isLoadingProfile = ref(true);
const profileError = ref('');
const activeTab = ref('library'); // Pestaña por defecto

const fetchUserProfile = async (publicId) => {
  if (!publicId) {
    profileError.value = "No se ha especificado un ID de perfil.";
    isLoadingProfile.value = false;
    return;
  }
  isLoadingProfile.value = true;
  profileError.value = '';
  try {
    const response = await getUserByPublicId(publicId); //
    viewedUser.value = response.data;
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

const isOwnProfile = computed(() => {
  return authStore.isAuthenticated && viewedUser.value && authStore.currentUser?.public_id === viewedUser.value.public_id;
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

const formatReadableDate = (isoDateString) => {
  if (!isoDateString) return '';
  return new Date(isoDateString).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });
};

const formatProfileVisibility = (visibility) => {
  const map = { 'PUBLICO': 'Público', 'PRIVADO': 'Privado', 'SOLO_AMIGOS': 'Solo Amigos' };
  return map[visibility] || visibility;
};

const setActiveTab = (tabName) => {
  activeTab.value = tabName;
};

onMounted(() => {
  fetchUserProfile(route.params.publicId);
});

watch(() => route.params.publicId, (newId) => {
  if (newId) {
    fetchUserProfile(newId);
    activeTab.value = 'library'; // Reset tab al cambiar de perfil
  }
});
</script>

<style src="./UserProfileView.css" scoped>

</style>