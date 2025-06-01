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

import { GameControllerApi } from '@/api-client/index.js'; //
import apiClient from '@/services/apiService.js';
import { Configuration } from '@/api-client/configuration.js'; //
console.log('SearchGamesView.vue: API client imports cargados');

import defaultGameCover from '@/assets/img/default-game-cover.png'; // Asegúrate que este archivo exista
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

<style src="./SearchGamesView.css" scoped></style>