<template>
  <div class="edit-tierlist-view">
    <div v-if="isLoading && !tierListDetails" class="loading-message">Cargando Tier List...</div>
    <div v-else-if="errorMessageApi" class="error-message">{{ errorMessageApi }}</div>

    <div v-if="tierListDetails" class="tierlist-content">
      <section class="tierlist-header-section section-block">
        <div class="header-content">
          <div class="header-info">
            <h1>{{ tierListDetails.name }}</h1>
            <p v-if="tierListDetails.description" class="list-description-detail">{{ tierListDetails.description }}</p>
            <p v-else class="list-description-detail-empty"><em>Esta tier list no tiene descripción.</em></p>
            <div class="list-meta-detail">
              <span :class="['status-chip', tierListDetails.is_public ? 'public' : 'private']">
                {{ tierListDetails.is_public ? 'Pública' : 'Privada' }}
              </span>
              <span class="meta-separator">|</span>
              <span>Tipo: {{ formatTierListType(tierListDetails.type) }}</span>
              <span v-if="tierListDetails.type === 'FROM_GAMELIST' && tierListDetails.source_game_list_public_id" class="meta-separator">|</span>
              <span v-if="tierListDetails.type === 'FROM_GAMELIST' && tierListDetails.source_game_list_public_id">
                Fuente: Lista de Juegos ID {{ tierListDetails.source_game_list_public_id.substring(0,8) }}...
              </span>
              <span class="meta-separator">|</span>
              <span>Creada por: {{ tierListDetails.owner_username }}</span>
              <span v-if="tierListDetails.updated_at" class="meta-separator">|</span>
              <span v-if="tierListDetails.updated_at">
                Última act.: {{ formatReadableDate(tierListDetails.updated_at) }}
              </span>
            </div>
          </div>
          <div v-if="isOwner" class="header-actions-tierlist">
            <button class="action-button primary">Editar Detalles</button> <button class="action-button primary">Eliminar Tier List</button> </div>
        </div>
      </section>

      <div class="tier-sections-container">
        <section class="tier-section unclassified-section section-block" 
                 v-if="tierListDetails.unclassified_section && tierListDetails.unclassified_section.items && tierListDetails.unclassified_section.items.length > 0">
          <header class="tier-section-header">
            <h2>{{ tierListDetails.unclassified_section.name }}</h2>
            </header>
          <div class="tier-items-grid">
            <div v-for="item in tierListDetails.unclassified_section.items" :key="item.tier_list_item_id" class="tier-item-card">
              <RouterLink :to="{ name: 'game-details', params: { igdbId: item.game_igdb_id } }">
                <img :src="getItemCoverUrl(item.game_cover_url, 'cover_big')" :alt="item.game_name" class="tier-item-cover" @error="onTierItemCoverError" />
                <span class="tier-item-name">{{ item.game_name }}</span>
              </RouterLink>
              </div>
          </div>
        </section>

        <section v-for="section in sortedCustomSections" :key="section.internal_id" class="tier-section section-block">
          <header class="tier-section-header">
            <h2>{{ section.name }}</h2>
            </header>
          <div v-if="!section.items || section.items.length === 0" class="empty-message">
            No hay juegos en esta tier.
          </div>
          <div class="tier-items-grid" v-else>
            <div v-for="item in section.items" :key="item.tier_list_item_id" class="tier-item-card">
              <RouterLink :to="{ name: 'game-details', params: { igdbId: item.game_igdb_id } }">
                <img :src="getItemCoverUrl(item, 't_thumb')" :alt="item.game_name" class="tier-item-cover" @error="onTierItemCoverError" />
                <span class="tier-item-name">{{ item.game_name }}</span>
              </RouterLink>
              </div>
          </div>
        </section>
      </div>
        </div>

    <div v-if="!isLoading && !tierListDetails && errorMessageApi" class="no-results-message">
        No se pudo cargar la Tier List o no es accesible.
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useRoute, RouterLink, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { getTierListDetailsByPublicId } from '@/services/apiInstances';
import defaultTierItemCover from '@/assets/img/default-game-cover.png'; // Placeholder para items

const props = defineProps({
  tierListPublicId: {
    type: String,
    required: true,
  },
});

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const tierListDetails = ref(null); // Almacenará TierListResponseDTO
const isLoading = ref(true);
const errorMessageApi = ref('');

// Propiedad computada para determinar si el usuario actual es el propietario
const isOwner = computed(() => {
  if (!authStore.isAuthenticated || !authStore.currentUser || !tierListDetails.value) {
    return false;
  }
  return authStore.currentUser.nombre_usuario === tierListDetails.value.owner_username;
});

// Propiedad computada para las secciones ordenadas (excluyendo la no clasificada por ahora)
const sortedCustomSections = computed(() => {
  if (tierListDetails.value && tierListDetails.value.sections) {
    return [...tierListDetails.value.sections].sort((a, b) => (a.order || 0) - (b.order || 0));
  }
  return [];
});

const fetchTierListDetails = async (id) => {
  if (!id) {
    errorMessageApi.value = "ID de Tier List no proporcionado.";
    isLoading.value = false;
    return;
  }
  isLoading.value = true;
  errorMessageApi.value = '';
  tierListDetails.value = null;
  try {
    const response = await getTierListDetailsByPublicId(id);
    tierListDetails.value = response.data;
    console.log("Detalles de la Tier List cargados:", tierListDetails.value);
  } catch (error) {
    console.error(`Error cargando detalles de la Tier List (ID: ${id}):`, error);
    if (error.response) {
      errorMessageApi.value = `Error ${error.response.status}: ${error.response.data.message || error.response.data.error || 'No se pudieron cargar los detalles de la Tier List.'}`;
      if (error.response.status === 403 || error.response.status === 404) {
        // Si no se encuentra o no se tiene permiso, redirigir
        router.push({ name: 'my-tierlists-profile-tab' }); // O a una página de error general
      }
    } else {
      errorMessageApi.value = 'Error de red al cargar los detalles de la Tier List.';
    }
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchTierListDetails(props.tierListPublicId);
});

watch(() => props.tierListPublicId, (newId, oldId) => {
  if (newId && newId !== oldId) {
    fetchTierListDetails(newId);
  }
});

// Función para obtener URL de carátula de un item de la Tier List
const getItemCoverUrl = (itemCoverUrl, targetSize = 'cover_small') => {
  // TierListItemGameInfoDTO ya provee game_cover_url
  if (typeof itemCoverUrl === 'string' && itemCoverUrl.trim() !== '') {
    let imageUrl = itemCoverUrl;
    if (imageUrl.startsWith('//')) {
      imageUrl = `https:${imageUrl}`;
    }
    // Aplicar transformación de tamaño si es necesario
    const regex = /(\/t_)[a-zA-Z0-9_-]+(\/)/;
    if (regex.test(imageUrl)) { // Si ya tiene un /t_.../
        imageUrl = imageUrl.replace(regex, `$1${targetSize}$2`);
    } else if (imageUrl.includes('/igdb/image/upload/')) { 
        if (!imageUrl.includes('/igdb/image/upload/' + targetSize + '/')) {
             imageUrl = imageUrl.replace('/upload/', `/upload/${targetSize}/`);
        }
    }
    return imageUrl;
  }
  return defaultTierItemCover; // Placeholder
};

const onTierItemCoverError = (event) => {
  event.target.src = defaultTierItemCover;
};

const formatReadableDate = (isoDateString) => {
  if (!isoDateString) return '';
  return new Date(isoDateString).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
};

const formatTierListType = (type) => {
  if (!type) return 'Tipo Desconocido';
  const typeMap = { 'PROFILE_GLOBAL': 'De Perfil', 'FROM_GAMELIST': 'Desde Lista de Juegos' };
  return typeMap[type.toUpperCase()] || type;
};

// TODO: Funciones para editar nombre/descripción de TierList, secciones, ítems, etc.
</script>
<style scoped>
.edit-tierlist-view {
  max-width: 1000px;
  margin: 1rem auto;
  padding: 1rem;
}

.loading-message, .error-message, .empty-message, .no-results-message {
  text-align: center; padding: 2rem; font-size: 1.2rem;
}
.error-message { color: #d8000c; background-color: #ffbaba; border: 1px solid #d8000c; border-radius: 4px;}
.empty-message { color: var(--color-text-light-2); font-style: italic; }

.section-block {
  padding: 1.5rem;
  margin-bottom: 1.5rem; 
  background-color: var(--color-background-soft);
  border-radius: 8px;
  border: 1px solid var(--color-border);
}
.section-block:last-child { margin-bottom: 0; }

.tierlist-header-section .header-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.tierlist-header-section h1 { font-size: 2rem; color: var(--color-heading); margin: 0 0 0.5rem 0; }
.list-description-detail { font-size: 1rem; color: var(--color-text); margin-bottom: 0.75rem; line-height: 1.6; white-space: pre-wrap; }
.list-description-detail-empty { font-size: 1rem; color: var(--color-text-light-2); font-style: italic; margin-bottom: 0.75rem; }
.list-meta-detail { font-size: 0.9rem; color: var(--color-text-light-2); }
.list-meta-detail .status-chip { /* Reutiliza estilos de GameListDetailView */ }
.meta-separator { margin: 0 0.5em; }
.header-actions-tierlist { margin-top: 1rem; display: flex; gap: 0.5rem; }

.tier-sections-container {
  margin-top: 1.5rem;
}
.tier-section { /* Cada fila de la tier list (S, A, B, Sin Clasificar...) */
  margin-bottom: 2rem; /* Espacio entre tiers */
}
.tier-section:last-child { margin-bottom: 0.5rem; }

.tier-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--color-border-hover);
}
.tier-section-header h2 {
  font-size: 1.5rem;
  color: var(--color-heading);
  margin: 0;
}
.unclassified-section h2 { /* Estilo un poco diferente para la sección "Sin Clasificar" */
  color: var(--color-text-light-2);
  font-style: italic;
}

.tier-items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); /* Items más pequeños */
  gap: 0.75rem;
  min-height: 120px; /* Altura mínima para que se vea como una fila aunque esté vacía */
  padding: 0.5rem;
  background-color: var(--color-background-mute);
  border-radius: 4px;
}

.tier-item-card {
  background-color: var(--color-background);
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  overflow: hidden;
  text-align: center;
  border: 1px solid var(--color-border);
}
.tier-item-card a { text-decoration: none; color: inherit; display: block; }
.tier-item-cover {
  width: 100%;
  aspect-ratio: 3 / 4;
  object-fit: cover;
  background-color: var(--color-background-soft);
}
.tier-item-name {
  display: block;
  font-size: 0.8rem;
  padding: 0.4rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--color-text);
}

/* Action button (reutilizado) */
.action-button {
  padding: 0.6em 1.2em; font-size: 0.95rem; border-radius: 4px;
  cursor: pointer; transition: background-color 0.2s, color 0.2s;
  border: 1px solid transparent; 
  font-weight: 500;
}
.action-button.primary {
  background-color: hsla(160, 100%, 37%, 1); color: white;
  border-color: hsla(160, 100%, 37%, 1);
}
.action-button.primary:hover:not(:disabled) { background-color: hsla(160, 100%, 30%, 1); }
.action-button.secondary {
  background-color: var(--color-background-mute); color: var(--color-text);
  border-color: var(--color-border-hover);
}
.action-button.secondary:hover:not(:disabled) { background-color: var(--color-border); }
.action-button:disabled { opacity: 0.6; cursor: not-allowed; }
</style>