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
<style src="./ViewPublicTierListsView.css" scoped>

</style>