<template>
  <div class="search-results-view">
    <h1 v-if="searchQueryFromRoute">Resultados para: "{{ searchQueryFromRoute }}"</h1>
    <h1 v-else>Búsqueda de Juegos</h1>

    <div v-if="isLoading" class="loading-message">Cargando resultados...</div>
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

    <div v-if="!isLoading && searchResults.length === 0 && searchQueryFromRoute && !errorMessage" class="no-results-message">
      No se encontraron juegos para "{{ searchQueryFromRoute }}".
    </div>

    <div class="results-grid" v-if="!isLoading && searchResults.length > 0">
      <div v-for="game in searchResults" :key="game.id" class="game-card">
        <RouterLink :to="{ name: 'game-details', params: { igdbId: game.id } }">
          <img
            :src="getCoverUrl(game.cover)"
            :alt="`Carátula de ${game.name || 'Juego sin nombre'}`"
            class="game-cover"
            @error="onImageError"
          />
          <div class="game-info">
            <h3 class="game-name">{{ game.name || 'Nombre no disponible' }}</h3>
            <p class="game-year" v-if="game.first_release_date">
              {{ formatReleaseYear(game.first_release_date) }}
            </p>
            <p v-if="game.summary" class="game-summary">{{ truncateText(game.summary, 100) }}</p>
          </div>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
console.log('SearchGamesView.vue: <script setup> está ejecutándose');

import { ref, watch, onMounted } from 'vue';
console.log('SearchGamesView.vue: Imports básicos cargados');

import { useRoute, RouterLink } from 'vue-router';
console.log('SearchGamesView.vue: Vue Router imports cargados');

import { GameControllerApi } from '@/api-client'; //
import apiClient from '@/services/apiService';
import { Configuration } from '@/api-client/configuration'; //
console.log('SearchGamesView.vue: API client imports cargados');

import defaultGameCover from '@/assets/default-game-cover.png'; // Asegúrate que este archivo exista
console.log('SearchGamesView.vue: defaultGameCover importado');


const route = useRoute();
console.log('SearchGamesView.vue: useRoute() llamado. Query actual:', route.query.q);

const gamesApi = new GameControllerApi(new Configuration(), undefined, apiClient); //
console.log('SearchGamesView.vue: GameControllerApi instanciado');

const searchResults = ref([]);
const isLoading = ref(false);
const errorMessage = ref('');
const searchQueryFromRoute = ref(route.query.q || '');
console.log(`SearchGamesView.vue: Refs inicializados. searchQueryFromRoute: "${searchQueryFromRoute.value}"`);


const fetchSearchResults = async (query) => {
  if (!query) {
    searchResults.value = [];
    errorMessage.value = '';
    console.log("fetchSearchResults: No query provided, clearing results.");
    return;
  }

  console.log(`fetchSearchResults: Iniciando búsqueda para query: "${query}"`);
  isLoading.value = true;
  errorMessage.value = '';
  searchResults.value = [];

  try {
    console.log(`fetchSearchResults: Llamando a gamesApi.buscarJuegosEnIgdb con "${query}"`);
    const response = await gamesApi.buscarJuegosEnIgdb(query); //
    
    console.log("fetchSearchResults: Respuesta de la API recibida:", response);

    if (response && response.data) {
      if (response.data.length > 0) {
        console.log("fetchSearchResults: Estructura del primer juego recibido:", JSON.stringify(response.data[0], null, 2));
      }
      searchResults.value = response.data; // response.data es Array<GameDto>
      console.log(`fetchSearchResults: searchResults.value actualizado. Número de resultados: ${searchResults.value.length}`);
      if (searchResults.value.length === 0) {
        console.log("fetchSearchResults: La API devolvió 0 resultados.");
      }
    } else {
      console.warn("fetchSearchResults: La respuesta de la API o response.data es undefined o null.");
      searchResults.value = [];
    }

  } catch (error) {
    console.error("fetchSearchResults: Error durante la búsqueda de juegos:", error);
    if (error.response) {
      console.error("fetchSearchResults: Error response data:", error.response.data);
      console.error("fetchSearchResults: Error response status:", error.response.status);
      errorMessage.value = `Error ${error.response.status}: ${error.response.data.message || error.response.data.error || 'No se pudieron obtener los resultados.'}`;
    } else if (error.request) {
      console.error("fetchSearchResults: Error request (no se recibió respuesta):", error.request);
      errorMessage.value = 'Error de red: No se pudo conectar con el servidor.';
    } else {
      console.error("fetchSearchResults: Error de configuración de la solicitud:", error.message);
      errorMessage.value = 'Error al configurar la solicitud de búsqueda.';
    }
    searchResults.value = [];
  } finally {
    isLoading.value = false;
    console.log(`fetchSearchResults: Finalizado. isLoading: ${isLoading.value}`);
  }
};

watch(() => route.query.q, (newQuery) => {
  const queryToSearch = newQuery || '';
  console.log(`WATCH route.query.q: newQuery es "${queryToSearch}"`);
  searchQueryFromRoute.value = queryToSearch;
  fetchSearchResults(queryToSearch);
}, { immediate: false });

onMounted(() => {
  const initialQuery = route.query.q || '';
  console.log(`onMounted: Hook ejecutado. initialQuery es "${initialQuery}"`);
  searchQueryFromRoute.value = initialQuery;
  if (initialQuery) {
      fetchSearchResults(initialQuery);
  } else {
      console.log("onMounted: No hay query inicial, no se llama a fetchSearchResults.");
      searchResults.value = [];
  }
});

// --- FUNCIÓN getCoverUrl REIMPLEMENTADA ---
const getCoverUrl = (cover) => { // cover es el objeto CoverDto
  if (cover && typeof cover.url === 'string') { //
    let imageUrl = cover.url;

    // 1. Asegurar el protocolo (si empieza con //)
    if (imageUrl.startsWith('//')) {
      imageUrl = `https:${imageUrl}`;
    }

    // 2. Intentar cambiar el tamaño de la imagen
    // Reemplazamos '/t_thumb/' o '/t_cover_small/' por '/t_cover_big/'
    // Es importante que estas cadenas existan en la URL original para que el replace funcione.
    if (imageUrl.includes('/t_thumb/')) {
      imageUrl = imageUrl.replace('/t_thumb/', '/t_cover_big/');
    } else if (imageUrl.includes('/t_cover_small/')) {
      imageUrl = imageUrl.replace('/t_cover_small/', '/t_cover_big/');
    }
    // Puedes añadir más transformaciones si es necesario o usar una regex más genérica
    // si conoces otros posibles tamaños de miniatura que podrían venir.
    // Ejemplo regex (usar con cuidado): 
    // imageUrl = imageUrl.replace(/(\/t_)[a-zA-Z0-9_-]+(\/)/, '$1cover_big$2');

    return imageUrl;
  }
  // Si no hay cover, cover.url, o no es un string, devolvemos el placeholder
  return defaultGameCover;
};

const onImageError = (event) => {
  console.warn("Error al cargar imagen:", event.target.src, "Cambiando a placeholder.");
  event.target.src = defaultGameCover;
};

const formatReleaseYear = (timestamp) => { //
  if (!timestamp) return '';
  // IGDB timestamps suelen estar en segundos, Date() espera milisegundos
  return new Date(Number(timestamp) * 1000).getFullYear();
};

const truncateText = (text, maxLength) => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

</script>

<style scoped>
/* Tus estilos existentes para .search-results-view, .loading-message, etc. */
.search-results-view {
  padding: 1rem;
}
.search-results-view h1 {
  margin-bottom: 1.5rem;
}
.loading-message, .error-message, .no-results-message {
  text-align: center;
  padding: 1rem;
  margin-bottom: 1rem;
}
.error-message {
  color: red;
  background-color: rgba(255,0,0,0.1);
  border: 1px solid red;
}
.no-results-message {
  color: var(--color-text);
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.5rem;
}

.game-card {
  background-color: var(--color-background-soft); /* */
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s ease-in-out;
}

.game-card:hover {
  transform: translateY(-5px);
}

.game-card a { 
  text-decoration: none;
  color: inherit;
  display: flex; /* Cambiado a flex para mejor control interno */
  flex-direction: column; /* Apilar imagen e info verticalmente */
  height: 100%; /* Para que el enlace ocupe toda la tarjeta */
}

.game-cover {
  width: 100%;
  aspect-ratio: 3 / 4; /* Proporción común para portadas, ajusta si es necesario */
  object-fit: cover; 
  background-color: var(--color-background-mute); /* */
}

.game-info {
  padding: 1rem;
  flex-grow: 1; /* Para que ocupe el espacio restante si las tarjetas tienen alturas variables */
  display: flex;
  flex-direction: column;
}

.game-name {
  font-size: 1rem; /* Ajustado */
  font-weight: bold;
  margin: 0 0 0.3rem 0; /* Ajustado */
  color: var(--color-heading); /* */
}

.game-year {
  font-size: 0.8rem; /* Ajustado */
  color: var(--vt-c-text-light-2); /* */
  margin-bottom: 0.4rem; /* Ajustado */
}
.game-summary {
  font-size: 0.8rem; /* Ajustado */
  color: var(--color-text); /* */
  line-height: 1.3; /* Ajustado */
  flex-grow: 1; /* Para que el resumen ocupe espacio si es corto */
}
</style>