<template>
  <div class="my-library-view">
    <div class="filters-panel section-block">
      <h3>Filtrar Biblioteca</h3>
      <div class="filter-controls">
        <div class="filter-group">
          <label for="filter-status">Estado:</label>
          <select id="filter-status" v-model="selectedStatus">
            <option v-for="opt in gameStatusOptions" :key="opt.value" :value="opt.value">
              {{ opt.text }}
            </option>
          </select>
        </div>
        <div class="filter-group">
          <label for="filter-platform">Plataforma Personal:</label>
          <select id="filter-platform" v-model="selectedPlatform">
            <option v-for="opt in personalPlatformOptions" :key="opt.value" :value="opt.value">
              {{ opt.text }}
            </option>
          </select>
        </div>
        <div class="filter-group">
          <label for="filter-score">Puntuación Mínima:</label>
          <select id="filter-score" v-model.number="selectedMinScore">
             <option v-for="opt in scoreOptions" :key="opt.value" :value="opt.value">
              {{ opt.text }}
            </option>
          </select>
        </div>
        <div class="filter-group">
          <button @click="resetFilters" class="button-secondary">Limpiar Filtros</button>
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
        <RouterLink :to="{ name: 'game-details', params: { igdbId: gameEntry.game_igdb_id } }">
          <img 
            :src="getCoverUrl(gameEntry.game_cover, 't_cover_big')" 
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
              <div v-if="gameEntry.personal_platform" class="info-item">
                <strong>Plataforma:</strong> {{ formatPersonalPlatform(gameEntry.personal_platform) }}
              </div>
              <div v-if="gameEntry.has_possession !== null && gameEntry.has_possession !== undefined" class="info-item"> 
                <strong>Lo Tengo:</strong> {{ gameEntry.has_possession ? 'Sí' : 'No' }} 
              </div>
              <div v-if="gameEntry.start_date" class="info-item">
                <strong>Empezado:</strong> {{ formatDateSimple(gameEntry.start_date) }}
              </div>
              <div v-if="gameEntry.end_date" class="info-item">
                <strong>Terminado:</strong> {{ formatDateSimple(gameEntry.end_date) }} 
              </div>
            </div>
          </div>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'; // Añadido computed
import { RouterLink } from 'vue-router'; 
import { getMyUserGameLibrary } from '@/services/apiInstances';
import { useAuthStore } from '@/stores/authStore';
import defaultLibraryCover from '@/assets/img/default-game-cover.png'; 

const authStore = useAuthStore();

const myLibraryGames = ref([]);
const isLoading = ref(true); 
const errorMessage = ref('');

// --- Estado para los Filtros ---
const selectedStatus = ref(null);
const selectedPlatform = ref(null);
const selectedMinScore = ref(null);

// Opciones para los selectores de filtro
const gameStatusOptions = [
  { value: null, text: 'Todos los Estados' },
  { value: 'PLAYING', text: 'Jugando' }, { value: 'PLAYING_PAUSED', text: 'Jugando (En Pausa)' },
  { value: 'PLAYING_ENDLESS', text: 'Jugando (Sin Fin)' }, { value: 'COMPLETED_MAIN_STORY', text: 'Historia Principal Completada' },
  { value: 'COMPLETED_MAIN_AND_SIDES', text: 'Principal + Secundarias Completado' }, { value: 'COMPLETED_100_PERCENT', text: 'Completado al 100%' },
  { value: 'WISHLIST', text: 'En Lista de Deseos' }, { value: 'ARCHIVED_ABANDONED', text: 'Archivado (Abandonado)' },
  { value: 'ARCHIVED_NOT_PLAYING', text: 'Archivado (Sin Jugar)' }, { value: 'ARCHIVED', text: 'Archivado (Otro)' },
];
const personalPlatformOptions = [
  { value: null, text: 'Todas las Plataformas' },
  { value: 'STEAM', text: 'Steam' }, { value: 'EPIC_GAMES', text: 'Epic Games Store' },
  { value: 'GOG_GALAXY', text: 'GOG Galaxy' }, { value: 'XBOX', text: 'Xbox' },
  { value: 'PLAYSTATION', text: 'PlayStation' }, { value: 'NINTENDO', text: 'Nintendo' },
  { value: 'BATTLE_NET', text: 'Battle.net' }, { value: 'EA_APP', text: 'EA App' },
  { value: 'UBISOFT_CONNECT', text: 'Ubisoft Connect' }, { value: 'OTHER', text: 'Otra' },
];
const scoreOptions = [
    { value: null, text: 'Cualquier Puntuación' }, { value: 0, text: 'Sin Puntuar' },
    { value: 1, text: '1+' }, { value: 2, text: '2+' }, { value: 3, text: '3+' },
    { value: 4, text: '4+' }, { value: 5, text: '5+' }, { value: 6, text: '6+' },
    { value: 7, text: '7+' }, { value: 8, text: '8+' }, { value: 9, text: '9+' },
    { value: 10, text: '10 (Perfecta)' }
];

const fetchLibrary = async () => {
  isLoading.value = true;
  errorMessage.value = ''; 
  try {
    const response = await getMyUserGameLibrary();
    myLibraryGames.value = response.data;
    // console.log("Biblioteca cargada:", myLibraryGames.value); 
  } catch (error) {
    console.error("Error cargando la biblioteca:", error);
    if (error.response) { 
      errorMessage.value = `Error ${error.response.status}: ${error.response.data.message || error.response.data.error || 'No se pudo cargar tu biblioteca.'}`; 
    } else {
      errorMessage.value = 'Error de red al cargar tu biblioteca.'; 
    }
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

// Propiedad Computada para la Biblioteca Filtrada
const filteredLibraryGames = computed(() => {
  // console.log('[MyLibraryView computed] Evaluando filteredLibraryGames...');
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

// Funciones Helper (ya las tenías, me aseguro de que la de getCoverUrl sea la que funciona para ti)
const formatUserGameStatus = (status) => { 
  if (!status) return 'No especificado';
  const statusMap = {
    'COMPLETED': 'Completado',
    'COMPLETED_MAIN_STORY': 'Historia Principal Completada',
    'COMPLETED_MAIN_AND_SIDES': 'Principal + Secundarias Importantes Completado',
    'COMPLETED_100_PERCENT': 'Completado al 100%',
    'ARCHIVED': 'Archivado',
    'ARCHIVED_ABANDONED': 'Archivado (Abandonado)',
    'ARCHIVED_NOT_PLAYING': 'Archivado (Sin Jugar)',
    'WISHLIST': 'En Lista de Deseos',
    'PLAYING': 'Jugando',
    'PLAYING_PAUSED': 'Jugando (En Pausa)',
    'PLAYING_ENDLESS': 'Jugando (Sin Fin / Rejugable)'
    // Añade más si tu enum tiene más valores
  };
  return statusMap[status] || status; 
};

const formatPersonalPlatform = (platform) => { 
  if (!platform) return 'No especificada';
  const platformMap = {
    'STEAM': 'Steam',
    'EPIC_GAMES': 'Epic Games Store',
    'GOG_GALAXY': 'GOG Galaxy',
    'XBOX': 'Xbox',
    'PLAYSTATION': 'PlayStation',
    'NINTENDO': 'Nintendo',
    'BATTLE_NET': 'Battle.net',
    'EA_APP': 'EA App',
    'UBISOFT_CONNECT': 'Ubisoft Connect',
    'OTHER': 'Otra'
    // Añade más si tu enum tiene más valores
  };
  return platformMap[platform] || platform; 
};

const formatDateSimple = (dateString) => dateString || '';

const getCoverUrl = (coverData, targetSizeFallback = 't_cover_big') => { 
  const currentPlaceholder = defaultLibraryCover; 
  // console.log('[getCoverUrl] Recibido coverData:', coverData);
  if (coverData && typeof coverData.url === 'string' && coverData.url.trim() !== '') { 
    let imageUrl = coverData.url;
    if (imageUrl.startsWith('//')) { 
      imageUrl = `https:${imageUrl}`; 
    }
    if (imageUrl.includes('/t_thumb/')) {
      imageUrl = imageUrl.replace('/t_thumb/', `/${targetSizeFallback}/`); 
    } else if (imageUrl.includes('/t_cover_small/')) {
      imageUrl = imageUrl.replace('/t_cover_small/', `/${targetSizeFallback}/`); 
    }
    return imageUrl; 
  }
  return currentPlaceholder; 
};

const onLibraryCoverError = (event) => {
  console.warn("Error al cargar imagen de biblioteca, usando placeholder:", event.target.src);
  event.target.src = defaultLibraryCover; 
};

const resetFilters = () => {
  selectedStatus.value = null;
  selectedPlatform.value = null;
  selectedMinScore.value = null;
  // Aquí no es necesario llamar a fetchLibrary() de nuevo,
  // la propiedad computada filteredLibraryGames se actualizará automáticamente.
};
</script>

<style src="./MyLibraryView.css" scoped>

</style>