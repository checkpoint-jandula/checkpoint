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
<style scoped>
.view-public-gamelists {
  max-width: 1200px;
  margin: 1rem auto;
  padding: 1rem;
}

.view-public-gamelists > h1 {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--color-heading);
}

.loading-message, .error-message, .empty-message {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
  color: var(--color-text);
}
.error-message {
  color: #d8000c;
  background-color: #ffbaba;
  border: 1px solid #d8000c;
  border-radius: 4px;
}
.empty-message {
  color: var(--color-text-light-2);
}

.gamelists-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.gamelist-card {
  background-color: var(--color-background-soft);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
  display: flex; /* Para que el enlace ocupe toda la tarjeta */
  flex-direction: column;
}

.gamelist-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.12);
}

.gamelist-card .card-link {
  text-decoration: none;
  color: inherit;
  padding: 1.2rem;
  display: flex;
  flex-direction: column;
  height: 100%; /* Asegura que el enlace ocupe todo el espacio */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.list-name {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}

.game-count {
  font-size: 0.9rem;
  color: var(--color-text-light-2);
  background-color: var(--color-background-mute);
  padding: 0.2em 0.6em;
  border-radius: 4px;
}

.list-description {
  font-size: 0.9rem;
  color: var(--color-text);
  line-height: 1.5;
  margin-bottom: 1rem;
  flex-grow: 1; /* Para que ocupe el espacio y empuje el footer hacia abajo */
  display: -webkit-box;
  -webkit-line-clamp: 3; /* Limitar a 3 líneas */
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: calc(1.5em * 3); /* Reserva espacio para 3 líneas */
}
.list-description-empty {
  font-size: 0.9rem;
  color: var(--color-text-light-2);
  font-style: italic;
  margin-bottom: 1rem;
  flex-grow: 1;
}


.list-footer {
  margin-top: auto; /* Empuja el footer hacia abajo */
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: var(--color-text-light-2);
  padding-top: 0.75rem;
  border-top: 1px solid var(--color-border);
}
</style>