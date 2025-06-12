<template>
  <div class="home-view">
    <h1>Explorar Juegos</h1>

    <section class="game-row">
      <h2>Más Populares</h2>
      <div v-if="isLoading.hyped" class="loading-message">Cargando...</div>
      <div v-if="errors.hyped" class="error-message">{{ errors.hyped }}</div>
      
      <div v-else class="carousel-wrapper">
        <button @click="scrollCarousel(mostHypedCarouselRef, 'prev')" class="carousel-arrow prev" aria-label="Anterior">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5" /></svg>
        </button>
        
        <div class="games-carousel" ref="mostHypedCarouselRef">
          <RouterLink v-for="game in mostHyped" :key="game.id" :to="{ name: 'game-details', params: { igdbId: game.id } }" class="game-card">
            <img :src="getCoverUrl(game.cover)" :alt="game.name" class="game-cover" @error.self="onImageError">
            <span class="game-name">{{ game.name }}</span>
          </RouterLink>
        </div>
        
        <button @click="scrollCarousel(mostHypedCarouselRef, 'next')" class="carousel-arrow next" aria-label="Siguiente">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m8.25 4.5 7.5 7.5-7.5 7.5" /></svg>
        </button>
      </div>
    </section>


    <section class="game-row">
      <h2>Próximos Lanzamientos</h2>
      <div v-if="isLoading.upcoming" class="loading-message">Cargando...</div>
      <div v-if="errors.upcoming" class="error-message">{{ errors.upcoming }}</div>

      <div v-else class="carousel-wrapper">
        <button @click="scrollCarousel(upcomingReleasesCarouselRef, 'prev')" class="carousel-arrow prev" aria-label="Anterior">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5" /></svg>
        </button>
        
        <div class="games-carousel" ref="upcomingReleasesCarouselRef">
          <RouterLink v-for="game in upcomingReleases" :key="game.id" :to="{ name: 'game-details', params: { igdbId: game.id } }" class="game-card">
            <img :src="getCoverUrl(game.cover)" :alt="game.name" class="game-cover" @error.self="onImageError">
            <span class="game-name">{{ game.name }}</span>
          </RouterLink>
        </div>
        
        <button @click="scrollCarousel(upcomingReleasesCarouselRef, 'next')" class="carousel-arrow next" aria-label="Siguiente">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m8.25 4.5 7.5 7.5-7.5 7.5" /></svg>
        </button>
      </div>
    </section>

    <section class="game-row">
      <h2>Más Esperado</h2>
      <div v-if="isLoading.anticipated" class="loading-message">Cargando...</div>
      <div v-if="errors.anticipated" class="error-message">{{ errors.anticipated }}</div>
      
      <div v-else class="carousel-wrapper">
        <button @click="scrollCarousel(highlyAnticipatedCarouselRef, 'prev')" class="carousel-arrow prev" aria-label="Anterior">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5" /></svg>
        </button>
        
        <div class="games-carousel" ref="highlyAnticipatedCarouselRef">
          <RouterLink v-for="game in highlyAnticipated" :key="game.id" :to="{ name: 'game-details', params: { igdbId: game.id } }" class="game-card">
            <img :src="getCoverUrl(game.cover)" :alt="game.name" class="game-cover" @error.self="onImageError">
            <span class="game-name">{{ game.name }}</span>
          </RouterLink>
        </div>
        
        <button @click="scrollCarousel(highlyAnticipatedCarouselRef, 'next')" class="carousel-arrow next" aria-label="Siguiente">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m8.25 4.5 7.5 7.5-7.5 7.5" /></svg>
        </button>
      </div>
    </section>

    <section class="game-row">
      <h2>Lanzados Recientemente</h2>
      <div v-if="isLoading.recently" class="loading-message">Cargando...</div>
      <div v-if="errors.recently" class="error-message">{{ errors.recently }}</div>
      
      <div v-else class="carousel-wrapper">
        <button @click="scrollCarousel(recentlyReleasedCarouselRef, 'prev')" class="carousel-arrow prev" aria-label="Anterior">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5" /></svg>
        </button>
        
        <div class="games-carousel" ref="recentlyReleasedCarouselRef">
          <RouterLink v-for="game in recentlyReleased" :key="game.id" :to="{ name: 'game-details', params: { igdbId: game.id } }" class="game-card">
            <img :src="getCoverUrl(game.cover)" :alt="game.name" class="game-cover" @error.self="onImageError">
            <span class="game-name">{{ game.name }}</span>
          </RouterLink>
        </div>
        
        <button @click="scrollCarousel(recentlyReleasedCarouselRef, 'next')" class="carousel-arrow next" aria-label="Siguiente">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m8.25 4.5 7.5 7.5-7.5 7.5" /></svg>
        </button>
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
import defaultGameCover from '@/assets/img/default-game-cover.svg';

// Estado para cada lista de juegos
const recentlyReleased = ref([]);
const mostHyped = ref([]);
const highlyAnticipated = ref([]);
const upcomingReleases = ref([]);

const mostHypedCarouselRef = ref(null);
const upcomingReleasesCarouselRef = ref(null);
const highlyAnticipatedCarouselRef = ref(null);
const recentlyReleasedCarouselRef = ref(null);

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


const scrollCarousel = (carouselRef, direction) => {
  if (carouselRef) {
    // Calculamos cuánto nos vamos a desplazar (ej: 80% del ancho visible)
    const scrollAmount = carouselRef.clientWidth * 0.5;
    
    // Usamos el método scrollBy para un desplazamiento suave
    carouselRef.scrollBy({
      left: direction === 'next' ? scrollAmount : -scrollAmount,
      behavior: 'smooth'
    });
  }
};
</script>

<style src="./HomeView.css" scoped>

</style>