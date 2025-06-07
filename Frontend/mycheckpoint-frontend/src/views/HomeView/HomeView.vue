<template>
  <div class="home-view">
    <h1>Explorar Juegos</h1>

    <section class="game-row">
      <h2>Lanzados Recientemente</h2>
      <div v-if="isLoading.recently" class="loading-message">Cargando...</div>
      <div v-if="errors.recently" class="error-message">{{ errors.recently }}</div>
      <div v-else class="games-carousel">
        <RouterLink v-for="game in recentlyReleased" :key="game.id" :to="{ name: 'game-details', params: { igdbId: game.id } }" class="game-card">
          <img :src="getCoverUrl(game.cover)" :alt="game.name" class="game-cover" @error.self="onImageError">
          <span class="game-name">{{ game.name }}</span>
        </RouterLink>
      </div>
    </section>

    <section class="game-row">
      <h2>Próximos Lanzamientos</h2>
      <div v-if="isLoading.upcoming" class="loading-message">Cargando...</div>
      <div v-if="errors.upcoming" class="error-message">{{ errors.upcoming }}</div>
      <div v-else class="games-carousel">
        <RouterLink v-for="game in upcomingReleases" :key="game.id" :to="{ name: 'game-details', params: { igdbId: game.id } }" class="game-card">
          <img :src="getCoverUrl(game.cover)" :alt="game.name" class="game-cover" @error.self="onImageError">
          <span class="game-name">{{ game.name }}</span>
        </RouterLink>
      </div>
    </section>

    <section class="game-row">
      <h2>Más Populares</h2>
      <div v-if="isLoading.hyped" class="loading-message">Cargando...</div>
      <div v-if="errors.hyped" class="error-message">{{ errors.hyped }}</div>
      <div v-else class="games-carousel">
        <RouterLink v-for="game in mostHyped" :key="game.id" :to="{ name: 'game-details', params: { igdbId: game.id } }" class="game-card">
          <img :src="getCoverUrl(game.cover)" :alt="game.name" class="game-cover" @error.self="onImageError">
          <span class="game-name">{{ game.name }}</span>
        </RouterLink>
      </div>
    </section>

    <section class="game-row">
      <h2>Más Anticipados</h2>
      <div v-if="isLoading.anticipated" class="loading-message">Cargando...</div>
      <div v-if="errors.anticipated" class="error-message">{{ errors.anticipated }}</div>
      <div v-else class="games-carousel">
        <RouterLink v-for="game in highlyAnticipated" :key="game.id" :to="{ name: 'game-details', params: { igdbId: game.id } }" class="game-card">
          <img :src="getCoverUrl(game.cover)" :alt="game.name" class="game-cover" @error.self="onImageError">
          <span class="game-name">{{ game.name }}</span>
        </RouterLink>
      </div>
    </section>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import {
  findRecentlyReleasedGames,
  findMostHypedGames,
  findHighlyAnticipatedGames,
  findUpcomingReleases
} from '@/services/apiInstances';
import defaultGameCover from '@/assets/img/default-game-cover.png';

// Estado para cada lista de juegos
const recentlyReleased = ref([]);
const mostHyped = ref([]);
const highlyAnticipated = ref([]);
const upcomingReleases = ref([]);

// Estado de carga para cada sección
const isLoading = reactive({
  recently: true,
  hyped: true,
  anticipated: true,
  upcoming: true
});

// Estado de error para cada sección
const errors = reactive({
  recently: '',
  hyped: '',
  anticipated: '',
  upcoming: ''
});

// Cargar todos los datos cuando el componente se monta
onMounted(async () => {
  const [recentlyRes, hypedRes, anticipatedRes, upcomingRes] = await Promise.allSettled([
    findRecentlyReleasedGames(),
    findMostHypedGames(),
    findHighlyAnticipatedGames(),
    findUpcomingReleases()
  ]);

  // Procesar resultados para Lanzamientos Recientes
  if (recentlyRes.status === 'fulfilled') {
    recentlyReleased.value = recentlyRes.value.data;
  } else {
    errors.recently = 'No se pudieron cargar los lanzamientos recientes.';
    console.error('Error en findRecentlyReleasedGames:', recentlyRes.reason);
  }
  isLoading.recently = false;

  // Procesar resultados para Más Populares
  if (hypedRes.status === 'fulfilled') {
    mostHyped.value = hypedRes.value.data;
  } else {
    errors.hyped = 'No se pudieron cargar los juegos más populares.';
    console.error('Error en findMostHypedGames:', hypedRes.reason);
  }
  isLoading.hyped = false;

  // Procesar resultados para Más Anticipados
  if (anticipatedRes.status === 'fulfilled') {
    highlyAnticipated.value = anticipatedRes.value.data;
  } else {
    errors.anticipated = 'No se pudieron cargar los juegos más anticipados.';
    console.error('Error en findHighlyAnticipatedGames:', anticipatedRes.reason);
  }
  isLoading.anticipated = false;

  // Procesar resultados para Próximos Lanzamientos
  if (upcomingRes.status === 'fulfilled') {
    upcomingReleases.value = upcomingRes.value.data;
  } else {
    errors.upcoming = 'No se pudieron cargar los próximos lanzamientos.';
    console.error('Error en findUpcomingReleases:', upcomingRes.reason);
  }
  isLoading.upcoming = false;
});


// Función de utilidad para obtener la URL de la carátula
const getCoverUrl = (cover) => {
  if (cover && typeof cover.url === 'string') {
    let imageUrl = cover.url.replace('/t_thumb/', '/t_cover_big/');
    if (imageUrl.startsWith('//')) {
      return `https:${imageUrl}`;
    }
    return imageUrl;
  }
  return defaultGameCover;
};

const onImageError = (event) => {
  event.target.src = defaultGameCover;
};
</script>

<style scoped>
.home-view {
  padding: 1rem 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

h1 {
  font-size: 2.2rem;
  color: var(--color-heading);
  margin-bottom: 1rem;
}

.game-row {
  margin-bottom: 2.5rem;
}

.game-row h2 {
  font-size: 1.6rem;
  color: var(--color-heading);
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--color-border);
}

.games-carousel {
  display: flex;
  overflow-x: auto;
  padding-bottom: 1rem; /* Espacio para la barra de scroll */
  gap: 1.5rem;
  scrollbar-width: thin;
  scrollbar-color: var(--color-border) var(--color-background-soft);
}

.games-carousel::-webkit-scrollbar {
  height: 8px;
}

.games-carousel::-webkit-scrollbar-track {
  background: var(--color-background-soft);
  border-radius: 4px;
}

.games-carousel::-webkit-scrollbar-thumb {
  background-color: var(--color-border-hover);
  border-radius: 4px;
}

.game-card {
  flex: 0 0 160px; /* No crece, no se encoge, base de 160px */
  display: flex;
  flex-direction: column;
  text-decoration: none;
  border-radius: 6px;
  overflow: hidden;
  background-color: var(--color-background-soft);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.game-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.game-cover {
  width: 100%;
  height: 213px; /* Proporción 3:4 en un ancho de 160px */
  object-fit: cover;
  background-color: var(--color-background-mute);
}

.game-name {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--color-text);
  padding: 0.75rem 0.5rem;
  text-align: center;
  line-height: 1.3;
}

.loading-message, .error-message {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-light-2);
}

.error-message {
    color: #d8000c;
    background-color: #ffbaba;
    border-radius: 4px;
}
</style>