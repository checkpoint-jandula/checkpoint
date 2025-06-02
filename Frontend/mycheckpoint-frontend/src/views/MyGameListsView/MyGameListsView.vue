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
        <RouterLink :to="{ name: 'edit-gamelist', params: { listPublicId: list.public_id } }" class="card-link-mylists">
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
// No necesitamos authStore aquí ya que los endpoints son para '/me/'

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
<style scoped>
/* Hereda .section-block de UserProfileView si se embebe, o definirlo aquí si es standalone */
.my-game-lists-view {
  /* padding: 1.5rem; si no es un section-block en sí mismo */
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.header-actions h2 {
  margin: 0;
  font-size: 1.6rem; /* Consistente con otros títulos de sección */
  color: var(--color-heading);
}

.loading-message, .error-message, .empty-message {
  text-align: center; padding: 2rem; font-size: 1.2rem;
}
.error-message { color: #d8000c; background-color: #ffbaba; border: 1px solid #d8000c; border-radius: 4px;}
.empty-message { color: var(--color-text-light-2); }

.gamelists-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.gamelist-card-mylists {
  background-color: var(--color-background);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
  border: 1px solid var(--color-border);
}
.gamelist-card-mylists:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}
.card-link-mylists {
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
  padding: 1rem 1.2rem;
  height: 100%;
}
.card-header-mylists {
  display: flex;
  justify-content: space-between;
  align-items: flex-start; /* Para que el chip no estire el header si el título es corto */
  margin-bottom: 0.5rem;
}
.list-name-mylists {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
  line-height: 1.3;
}
.list-status-chip {
  font-size: 0.75rem;
  padding: 0.2em 0.6em;
  border-radius: 10px;
  font-weight: 500;
  white-space: nowrap;
}
.list-status-chip.public {
  background-color: hsla(160, 100%, 37%, 0.15);
  color: hsla(160, 100%, 30%, 1);
  border: 1px solid hsla(160, 100%, 37%, 0.3);
}
.list-status-chip.private {
  background-color: var(--color-background-mute);
  color: var(--color-text-light-2);
  border: 1px solid var(--color-border);
}
.list-description-mylists, .list-description-empty-mylists {
  font-size: 0.85rem;
  color: var(--color-text);
  line-height: 1.5;
  margin-bottom: 0.75rem;
  flex-grow: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: calc(1.5em * 2); /* Espacio para al menos 2 líneas */
}
.list-description-empty-mylists { font-style: italic; color: var(--color-text-light-2); }
.list-footer-mylists {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: var(--color-text-light-2);
  padding-top: 0.5rem;
  border-top: 1px solid var(--color-border);
}

/* Estilos del Modal de Creación (similares a los de GameDetailsView) */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background-color: rgba(0, 0, 0, 0.6); display: flex;
  justify-content: center; align-items: center; z-index: 1050; /* Encima de otros modales si los hubiera */
}
.modal-panel {
  background-color: var(--color-background-soft); padding: 0; /* El padding lo manejan header/body/footer */
  border-radius: 8px; box-shadow: 0 5px 15px rgba(0,0,0,0.3);
  width: 90%; max-width: 500px; 
  display: flex; flex-direction: column;
  max-height: 90vh;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 1rem 1.5rem; border-bottom: 1px solid var(--color-border);
}
.modal-header h3 { margin: 0; font-size: 1.2rem; color: var(--color-heading); }
.modal-close-button {
  background: none; border: none; font-size: 1.8rem; line-height: 1;
  color: var(--color-text-light-2); cursor: pointer;
}
.modal-body {
  padding: 1.5rem; overflow-y: auto;
}
.create-list-form .form-group { margin-bottom: 1rem; }
.create-list-form .form-group:last-of-type { margin-bottom: 0; }
.create-list-form label { display: block; margin-bottom: 0.3rem; font-size: 0.9rem; color: var(--color-text-light-2); }
.create-list-form input[type="text"],
.create-list-form textarea {
  width: 100%; padding: 0.6rem; border: 1px solid var(--color-border);
  border-radius: 4px; background-color: var(--color-background); color: var(--color-text); font-size: 1rem;
}
.create-list-form .checkbox-group { display: flex; align-items: center; margin-top: 0.5rem;}
.create-list-form .checkbox-group input[type="checkbox"] { margin-right: 0.5rem; width: auto; }
.create-list-form .checkbox-group label { margin-bottom: 0; font-size: 1rem; color: var(--color-text); }
.modal-error { font-size: 0.9rem; margin-top: 0.5rem; }

.modal-footer {
  padding: 1rem 1.5rem; border-top: 1px solid var(--color-border);
  display: flex; justify-content: flex-end; gap: 0.75rem;
}

/* Action button (si no tienes uno global, puedes usar estos) */
.action-button {
  padding: 0.6em 1.2em; font-size: 0.95rem; border-radius: 4px;
  cursor: pointer; transition: background-color 0.2s, color 0.2s;
  border: 1px solid transparent; 
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