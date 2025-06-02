<template>
  <div class="view-public-tierlists">
    <h1>Tier Lists Públicas</h1>

    <div v-if="isLoading" class="loading-message">Cargando tier lists públicas...</div>
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

    <div v-if="!isLoading && publicTierLists.length === 0 && !errorMessage" class="empty-message">
      Actualmente no hay tier lists públicas disponibles.
    </div>

    <div class="tierlists-grid" v-if="!isLoading && publicTierLists.length > 0">
      <div v-for="tierList in publicTierLists" :key="tierList.public_id" class="tierlist-card">
        <RouterLink :to="{ name: 'view-public-tierlist', params: { tierListPublicId: tierList.public_id } }" class="card-link">
          <div class="card-header">
            <h3 class="list-name">{{ tierList.name }}</h3>
            <span class="list-type-chip">{{ formatTierListType(tierList.type) }}</span>
          </div>
          <p v-if="tierList.description" class="list-description">{{ truncateText(tierList.description, 150) }}</p>
          <p v-else class="list-description-empty"><em>Sin descripción.</em></p>
          <div class="list-footer">
            <span class="owner-info">Por: {{ tierList.owner_username }}</span>
            <span class="last-updated" v-if="tierList.updated_at">Última act.: {{ formatReadableDate(tierList.updated_at) }}</span>
          </div>
        </RouterLink>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import { fetchAllPublicTierLists } from '@/services/apiInstances';

const publicTierLists = ref([]); // Almacenará Array<TierListResponseDTO>
const isLoading = ref(true);
const errorMessage = ref('');

const loadPublicTierLists = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const response = await fetchAllPublicTierLists();
    publicTierLists.value = response.data;
    console.log("Tier lists públicas cargadas:", publicTierLists.value);
  } catch (error) {
    console.error("Error cargando tier lists públicas:", error);
    if (error.response) {
      errorMessage.value = `Error ${error.response.status}: ${error.response.data.message || error.response.data.error || 'No se pudieron cargar las tier lists.'}`;
    } else {
      errorMessage.value = 'Error de red al cargar las tier lists.';
    }
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  loadPublicTierLists();
});

const formatReadableDate = (isoDateString) => {
  if (!isoDateString) return '';
  try {
    return new Date(isoDateString).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
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

const formatTierListType = (type) => { //
  if (!type) return 'Tipo Desconocido';
  const typeMap = {
    'PROFILE_GLOBAL': 'De Perfil',
    'FROM_GAMELIST': 'Basada en Lista de Juegos'
  };
  return typeMap[type] || type;
};
</script>
<style scoped>
.view-public-tierlists {
  max-width: 1200px;
  margin: 1rem auto;
  padding: 1rem;
}

.view-public-tierlists > h1 {
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

.tierlists-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.tierlist-card {
  background-color: var(--color-background-soft);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
  display: flex; 
  flex-direction: column;
}

.tierlist-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.12);
}

.tierlist-card .card-link {
  text-decoration: none;
  color: inherit;
  padding: 1.2rem;
  display: flex;
  flex-direction: column;
  height: 100%;
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

.list-type-chip {
  font-size: 0.8rem;
  color: var(--color-text);
  background-color: var(--color-background-mute);
  padding: 0.2em 0.6em;
  border-radius: 10px;
  border: 1px solid var(--color-border);
}

.list-description {
  font-size: 0.9rem;
  color: var(--color-text);
  line-height: 1.5;
  margin-bottom: 1rem;
  flex-grow: 1; 
  display: -webkit-box;
  -webkit-line-clamp: 3; 
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: calc(1.5em * 3); 
}
.list-description-empty {
  font-size: 0.9rem;
  color: var(--color-text-light-2);
  font-style: italic;
  margin-bottom: 1rem;
  flex-grow: 1;
}

.list-footer {
  margin-top: auto; 
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: var(--color-text-light-2);
  padding-top: 0.75rem;
  border-top: 1px solid var(--color-border);
}
</style>