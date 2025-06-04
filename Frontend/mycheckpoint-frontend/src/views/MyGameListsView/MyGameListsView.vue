<template>
  <div class="my-game-lists-view section-block">
    <div class="header-actions">
      <h2>Mis Listas de Juegos</h2>
      <button @click="openCreateListModal" class="action-button primary">Crear Nueva Lista</button>
    </div>

    <div v-if="isLoading" class="loading-message">Cargando mis listas...</div>
    <div v-if="errorMessageApi" class="error-message">{{ errorMessageApi }}</div>

    <div v-if="!isLoading && gameLists.length === 0 && !errorMessageApi" class="empty-message">
      Aún no has creado ninguna lista de juegos.
    </div>

    <div class="gamelists-grid" v-if="!isLoading && gameLists.length > 0">
      <div v-for="list in gameLists" :key="list.public_id" class="gamelist-card-mylists">
        <RouterLink :to="{ name: 'gamelist-detail', params: { listPublicId: list.public_id } }" class="card-link-mylists">
          <div class="card-header-mylists">
            <h3 class="list-name-mylists">{{ list.name }}</h3>
            <span class="list-status-chip" :class="list.is_public ? 'public' : 'private'">
              {{ list.is_public ? 'Pública' : 'Privada' }}
            </span>
          </div>
          <p v-if="list.description" class="list-description-mylists">{{ truncateText(list.description, 120) }}</p>
          <p v-else class="list-description-empty-mylists"><em>Sin descripción.</em></p>
          <div class="list-footer-mylists">
            <span class="game-count-mylists">{{ list.game_count }} juego(s)</span>
            <span class="last-updated-mylists" v-if="list.updated_at">Última act.: {{ formatReadableDate(list.updated_at) }}</span>
          </div>
        </RouterLink>
      </div>
    </div>

    <div v-if="showCreateModal" class="modal-overlay" @click.self="closeCreateListModal">
      <div class="modal-panel">
        <form @submit.prevent="handleCreateList" class="create-list-form">
          <div class="modal-header">
            <h3>Crear Nueva Lista de Juegos</h3>
            <button type="button" @click="closeCreateListModal" class="modal-close-button" aria-label="Cerrar">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label for="listName">Nombre de la Lista:</label>
              <input type="text" id="listName" v-model="newListForm.name" required maxlength="150">
            </div>
            <div class="form-group">
              <label for="listDescription">Descripción (opcional):</label>
              <textarea id="listDescription" v-model="newListForm.description" rows="3" maxlength="1000"></textarea>
            </div>
            <div class="form-group checkbox-group">
              <input type="checkbox" id="listIsPublic" v-model="newListForm.is_public">
              <label for="listIsPublic">¿Hacer esta lista pública?</label>
            </div>
            <div v-if="createErrorMessage" class="error-message modal-error">{{ createErrorMessage }}</div>
          </div>
          <div class="modal-footer">
            <button type="button" @click="closeCreateListModal" class="action-button secondary" :disabled="isCreatingList">
              Cancelar
            </button>
            <button type="submit" :disabled="isCreatingList || !newListForm.name" class="action-button primary">
              {{ isCreatingList ? 'Creando...' : 'Crear Lista' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, reactive } from 'vue';
import { RouterLink } from 'vue-router';
import { getMyGameLists, createMyGameList } from '@/services/apiInstances';


const gameLists = ref([]); // Array<GameListResponseDTO>
const isLoading = ref(true);
const errorMessageApi = ref('');

// Estado para el modal de creación
const showCreateModal = ref(false);
const isCreatingList = ref(false);
const createErrorMessage = ref('');
const newListForm = reactive({ // GameListRequestDTO
  name: '',
  description: null,
  is_public: false,
});

const loadMyLists = async () => {
  isLoading.value = true;
  errorMessageApi.value = '';
  try {
    const response = await getMyGameLists();
    gameLists.value = response.data;
  } catch (error) {
    console.error("Error cargando mis listas de juegos:", error);
    errorMessageApi.value = "Error al cargar tus listas de juegos.";
    if (error.response) {
        errorMessageApi.value = `Error ${error.response.status}: ${error.response.data.message || error.response.data.error || 'No se pudieron cargar las listas.'}`;
    }
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  loadMyLists();
});

const openCreateListModal = () => {
  newListForm.name = '';
  newListForm.description = null;
  newListForm.is_public = false;
  createErrorMessage.value = '';
  showCreateModal.value = true;
};

const closeCreateListModal = () => {
  showCreateModal.value = false;
};

const handleCreateList = async () => {
  if (!newListForm.name || newListForm.name.trim() === '') {
    createErrorMessage.value = "El nombre de la lista es obligatorio.";
    return;
  }
  isCreatingList.value = true;
  createErrorMessage.value = '';

  const requestDTO = {
    name: newListForm.name,
    description: newListForm.description || null, // Enviar null si está vacío
    is_public: newListForm.is_public,
  };

  try {
    const response = await createMyGameList(requestDTO);
    gameLists.value.unshift(response.data); // Añadir la nueva lista al principio
    closeCreateListModal();
  } catch (error) {
    console.error("Error creando lista de juegos:", error);
    if (error.response && error.response.data) {
      if (error.response.data.errors && Array.isArray(error.response.data.errors)) {
        createErrorMessage.value = error.response.data.errors.join(', ');
      } else {
        createErrorMessage.value = error.response.data.message || error.response.data.error || "No se pudo crear la lista.";
      }
    } else {
      createErrorMessage.value = "Error de red al crear la lista.";
    }
  } finally {
    isCreatingList.value = false;
  }
};

const formatReadableDate = (isoDateString) => {
  if (!isoDateString) return '';
  return new Date(isoDateString).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
};

const truncateText = (text, maxLength) => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};
</script>
<style src="./MyGameListsView.css" scoped>

</style>