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

<style src="./HomeView.css" scoped>

</style>