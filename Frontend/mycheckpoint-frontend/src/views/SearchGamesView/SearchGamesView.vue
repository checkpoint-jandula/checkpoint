<template>
  <div class="search-results-view">
    <h1>{{ pageTitle }}</h1>

    <div v-if="isLoading" class="loading-message">Cargando resultados...</div>
    <div v-else-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    <div v-else-if="!isLoading && searchResults.length === 0 && hasSearched" class="no-results-message">
      No se encontraron juegos que coincidan con los criterios.
    </div>

    <div class="results-grid" v-else-if="!isLoading && searchResults.length > 0">
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
    <div v-else class="no-results-message">
      Usa la barra de búsqueda o los filtros avanzados en el menú para encontrar juegos.
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { useRoute, RouterLink } from 'vue-router';
// MODIFICADO: Importamos las funciones específicas de apiInstances
import { buscarJuegosEnIgdb, filtrarJuegosEnIgdb } from '@/services/apiInstances'; 
import defaultGameCover from '@/assets/img/default-game-cover.svg';

const route = useRoute();
const searchResults = ref([]);
const isLoading = ref(false);
const errorMessage = ref('');
const hasSearched = ref(false);

const pageTitle = computed(() => {
  const query = route.query;
  if (query.q) {
    return `Resultados para: "${query.q}"`;
  }
  if (query.filter === 'true') {
    return "Resultados del Filtro Avanzado";
  }
  return "Búsqueda de Juegos";
});

// MODIFICADO: La lógica interna de esta función cambia para usar los nuevos servicios
const processRouteQuery = async (query) => {
  if (Object.keys(query).length === 0) {
    searchResults.value = [];
    hasSearched.value = false;
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';
  searchResults.value = [];
  hasSearched.value = true;

  try {
    let response;
    if (query.q) {
      // Se llama a la función de búsqueda por texto
      response = await buscarJuegosEnIgdb(query.q);
    } else if (query.filter === 'true') {
      // Se llama a la función de filtrado, pasando los parámetros individualmente
      response = await filtrarJuegosEnIgdb(
        query.fecha_inicio,
        query.fecha_fin,
        query.id_genero,
        query.id_tema,
        query.id_modo_juego,
        query.limite
      );
    } else {
      isLoading.value = false;
      hasSearched.value = false;
      return;
    }
    
    searchResults.value = response.data;

    if (response.data.length === 0) {
        errorMessage.value = "No se encontraron juegos que coincidan con los criterios."
    }

  } catch (error) {
    console.error("Error al obtener los resultados del juego:", error);
    errorMessage.value = 'No se pudieron obtener los resultados. Por favor, inténtalo de nuevo.';
  } finally {
    isLoading.value = false;
  }
};

// El watch no necesita cambios, seguirá funcionando perfectamente
watch(() => route.query, 
  (newQuery) => {
    processRouteQuery(newQuery);
  }, 
  { immediate: true, deep: true }
);

// --- Funciones de Utilidad (sin cambios) ---
const getCoverUrl = (cover) => {
  if (cover && typeof cover.url === 'string') {
    let imageUrl = cover.url.replace('/t_thumb/', '/t_cover_big/');
    if (imageUrl.startsWith('//')) {
      imageUrl = `https:${imageUrl}`;
    }
    return imageUrl;
  }
  return defaultGameCover;
};

const onImageError = (event) => {
  event.target.src = defaultGameCover;
};

const formatReleaseYear = (timestamp) => {
  if (!timestamp) return '';
  return new Date(Number(timestamp) * 1000).getFullYear();
};

const truncateText = (text, maxLength) => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};
</script>

<style src="./SearchGamesView.css" scoped></style>