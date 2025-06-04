<template>
  <div class="view-public-gamelists">
    <h1>Listas de Juegos Públicas</h1>

    <div v-if="isLoading" class="loading-message">Cargando listas públicas...</div>
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

    <div v-if="!isLoading && publicGameLists.length === 0 && !errorMessage" class="empty-message">
      Actualmente no hay listas de juegos públicas disponibles.
    </div>

    <div class="gamelists-grid" v-if="!isLoading && publicGameLists.length > 0">
      <div v-for="list in publicGameLists" :key="list.public_id" class="gamelist-card">
        <RouterLink :to="{ name: 'view-public-gamelist', params: { listPublicId: list.public_id } }" class="card-link">
          <div class="card-header">
            <h3 class="list-name">{{ list.name }}</h3>
            <span class="game-count">{{ list.game_count }} juego(s)</span>
          </div>
          <p v-if="list.description" class="list-description">{{ truncateText(list.description, 150) }}</p>
          <p v-else class="list-description-empty"><em>Sin descripción.</em></p>
          <div class="list-footer">
            <span class="owner-info">Por: {{ list.owner_username }}</span>
            <span class="last-updated" v-if="list.updated_at">Última act.: {{ formatReadableDate(list.updated_at) }}</span>
          </div>
        </RouterLink>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import { fetchAllPublicGameLists } from '@/services/apiInstances';

const publicGameLists = ref([]); // Almacenará Array<GameListResponseDTO>
const isLoading = ref(true);
const errorMessage = ref('');

const loadPublicGameLists = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const response = await fetchAllPublicGameLists();
    publicGameLists.value = response.data;
    console.log("Listas públicas cargadas:", publicGameLists.value);
  } catch (error) {
    console.error("Error cargando listas públicas de juegos:", error);
    if (error.response) {
      errorMessage.value = `Error ${error.response.status}: ${error.response.data.message || error.response.data.error || 'No se pudieron cargar las listas.'}`;
    } else {
      errorMessage.value = 'Error de red al cargar las listas.';
    }
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  loadPublicGameLists();
});

const formatReadableDate = (isoDateString) => {
  if (!isoDateString) return '';
  try {
    return new Date(isoDateString).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      // hour: '2-digit', minute: '2-digit' // Opcional si quieres la hora
    });
  } catch (e) {
    return isoDateString;
  }
};

const truncateText = (text, maxLength) => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};
</script>
<style src="./ViewPublicGameListsView.css" scoped>

</style>