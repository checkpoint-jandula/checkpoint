<template>
  <div class="my-library-view">
    <div class="filters-panel section-block">
      <div class="filter-controls">
        <h3 class="filters-title">Filtrar Biblioteca</h3>
        <div class="filter-group">
          <label for="filter-status">Estado</label>
          <select id="filter-status" v-model="selectedStatus">
            <option v-for="opt in gameStatusOptions" :key="opt.value" :value="opt.value">
              {{ opt.text }}
            </option>
          </select>
        </div>
        <div class="filter-group">
          <label for="filter-platform">Plataforma</label>
          <select id="filter-platform" v-model="selectedPlatform">
            <option v-for="opt in personalPlatformOptions" :key="opt.value" :value="opt.value">
              {{ opt.text }}
            </option>
          </select>
        </div>
        <div class="filter-group">
          <label for="filter-score">Puntuación Mínima</label>
          <select id="filter-score" v-model.number="selectedMinScore">
             <option v-for="opt in scoreOptions" :key="opt.value" :value="opt.value">
              {{ opt.text }}
            </option>
          </select>
        </div>
        <div class="filter-actions">
          <button @click="resetFilters" class="button-reset">
            <span class="icon-reset">&times;</span> Limpiar
          </button>
        </div>
      </div>
    </div>
    <div v-if="isLoading" class="loading-message">Cargando mi biblioteca...</div>
    <div v-else-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    <div v-else-if="filteredLibraryGames.length === 0" class="empty-library-message">
      <span v-if="myLibraryGames.length === 0 && !selectedStatus && !selectedPlatform && selectedMinScore === null">
        Aún no tienes juegos en tu biblioteca. ¡Empieza a añadir algunos! 
      </span>
      <span v-else>
        No hay juegos que coincidan con tus filtros actuales.
      </span>
    </div>

    <div class="library-grid" v-if="!isLoading && filteredLibraryGames.length > 0">
      <div v-for="gameEntry in filteredLibraryGames" :key="gameEntry.game_igdb_id" class="library-game-card">
        <RouterLink :to="{ name: 'game-details', params: { igdbId: gameEntry.game_igdb_id } }" class="card-full-link">
          <div class="card-cover-container">
            <img 
              :src="getCoverUrl(gameEntry.game_cover, 't_cover_big')" 
              :alt="`Carátula de ${gameEntry.game_name || 'Juego'}`" 
              class="library-game-cover" 
              @error="onLibraryCoverError" 
            />
            <div v-if="gameEntry.score !== null && gameEntry.score !== undefined"
                 class="score-badge-on-cover" 
                 :class="getScoreColorClass(gameEntry.score)">
              {{ gameEntry.score }}
            </div>
            <div v-if="gameEntry.personal_platform" class="platform-logo-on-cover">
              <img :src="getPlatformLogoUrl(gameEntry.personal_platform)" :alt="gameEntry.personal_platform" />
            </div>
          </div>
          <div class="card-content">
            <h3 class="game-title">{{ gameEntry.game_name || `Juego ID: ${gameEntry.game_igdb_id}` }}</h3> 
          </div>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { RouterLink } from 'vue-router'; 
import { getMyUserGameLibrary } from '@/services/apiInstances';
import { useAuthStore } from '@/stores/authStore';
import defaultLibraryCover from '@/assets/img/default-game-cover.svg'; 

const authStore = useAuthStore();
const myLibraryGames = ref([]);
const isLoading = ref(true); 
const errorMessage = ref('');

// Estado para los Filtros
const selectedStatus = ref(null);
const selectedPlatform = ref(null);
const selectedMinScore = ref(null);

// Opciones para los selectores de filtro (sin cambios)
const gameStatusOptions = [
  { value: null, text: 'Todos los Estados' }, 
  { value: 'PLAYING', text: 'Jugando' }, 
  { value: 'PLAYING_PAUSED', text: 'Jugando (En Pausa)' }, 
  { value: 'PLAYING_ENDLESS', text: 'Jugando (Sin Fin)' }, 
  { value: 'COMPLETED_MAIN_STORY', text: 'Historia Principal Completada' }, 
  { value: 'COMPLETED_MAIN_AND_SIDES', text: 'Principal + Secundarias Completado' }, 
  { value: 'COMPLETED_100_PERCENT', text: 'Completado al 100%' }, 
  { value: 'WISHLIST', text: 'En Lista de Deseos' }, 
  { value: 'ARCHIVED_ABANDONED', text: 'Archivado (Abandonado)' }, 
  { value: 'ARCHIVED_NOT_PLAYING', text: 'Archivado (Sin Jugar)' }, 
  { value: 'ARCHIVED', text: 'Archivado (Otro)' },
];
const personalPlatformOptions = [
  { value: null, text: 'Todas las Plataformas' }, 
  { value: 'STEAM', text: 'Steam' }, 
  { value: 'EPIC_GAMES', text: 'Epic Games Store' }, 
  { value: 'GOG_GALAXY', text: 'GOG Galaxy' }, 
  { value: 'XBOX', text: 'Xbox' }, 
  { value: 'PLAYSTATION', text: 'PlayStation' }, 
  { value: 'NINTENDO', text: 'Nintendo' }, 
  { value: 'BATTLE_NET', text: 'Battle.net' }, 
  { value: 'EA_APP', text: 'EA App' }, 
  { value: 'UBISOFT_CONNECT', text: 'Ubisoft Connect' }, 
  { value: 'OTHER', text: 'Otra' },
];
const scoreOptions = [
    { value: null, text: 'Cualquier Puntuación' }, 
    { value: 0, text: 'Sin Puntuar' }, 
    { value: 1, text: '1+' }, 
    { value: 2, text: '2+' }, 
    { value: 3, text: '3+' }, 
    { value: 4, text: '4+' }, 
    { value: 5, text: '5+' }, 
    { value: 6, text: '6+' }, 
    { value: 7, text: '7+' }, 
    { value: 8, text: '8+' }, 
    { value: 9, text: '9+' }, 
    { value: 10, text: '10 (Perfecta)' }
];

// --- NUEVAS FUNCIONES HELPER ---

// Devuelve la clase de color para la cápsula de puntuación
const getScoreColorClass = (score) => {
  if (score >= 8) return 'score-high';
  if (score >= 5) return 'score-medium';
  if (score > 0) return 'score-low';
  return 'score-none'; // Para puntuaciones de 0 o nulas
};

// Devuelve la URL del logo de la plataforma
const getPlatformLogoUrl = (platform) => {
  if (!platform) return '';
  const platformName = platform.toLowerCase();
  try {
    // Esta sintaxis es necesaria para que Vite encuentre las imágenes dinámicamente
    return new URL(`/src/assets/logo-personal-platform/${platformName}.svg`, import.meta.url).href;
  } catch (error) {
    console.warn(`No se encontró el logo para la plataforma: ${platformName}.svg`);
    // Devuelve un logo por defecto si no se encuentra
    return new URL(`/src/assets/logo-personal-platform/other.svg`, import.meta.url).href;
  }
};

// --- LÓGICA EXISTENTE (sin cambios) ---

const fetchLibrary = async () => {
  isLoading.value = true;
  errorMessage.value = ''; 
  try {
    const response = await getMyUserGameLibrary();
    myLibraryGames.value = response.data;
  } catch (error) {
    console.error("Error cargando la biblioteca:", error);
    errorMessage.value = 'Error de red al cargar tu biblioteca.';
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => { 
  if (authStore.isAuthenticated) {
    fetchLibrary();
  } else {
    errorMessage.value = "Debes iniciar sesión para ver tu biblioteca.";
    isLoading.value = false;
  }
});

const filteredLibraryGames = computed(() => {
  if (!myLibraryGames.value) return [];
  return myLibraryGames.value.filter(gameEntry => {
    const statusMatch = !selectedStatus.value || gameEntry.status === selectedStatus.value;
    const platformMatch = !selectedPlatform.value || gameEntry.personal_platform === selectedPlatform.value;
    let scoreMatch = true;
    if (selectedMinScore.value !== null && selectedMinScore.value !== undefined) {
      if (selectedMinScore.value === 0) {
        scoreMatch = gameEntry.score === null || gameEntry.score === undefined || gameEntry.score === 0;
      } else {
        scoreMatch = gameEntry.score !== null && gameEntry.score !== undefined && gameEntry.score >= selectedMinScore.value;
      }
    }
    return statusMatch && platformMatch && scoreMatch;
  });
});

const getCoverUrl = (coverData, targetSizeFallback = 't_cover_big') => { 
  if (coverData && typeof coverData.url === 'string' && coverData.url.trim() !== '') { 
    let imageUrl = coverData.url;
    if (imageUrl.startsWith('//')) { imageUrl = `https:${imageUrl}`; }
    if (imageUrl.includes('/t_thumb/')) {
      imageUrl = imageUrl.replace('/t_thumb/', `/${targetSizeFallback}/`); 
    } else if (imageUrl.includes('/t_cover_small/')) {
      imageUrl = imageUrl.replace('/t_cover_small/', `/${targetSizeFallback}/`); 
    }
    return imageUrl; 
  }
  return defaultLibraryCover; 
};

const onLibraryCoverError = (event) => {
  event.target.src = defaultLibraryCover; 
};

const resetFilters = () => {
  selectedStatus.value = null;
  selectedPlatform.value = null;
  selectedMinScore.value = null;
};
</script>

<style src="./MyLibraryView.css" scoped></style>