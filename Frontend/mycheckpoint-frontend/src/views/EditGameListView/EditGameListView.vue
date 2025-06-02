<template>
  <div class="edit-gamelist-view">
    <div v-if="isLoading && !gameListDetails" class="loading-message">Cargando detalles de la lista...</div>
    <div v-else-if="errorMessageApi" class="error-message">{{ errorMessageApi }}</div>

    <div v-if="gameListDetails" class="gamelist-content">
      <section class="gamelist-header-section section-block">
        <div class="header-content">
          <div class="header-info">
            <h1>{{ gameListDetails.name }}</h1>
            <p v-if="gameListDetails.description" class="list-description-detail">{{ gameListDetails.description }}</p>
            <p v-else class="list-description-detail-empty"><em>Esta lista no tiene descripción.</em></p>
            <div class="list-meta-detail">
              <span :class="['status-chip', gameListDetails.is_public ? 'public' : 'private']">
                {{ gameListDetails.is_public ? 'Pública' : 'Privada' }}
              </span>
              <span class="meta-separator">|</span>
              <span>Creada por: {{ gameListDetails.owner_username }}</span>
              <span v-if="gameListDetails.updated_at" class="meta-separator">|</span>
              <span v-if="gameListDetails.updated_at">
                Última actualización: {{ formatReadableDate(gameListDetails.updated_at) }}
              </span>
            </div>
          </div>
          <div class="header-actions-gamelist">
            <button @click="openEditMetadataModal" class="action-button secondary">
              Editar Detalles de Lista
            </button>
            <button @click="handleDeleteList" :disabled="isLoadingMetadataUpdate || isLoadingActionOnGame" class="action-button danger">
              Eliminar Lista
            </button>
          </div>
        </div>
      </section>

      <div v-if="showEditMetadataModal" class="modal-overlay" @click.self="closeEditMetadataModal">
        <div class="modal-panel">
          <form @submit.prevent="handleUpdateListMetadata" class="edit-list-form-modal">
            <div class="modal-header">
              <h3>Editar Detalles de la Lista</h3>
              <button type="button" @click="closeEditMetadataModal" class="modal-close-button" aria-label="Cerrar">&times;</button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label for="editListName">Nombre de la Lista:</label>
                <input type="text" id="editListName" v-model="editListForm.name" required maxlength="150">
              </div>
              <div class="form-group">
                <label for="editListDescription">Descripción (opcional):</label>
                <textarea id="editListDescription" v-model="editListForm.description" rows="4" maxlength="1000"></textarea>
              </div>
              <div class="form-group checkbox-group">
                <input type="checkbox" id="editListIsPublic" v-model="editListForm.is_public">
                <label for="editListIsPublic">¿Hacer esta lista pública?</label>
              </div>
            </div>
            <div class="modal-footer">
              <div v-if="editMetadataMessage" :class="editMetadataError ? 'error-message' : 'success-message'" class="action-message modal-action-message">
                {{ editMetadataMessage }}
              </div>
              <button type="button" @click="closeEditMetadataModal" class="action-button secondary" :disabled="isLoadingMetadataUpdate">
                Cancelar
              </button>
              <button type="submit" :disabled="isLoadingMetadataUpdate || !editListForm.name" class="action-button primary">
                {{ isLoadingMetadataUpdate ? 'Guardando...' : 'Guardar Cambios' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <section class="games-in-list-section section-block">
        <div class="section-header-actions">
          <h2>Juegos en "{{ gameListDetails.name }}" ({{ gameListDetails.game_count || (gameListDetails.games_in_list ? gameListDetails.games_in_list.length : 0) }})</h2>
          <button @click="openAddGamesModal" class="action-button primary" :disabled="isLoadingActionOnGame">Añadir Juegos</button>
        </div>
        
        <div v-if="isLoadingActionOnGame && !showAddGamesModal" class="loading-message small-loader">Actualizando lista...</div>
        <div v-else-if="!gameListDetails.games_in_list || gameListDetails.games_in_list.length === 0" class="empty-message list-empty">
          Esta lista aún no tiene juegos.
        </div>
        <div class="library-grid" v-else>
          <div v-for="gameEntry in gameListDetails.games_in_list" :key="gameEntry.internal_id" class="library-game-card">
            <RouterLink :to="{ name: 'game-details', params: { igdbId: gameEntry.game_igdb_id } }" class="game-card-link-content">
              <img 
                :src="getCoverUrl(gameEntry.game_cover, 'cover_big')" 
                :alt="`Carátula de ${gameEntry.game_name || 'Juego'}`" 
                class="library-game-cover" 
                @error="onListGameCoverError" 
              />
              <div class="card-content">
                <h3 class="game-title">{{ gameEntry.game_name || `Juego ID: ${gameEntry.game_igdb_id}` }}</h3>
                <div v-if="gameEntry.status" class="info-item minimal-info">
                  {{ formatUserGameStatus(gameEntry.status) }}
                </div>
              </div>
            </RouterLink>
            <button 
              @click.stop="handleRemoveGameFromList(gameEntry.internal_id)" 
              class="remove-game-button" 
              title="Quitar de la lista"
              :disabled="isLoadingActionOnGame">
              &times;
            </button>
          </div>
        </div>
      </section>
    </div>

    <div v-if="showAddGamesModal" class="modal-overlay" @click.self="closeAddGamesModal">
      <div class="modal-panel add-games-modal-panel">
        <div class="modal-header">
          <h3>Añadir Juegos a "{{ gameListDetails?.name }}"</h3>
          <button type="button" @click="closeAddGamesModal" class="modal-close-button" aria-label="Cerrar">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="isLoadingLibraryForSelection" class="loading-message">Cargando tu biblioteca...</div>
          <div v-if="addGamesErrorMessage" class="error-message modal-error">{{ addGamesErrorMessage }}</div>
          
          <div v-if="!isLoadingLibraryForSelection && libraryForSelection.length === 0 && !addGamesErrorMessage" class="empty-message">
            No hay más juegos en tu biblioteca para añadir o ya están todos en esta lista.
          </div>

          <ul class="add-games-list" v-if="libraryForSelection.length > 0">
            <li v-for="game in libraryForSelection" :key="game.internal_id" 
                :class="{ 'selected-for-add': gamesToAdd.has(game.internal_id) }"
                @click="toggleGameForAddition(game.internal_id)">
              <img :src="getCoverUrl(game.game_cover, 'cover_big')" @error="onListGameCoverError" class="add-game-cover-thumb" />
              <span class="add-game-name-text">{{ game.game_name || `ID: ${game.game_igdb_id}` }}</span>
              <input type="checkbox" :checked="gamesToAdd.has(game.internal_id)" @click.stop readonly class="add-game-checkbox"/>
            </li>
          </ul>
        </div>
        <div class="modal-footer">
          <div v-if="addGamesErrorMessage && !isLoadingLibraryForSelection" :class="addGamesErrorMessage ? 'error-message' : ''" class="action-message modal-action-message">
            {{ addGamesErrorMessage }}
          </div>
          <button type="button" @click="closeAddGamesModal" class="action-button secondary" :disabled="isLoadingActionOnGame || isLoadingLibraryForSelection">
            Cancelar
          </button>
          <button @click="handleAddSelectedGamesToList" :disabled="isLoadingActionOnGame || isLoadingLibraryForSelection || gamesToAdd.size === 0" class="action-button primary">
            {{ isLoadingActionOnGame ? 'Añadiendo...' : `Añadir Seleccionados (${gamesToAdd.size})` }}
          </button>
        </div>
      </div>
    </div>
    <div v-if="!isLoading && !gameListDetails && errorMessageApi" class="no-results-message">
        No se pudo cargar la lista de juegos o no tienes permiso para verla.
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, watch, reactive } from 'vue';
import { useRoute, RouterLink, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { 
  getMySpecificGameListDetails, 
  updateMyUserGameList,
  addGameToMyGameList,
  removeGameFromMyGameList,
  getMyUserGameLibrary, // Para el modal de añadir juegos
  deleteMyGameList // Para eliminar la lista completa
} from '@/services/apiInstances';
import defaultLibraryCover from '@/assets/img/default-game-cover.png';

const props = defineProps({
  listPublicId: {
    type: String,
    required: true,
  },
});

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const gameListDetails = ref(null); // GameListResponseDTO
const isLoading = ref(true); // Loader principal para la lista
const errorMessageApi = ref('');

// Estado para modal de edición de metadatos
const showEditMetadataModal = ref(false);
const isLoadingMetadataUpdate = ref(false);
const editMetadataMessage = ref('');
const editMetadataError = ref(false);
const editListForm = reactive({
  name: '',
  description: null,
  is_public: false,
});

// Estado para Modal de "Añadir Juegos"
const showAddGamesModal = ref(false);
const isLoadingLibraryForSelection = ref(false);
const libraryForSelection = ref([]);
const gamesToAdd = ref(new Set()); // Set de internal_id de UserGame
const addGamesErrorMessage = ref('');
const isLoadingActionOnGame = ref(false); // Loader para añadir/quitar juego individual

const fetchListDetails = async (id) => {
  if (!id) {
    errorMessageApi.value = "ID de lista no proporcionado.";
    isLoading.value = false;
    return;
  }
  isLoading.value = true;
  errorMessageApi.value = '';
  // gameListDetails.value = null; // No resetear aquí para que no parpadee si solo se actualizan los juegos
  try {
    const response = await getMySpecificGameListDetails(id);
    gameListDetails.value = response.data;
    console.log("Detalles de la lista de juegos cargados/actualizados:", gameListDetails.value);
  } catch (error) {
    console.error(`Error cargando detalles de la lista de juegos (ID: ${id}):`, error);
    if (error.response) {
      errorMessageApi.value = `Error ${error.response.status}: ${error.response.data.message || error.response.data.error || 'No se pudieron cargar los detalles de la lista.'}`;
      if (error.response.status === 403 || error.response.status === 401 || error.response.status === 404) {
        router.push({ name: 'my-gamelists-profile-tab' }); // Redirigir a la vista de "Mis Listas" en el perfil
      }
    } else {
      errorMessageApi.value = 'Error de red al cargar los detalles de la lista.';
    }
    gameListDetails.value = null; // Asegurarse de limpiar en caso de error total
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchListDetails(props.listPublicId);
});

watch(() => props.listPublicId, (newId, oldId) => {
  if (newId && newId !== oldId) {
    fetchListDetails(newId);
  }
});

// --- Lógica para Editar Metadatos de la Lista ---
const openEditMetadataModal = () => {
  if (!gameListDetails.value) return;
  editListForm.name = gameListDetails.value.name || '';
  editListForm.description = gameListDetails.value.description || null;
  editListForm.is_public = gameListDetails.value.is_public || false;
  editMetadataMessage.value = '';
  editMetadataError.value = false;
  showEditMetadataModal.value = true;
};

const closeEditMetadataModal = () => {
  showEditMetadataModal.value = false;
};

const handleUpdateListMetadata = async () => {
  if (!editListForm.name || editListForm.name.trim() === '') {
    editMetadataMessage.value = "El nombre de la lista es obligatorio.";
    editMetadataError.value = true;
    return;
  }
  isLoadingMetadataUpdate.value = true;
  editMetadataMessage.value = '';
  editMetadataError.value = false;

  const dto = {
    name: editListForm.name,
    description: editListForm.description || null,
    is_public: editListForm.is_public,
  };
  
  // Construir DTO solo con campos que cambiaron, si el backend soporta PATCH o PUT parcial.
  // Si el backend espera todos los campos para PUT, enviar 'dto' directamente.
  // La documentación de GameListControllerApi.updateMyGameList dice "Solo los campos proporcionados [...] serán actualizados".
  const changes = {};
  let hasChanges = false;
  if (dto.name !== gameListDetails.value.name) { changes.name = dto.name; hasChanges = true; }
  if (dto.description !== (gameListDetails.value.description || null)) { changes.description = dto.description; hasChanges = true; }
  if (dto.is_public !== gameListDetails.value.is_public) { changes.is_public = dto.is_public; hasChanges = true; }

  if (!hasChanges) {
    editMetadataMessage.value = "No se han realizado cambios.";
    editMetadataError.value = false;
    isLoadingMetadataUpdate.value = false;
    return;
  }

  try {
    const response = await updateMyUserGameList(props.listPublicId, changes);
    gameListDetails.value = response.data; 
    editMetadataMessage.value = "¡Detalles de la lista actualizados!";
    editMetadataError.value = false;
    setTimeout(() => closeEditMetadataModal(), 1500);
  } catch (error) {
    console.error("Error actualizando metadatos de la lista:", error);
    editMetadataError.value = true;
    if (error.response?.data) {
      editMetadataMessage.value = error.response.data.errors?.join(', ') || error.response.data.message || error.response.data.error || "No se pudo actualizar la lista.";
    } else {
      editMetadataMessage.value = "Error de red al actualizar la lista.";
    }
  } finally {
    isLoadingMetadataUpdate.value = false;
  }
};

// --- Lógica para "Añadir Juegos" ---
const fetchLibraryForSelection = async () => {
  isLoadingLibraryForSelection.value = true;
  addGamesErrorMessage.value = '';
  try {
    const response = await getMyUserGameLibrary();
    const currentGameIdsInList = new Set(gameListDetails.value?.games_in_list?.map(g => g.internal_id) || []);
    libraryForSelection.value = response.data.filter(game => !currentGameIdsInList.has(game.internal_id));
  } catch (error) {
    console.error("Error fetching user library for selection:", error);
    addGamesErrorMessage.value = "No se pudo cargar tu biblioteca para seleccionar juegos.";
  } finally {
    isLoadingLibraryForSelection.value = false;
  }
};

const openAddGamesModal = () => {
  console.log("Botón 'Añadir Juegos' clickeado. Abriendo modal..."); // <--- AÑADE ESTE LOG
  gamesToAdd.value.clear();
  fetchLibraryForSelection(); // Esta función carga los juegos para el modal
  showAddGamesModal.value = true;
  console.log("showAddGamesModal debería ser true ahora:", showAddGamesModal.value); // <--- AÑADE ESTE LOG TAMBIÉN
};

const closeAddGamesModal = () => {
  showAddGamesModal.value = false;
};

const toggleGameForAddition = (userGameInternalId) => {
  if (gamesToAdd.value.has(userGameInternalId)) {
    gamesToAdd.value.delete(userGameInternalId);
  } else {
    gamesToAdd.value.add(userGameInternalId);
  }
};

const handleAddSelectedGamesToList = async () => {
  if (gamesToAdd.value.size === 0) {
    addGamesErrorMessage.value = "Selecciona al menos un juego para añadir.";
    return;
  }
  isLoadingActionOnGame.value = true;
  addGamesErrorMessage.value = '';
  
  const promises = [];
  gamesToAdd.value.forEach(userGameId => {
    promises.push(addGameToMyGameList(props.listPublicId, { user_game_id: userGameId }));
  });

  try {
    await Promise.all(promises);
    // Tras añadir, recargar los detalles de la lista para ver los cambios.
    await fetchListDetails(props.listPublicId); 
    closeAddGamesModal();
  } catch (error) {
    console.error("Error añadiendo juegos a la lista:", error);
    if (error.response?.data) {
      addGamesErrorMessage.value = error.response.data.message || error.response.data.error || "Error al añadir algunos juegos.";
    } else {
      addGamesErrorMessage.value = "Error de red al añadir juegos.";
    }
  } finally {
    isLoadingActionOnGame.value = false;
  }
};

// --- Lógica para "Quitar Juego de la Lista" ---
const handleRemoveGameFromList = async (userGameInternalId) => {
  if (!userGameInternalId) {
    errorMessageApi.value = "Error: No se pudo identificar el juego a eliminar.";
    return;
  }
  if (!window.confirm("¿Estás seguro de que quieres quitar este juego de la lista?")) return;

  isLoadingActionOnGame.value = true;
  errorMessageApi.value = '';
  try {
    await removeGameFromMyGameList(props.listPublicId, userGameInternalId);
    // Actualizar localmente o recargar
    if (gameListDetails.value?.games_in_list) {
      gameListDetails.value.games_in_list = gameListDetails.value.games_in_list.filter(
        game => game.internal_id !== userGameInternalId
      );
      gameListDetails.value.game_count = gameListDetails.value.games_in_list.length;
    }
  } catch (error) {
    console.error("Error quitando juego de la lista:", error);
    if (error.response?.data) {
      errorMessageApi.value = error.response.data.message || error.response.data.error || "No se pudo quitar el juego.";
    } else {
      errorMessageApi.value = "Error de red al quitar el juego.";
    }
  } finally {
    isLoadingActionOnGame.value = false;
  }
};

// --- Lógica para "Eliminar Lista" (TODO) ---
const handleDeleteList = async () => {
  if (!gameListDetails.value) return;
  if (window.confirm(`¿Estás seguro de que quieres eliminar la lista "${gameListDetails.value.name}" permanentemente? Esta acción no se puede deshacer.`)) {
    isLoading.value = true; // Usar el loader principal
    errorMessageApi.value = '';
    try {
      console.log("Intentando eliminar lista con listPublicId:", props.listPublicId);
if (!props.listPublicId) {
    errorMessageApi.value = "Error: ID de lista no disponible para eliminación.";
    isLoading.value = false; // Asegúrate de resetear isLoading si retornas aquí
    return;
}
      await deleteMyGameList(props.listPublicId);
      
      // Redirección CORREGIDA:
      // Opción 1: Ir al perfil del usuario (y él puede seleccionar la pestaña de listas si quiere)
      if (authStore.currentUser?.public_id) {
        router.push({ name: 'profile', params: { publicId: authStore.currentUser.public_id } });
        // Para que se seleccione la pestaña de "Mis Listas" al llegar, UserProfileView.vue
        // podría necesitar una lógica para aceptar una query param o un prop que indique la pestaña activa.
        // O, podrías tener una ruta específica para "Mis Listas" como tab del perfil.
        // Por simplicidad ahora, redirigimos al perfil general.
      } else {
        router.push({ name: 'home' }); // Fallback a la home si no podemos ir al perfil
      }
      
      // Opcional: Si tienes una ruta específica que es la vista de "Mis Listas" (no la de edición)
      // router.push({ name: 'nombre-de-ruta-para-ver-todas-mis-listas' });

    } catch (error) {
        console.error("Error eliminando la lista:", error);
        if (error.response?.data) {
          errorMessageApi.value = error.response.data.message || error.response.data.error || "No se pudo eliminar la lista.";
        } else if (error.message && error.message.includes("status code 404")) {
            errorMessageApi.value = "No se pudo eliminar la lista: Recurso no encontrado (404). Verifica el endpoint o el ID.";
        }
         else {
          errorMessageApi.value = "Error de red o inesperado al eliminar la lista.";
        }
    } finally {
        isLoading.value = false;
    }
  }
};


// --- Funciones Helper de Formato ---
const getCoverUrl = (coverData, targetSize = 't_cover_small') => {
  const currentPlaceholder = defaultLibraryCover; 
  if (coverData && typeof coverData.url === 'string' && coverData.url.trim() !== '') { 
    let imageUrl = coverData.url; 
    if (imageUrl.startsWith('//')) { imageUrl = `https:${imageUrl}`; }
    const originalUrlBeforeReplace = imageUrl;
    imageUrl = imageUrl.replace(/(\/t_)[a-zA-Z0-9_-]+(\/)/, `$1${targetSize}$2`);
    if (imageUrl === originalUrlBeforeReplace && !imageUrl.includes(`/${targetSize}/`) && imageUrl.includes('/upload/') && !imageUrl.includes('/igdb/image/upload/' + targetSize + '/')) {
      imageUrl = imageUrl.replace('/upload/', `/upload/${targetSize}/`);
    }
    return imageUrl; 
  }
  return currentPlaceholder; 
};
const onListGameCoverError = (event) => { event.target.src = defaultLibraryCover; };
const formatUserGameStatus = (status) => {
  if (!status) return 'N/A';
  const map = { 'PLAYING': 'Jugando', 'COMPLETED_MAIN_STORY': 'Completado', 'COMPLETED': 'Completado 100%', 'WISHLIST': 'En Deseos', /* ...otros */ };
  return map[status] || status;
};
const formatReadableDate = (isoDateString) => {
  if (!isoDateString) return '';
  return new Date(isoDateString).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
};
const truncateText = (text, maxLength) => {
  if (!text) return '';
  return text.length <= maxLength ? text : text.substring(0, maxLength) + '...';
};
</script>
<style scoped>
.edit-gamelist-view {
  max-width: 900px;
  margin: 1rem auto;
  padding: 1rem;
}

.loading-message, .error-message, .empty-message {
  text-align: center; padding: 2rem; font-size: 1.2rem;
}
.error-message { color: #d8000c; background-color: #ffbaba; border: 1px solid #d8000c; border-radius: 4px;}
.empty-message.list-empty { margin-top: 1rem; color: var(--color-text-light-2); }

.section-block {
  padding: 1.5rem;
  margin-bottom: 1.5rem; 
  background-color: var(--color-background-soft);
  border-radius: 8px;
  border: 1px solid var(--color-border);
}
.section-block:last-child { margin-bottom: 0; }

.gamelist-header-section .header-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
@media (min-width: 768px) {
  .gamelist-header-section .header-content {
    flex-direction: row;
    justify-content: space-between;
    align-items: flex-start;
  }
  .gamelist-header-section .header-actions-gamelist {
    flex-shrink: 0; /* Evita que los botones se encojan */
    margin-left: 1rem;
  }
}


.gamelist-header-section h1 {
  font-size: 2rem;
  color: var(--color-heading);
  margin-top: 0;
  margin-bottom: 0.5rem;
}
.list-description-detail {
  font-size: 1rem;
  color: var(--color-text);
  margin-bottom: 0.75rem;
  line-height: 1.6;
  white-space: pre-wrap;
}
.list-description-detail-empty {
    font-size: 1rem; color: var(--color-text-light-2); font-style: italic; margin-bottom: 0.75rem;
}
.list-meta-detail {
  font-size: 0.9rem;
  color: var(--color-text-light-2);
}
.list-meta-detail .status-chip {
  font-size: 0.8rem; padding: 0.2em 0.6em; border-radius: 10px;
  font-weight: 500; margin-right: 0.5rem;
}
.list-meta-detail .status-chip.public {
  background-color: hsla(160, 100%, 37%, 0.15); color: hsla(160, 100%, 30%, 1);
  border: 1px solid hsla(160, 100%, 37%, 0.3);
}
.list-meta-detail .status-chip.private {
  background-color: var(--color-background-mute); color: var(--color-text-light-2);
  border: 1px solid var(--color-border);
}
.meta-separator { margin: 0 0.5em; }

.games-in-list-section .section-header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--color-border-hover);
}
.games-in-list-section h2 {
  font-size: 1.6rem; color: var(--color-heading);
  margin:0; padding: 0; border: none;
}

/* Estilos para el grid de juegos (reutilizados de MyLibraryView) */
.library-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); /* Más pequeños para esta vista */
  gap: 1rem;
}
.library-game-card {
  background-color: var(--color-background);
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.07);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
  border: 1px solid var(--color-border);
  position: relative; /* Para el botón de eliminar */
}
.library-game-card:hover { transform: translateY(-4px); box-shadow: 0 3px 8px rgba(0,0,0,0.1); }
.library-game-card a { text-decoration: none; color: inherit; display: flex; flex-direction: column; height: 100%; }
.library-game-cover { width: 100%; aspect-ratio: 3/4; object-fit: cover; background-color: var(--color-background-mute); }
.card-content { padding: 0.6rem; display: flex; flex-direction: column; flex-grow: 1; }
.game-title {
  font-size: 0.9rem; font-weight: 600; color: var(--color-heading);
  margin: 0 0 0.3rem 0; line-height: 1.2;
  overflow: hidden; text-overflow: ellipsis; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical; min-height: calc(1.2em * 2);
}
.info-item.minimal-info { font-size: 0.75rem; color: var(--color-text-light-2); margin-top: auto;}

.remove-game-button {
  position: absolute;
  top: 5px;
  right: 5px;
  background-color: rgba(211, 72, 72, 0.8); /* Rojo semitransparente */
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  font-size: 14px;
  line-height: 24px;
  text-align: center;
  cursor: pointer;
  opacity: 0.7;
  transition: opacity 0.2s;
}
.library-game-card:hover .remove-game-button {
  opacity: 1;
}
.remove-game-button:hover {
  background-color: #d9534f; /* Rojo más opaco */
}


/* Estilos del Modal (similares a GameDetailsView modal) */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background-color: rgba(0, 0, 0, 0.65); display: flex;
  justify-content: center; align-items: center; z-index: 1000;
}
.modal-panel {
  background-color: var(--color-background-soft); padding: 0;
  border-radius: 8px; box-shadow: 0 5px 15px rgba(0,0,0,0.3);
  width: 90%; max-width: 550px; 
  display: flex; flex-direction: column; max-height: 90vh;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 1rem 1.5rem; border-bottom: 1px solid var(--color-border);
}
.modal-header h3 { margin: 0; font-size: 1.3rem; color: var(--color-heading); }
.modal-close-button {
  background: none; border: none; font-size: 1.8rem; line-height: 1;
  color: var(--color-text-light-2); cursor: pointer;
}
.modal-body { padding: 1.5rem; overflow-y: auto; }
.edit-list-form-modal .form-group { margin-bottom: 1rem; }
.edit-list-form-modal .form-group:last-of-type { margin-bottom: 0; }
.edit-list-form-modal label { display: block; margin-bottom: 0.4rem; font-size: 0.9rem; color: var(--color-text); font-weight:500; }
.edit-list-form-modal input[type="text"],
.edit-list-form-modal textarea {
  width: 100%; padding: 0.7rem; border: 1px solid var(--color-border);
  border-radius: 4px; background-color: var(--color-background); color: var(--color-text); font-size: 1rem;
}
.edit-list-form-modal .checkbox-group { display: flex; align-items: center; margin-top: 0.5rem;}
.edit-list-form-modal .checkbox-group input[type="checkbox"] { margin-right: 0.5rem; width: auto; transform: scale(1.1); }
.edit-list-form-modal .checkbox-group label { margin-bottom: 0; font-size: 1rem; font-weight: normal; color: var(--color-text); }
.action-message { font-size: 0.9rem; margin-top: 0.5rem; text-align:left; }
.modal-error { /* Ya definido, pero puedes especializar .action-message.error-message */ }

.modal-footer {
  padding: 1rem 1.5rem; border-top: 1px solid var(--color-border);
  display: flex; justify-content: space-between; /* Para mensaje a la izquierda, botones a la derecha */
  align-items: center; gap: 0.75rem;
}
.modal-footer .action-message { /* Mensaje en el footer */
   flex-grow: 1; text-align: left;
}
.modal-footer .buttons-group { /* Contenedor para botones si necesitas agruparlos */
  display: flex;
  gap: 0.75rem;
}


/* Action button (consistente) */
.action-button {
  padding: 0.7em 1.4em; font-size: 0.9rem; border-radius: 5px;
  cursor: pointer; transition: background-color 0.2s, color 0.2s, border-color 0.2s;
  border: 1px solid transparent; font-weight: 500;
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

.section-header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}
.section-header-actions h2 {
  margin:0; padding: 0; border: none;
}

.library-game-card {
  position: relative; /* Para posicionar el botón de quitar */
}
.game-card-link-content { /* Nuevo wrapper para el contenido que no es el botón de eliminar */
    display: flex;
    flex-direction: column;
    height: 100%;
}

.remove-game-button {
  position: absolute;
  top: 6px;
  right: 6px;
  background-color: rgba(220, 53, 69, 0.7); /* Rojo bootstrap danger con transparencia */
  color: white;
  border: none;
  border-radius: 50%;
  width: 26px;
  height: 26px;
  font-size: 16px;
  font-weight: bold;
  line-height: 26px;
  text-align: center;
  cursor: pointer;
  opacity: 0; /* Oculto por defecto */
  transition: opacity 0.2s, background-color 0.2s;
  z-index: 10; /* Encima de la imagen/enlace */
}
.library-game-card:hover .remove-game-button {
  opacity: 1; /* Visible al pasar el ratón por la tarjeta */
}
.remove-game-button:hover {
  background-color: #dc3545; /* Rojo bootstrap danger */
}
.remove-game-button:disabled {
    background-color: #aaa;
    cursor: not-allowed;
}


/* Estilos para el Modal de Añadir Juegos */
.add-games-modal-panel { /* Hereda de .modal-panel pero puede tener ajustes */
    max-width: 700px; /* Un poco más ancho para la lista */
}
.add-games-list {
  list-style-type: none;
  padding: 0;
  max-height: 60vh; /* Altura máxima para la lista antes de scroll */
  overflow-y: auto;
}
.add-games-list li {
  display: flex;
  align-items: center;
  padding: 0.75rem 0.5rem;
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  transition: background-color 0.15s ease-in-out;
}
.add-games-list li:last-child {
  border-bottom: none;
}
.add-games-list li:hover {
  background-color: var(--color-background-mute);
}
.add-games-list li.selected-for-add {
  background-color: hsla(160, 100%, 37%, 0.1);
  font-weight: 500;
}
.add-game-cover-thumb {
  width: 40px;
  height: auto;
  aspect-ratio: 3/4;
  object-fit: cover;
  margin-right: 1rem;
  border-radius: 3px;
}
.add-games-list li span {
  flex-grow: 1;
}
.add-game-checkbox {
  margin-left: 1rem;
  pointer-events: none; /* Para que el clic en el li funcione */
  transform: scale(1.2);
}
.modal-error {
  padding: 0.5rem; /* Menos padding para errores dentro del modal */
  font-size: 0.9rem;
}

</style>