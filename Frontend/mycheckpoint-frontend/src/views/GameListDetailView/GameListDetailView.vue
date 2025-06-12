<template>
  <div class="gamelist-detail-view">
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
          <div v-if="isOwner" class="header-actions-gamelist">
            <button @click="openEditMetadataModal" class="action-button secondary">
              Editar Detalles de Lista
            </button>
            <button @click="handleGetOrCreateTierListFromGameList" class="action-button primary"
              :disabled="isLoadingTierListCreation || isLoadingMetadataUpdate || isLoadingActionOnGame">
              {{ isLoadingTierListCreation ? 'Procesando Tier List...' : 'Crear/Ver Tier List de esta Lista' }}
            </button>
            <button @click="handleDeleteList" :disabled="isLoadingMetadataUpdate || isLoadingActionOnGame"
              class="action-button danger">
              Eliminar Lista
            </button>
          </div>
        </div>
      </section>

      <div v-if="showEditMetadataModal && isOwner" class="modal-overlay" @click.self="closeEditMetadataModal">
        <div class="modal-panel">
          <form @submit.prevent="handleUpdateListMetadata" class="edit-list-form-modal">
  <div class="modal-header">
    <h3>Editar Detalles de la Lista</h3>
    <button type="button" @click="closeEditMetadataModal" class="modal-close-button"
      aria-label="Cerrar">&times;</button>
  </div>
  <div class="modal-body">
    <div class="form-group">
      <label for="editListName">Nombre de la Lista:</label>
      <input type="text" id="editListName" v-model="editListForm.name" required maxlength="30">
      <small class="char-counter">{{ 30 - editListForm.name.length }} caracteres restantes</small>
    </div>
    <div class="form-group">
      <label for="editListDescription">Descripción (opcional):</label>
      <textarea id="editListDescription" v-model="editListForm.description" rows="4" maxlength="200"></textarea>
      <small class="char-counter">{{ 200 - (editListForm.description || '').length }} caracteres restantes</small>
    </div>
    <div class="form-group checkbox-group">
      <input type="checkbox" id="editListIsPublic" v-model="editListForm.is_public">
      <label for="editListIsPublic">¿Hacer esta lista pública?</label>
    </div>
  </div>
  <div class="modal-footer">
    <div v-if="editMetadataMessage" :class="editMetadataError ? 'error-message' : 'success-message'"
      class="action-message modal-action-message">
      {{ editMetadataMessage }}
    </div>
    <button type="button" @click="closeEditMetadataModal" class="action-button secondary"
      :disabled="isLoadingMetadataUpdate">
      Cancelar
    </button>
    <button type="submit" :disabled="isLoadingMetadataUpdate || !editListForm.name"
      class="action-button primary">
      {{ isLoadingMetadataUpdate ? 'Guardando...' : 'Guardar Cambios' }}
    </button>
  </div>
</form>
        </div>
      </div>

      <section class="games-in-list-section section-block">
        <div class="section-header-actions">
          <h2>Juegos en "{{ gameListDetails.name }}" ({{ gameListDetails.game_count || (gameListDetails.games_in_list ?
            gameListDetails.games_in_list.length : 0) }})</h2>
          <button v-if="isOwner" @click="openAddGamesModal" class="action-button primary"
            :disabled="isLoadingActionOnGame">Añadir Juegos</button>
        </div>

        <div v-if="isLoadingActionOnGame && !showAddGamesModal && isOwner" class="loading-message small-loader">
          Actualizando lista...</div>
        <div v-else-if="!gameListDetails.games_in_list || gameListDetails.games_in_list.length === 0"
          class="empty-message list-empty">
          Esta lista aún no tiene juegos.
        </div>
        <div class="library-grid" v-else>
            <div v-for="gameEntry in gameListDetails.games_in_list" :key="gameEntry.internal_id"
  class="library-game-card">

  <RouterLink :to="{ name: 'game-details', params: { igdbId: gameEntry.game_igdb_id } }"
    class="game-card-link-content">
    
    <div class="card-cover-container">
      <img :src="getCoverUrl(gameEntry.game_cover, 'cover_big')"
        :alt="`Carátula de ${gameEntry.game_name || 'Juego'}`" class="library-game-cover"
        @error="onListGameCoverError" />

      <div v-if="gameEntry.personal_platform" class="platform-logo-on-cover">
        <img :src="getPlatformLogoUrl(gameEntry.personal_platform)" :alt="gameEntry.personal_platform" />
      </div>
    </div>

    <div class="card-content">
      <h3 class="game-title">{{ gameEntry.game_name || `Juego ID: ${gameEntry.game_igdb_id}` }}</h3>
      </div>
  </RouterLink>

  <button v-if="isOwner" @click.stop="handleRemoveGameFromList(gameEntry.internal_id)"
    class="remove-game-button" title="Quitar de la lista" :disabled="isLoadingActionOnGame">
    <span class="remove-icon">&times;</span>
  </button>
</div>
        </div>
      </section>
    </div>

    <div v-if="showAddGamesModal && isOwner" class="modal-overlay" @click.self="closeAddGamesModal">
      <div class="modal-panel add-games-modal-panel">
        <div class="modal-header">
          <h3>Añadir Juegos a "{{ gameListDetails?.name }}"</h3>
          <button type="button" @click="closeAddGamesModal" class="modal-close-button"
            aria-label="Cerrar">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="isLoadingLibraryForSelection" class="loading-message">Cargando tu biblioteca...</div>
          <div v-if="addGamesErrorMessage" class="error-message modal-error">{{ addGamesErrorMessage }}</div>

          <div v-if="!isLoadingLibraryForSelection && libraryForSelection.length === 0 && !addGamesErrorMessage"
            class="empty-message">
            No hay más juegos en tu biblioteca para añadir o ya están todos en esta lista.
          </div>

          <ul class="add-games-list" v-if="libraryForSelection.length > 0">
            <li v-for="game in libraryForSelection" :key="game.internal_id"
              :class="{ 'selected-for-add': gamesToAdd.has(game.internal_id) }"
              @click="toggleGameForAddition(game.internal_id)">
              <img :src="getCoverUrl(game.game_cover, 'cover_big')" @error="onListGameCoverError"
                class="add-game-cover-thumb" />
              <span class="add-game-name-text">{{ game.game_name || `ID: ${game.game_igdb_id}` }}</span>
              <input type="checkbox" :checked="gamesToAdd.has(game.internal_id)" @click.stop readonly
                class="add-game-checkbox" />
            </li>
          </ul>
        </div>
        <div class="modal-footer">
          <div v-if="addGamesErrorMessage && !isLoadingLibraryForSelection"
            :class="addGamesErrorMessage ? 'error-message' : ''" class="action-message modal-action-message">
            {{ addGamesErrorMessage }}
          </div>
          <button type="button" @click="closeAddGamesModal" class="action-button secondary"
            :disabled="isLoadingActionOnGame || isLoadingLibraryForSelection">
            Cancelar
          </button>
          <button @click="handleAddSelectedGamesToList"
            :disabled="isLoadingActionOnGame || isLoadingLibraryForSelection || gamesToAdd.size === 0"
            class="action-button primary">
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
import { ref, onMounted, watch, reactive, computed } from 'vue';
import { useRoute, RouterLink, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import {
  getMySpecificGameListDetails,
  viewPublicGameListDetails,
  updateMyUserGameList,
  addGameToMyGameList,
  removeGameFromMyGameList,
  getMyUserGameLibrary,
  deleteMyGameList,
  getOrCreateTierListFromGameList
} from '@/services/apiInstances';
import defaultLibraryCover from '@/assets/img/default-game-cover.svg';

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

// --- NUEVO: Estado para creación/obtención de TierList ---
const isLoadingTierListCreation = ref(false);
const tierListCreationError = ref('');

// Estado para Modal de "Añadir Juegos"
const showAddGamesModal = ref(false);
const isLoadingLibraryForSelection = ref(false);
const libraryForSelection = ref([]);
const gamesToAdd = ref(new Set()); // Set de internal_id de UserGame
const addGamesErrorMessage = ref('');
const isLoadingActionOnGame = ref(false); // Loader para añadir/quitar juego individual

const isOwner = computed(() => {
  if (!authStore.isAuthenticated || !authStore.currentUser || !gameListDetails.value) {
    return false;
  }
  return authStore.currentUser.nombre_usuario === gameListDetails.value.owner_username;
});

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
    // Intenta obtenerla como si fuera del usuario actual
    const response = await getMySpecificGameListDetails(id);
    gameListDetails.value = response.data;
    console.log("Detalles de la lista (propia) cargados:", gameListDetails.value);
  } catch (error) {
    console.warn(`No se pudo cargar la lista ${id} como propia del usuario. Intentando como pública...`, error.response?.status);
    // Si falla (ej. 403 No eres el dueño, 404 No es tuya), intenta obtenerla como pública
    if (error.response && (error.response.status === 403 || error.response.status === 404 || error.response.status === 401)) {
      try {
        const publicResponse = await viewPublicGameListDetails(id);
        gameListDetails.value = publicResponse.data;
        // Asegurarse de que realmente sea pública si el endpoint la devuelve
        if (!gameListDetails.value.is_public) {
          // Si el endpoint viewPublicGameList devuelve una lista privada (no debería según la semántica)
          errorMessageApi.value = "Esta lista no es pública.";
          gameListDetails.value = null; // No mostrarla
          router.push({ name: 'home' }); // O a una página de error
          return;
        }
        console.log("Detalles de la lista (pública de otro) cargados:", gameListDetails.value);
      } catch (publicError) {
        console.error(`Error cargando lista pública de juegos (ID: ${id}):`, publicError);
        if (publicError.response) {
          errorMessageApi.value = `Error ${publicError.response.status}: ${publicError.response.data.message || publicError.response.data.error || 'No se pudo cargar la lista pública.'}`;
        } else {
          errorMessageApi.value = 'Error de red al cargar la lista pública.';
        }
        gameListDetails.value = null;
      }
    } else { // Otro tipo de error en la primera llamada
      console.error(`Error cargando detalles de la lista de juegos (ID: ${id}):`, error);
      if (error.response) {
        errorMessageApi.value = `Error ${error.response.status}: ${error.response.data.message || error.response.data.error || 'No se pudieron cargar los detalles de la lista.'}`;
      } else {
        errorMessageApi.value = 'Error de red al cargar los detalles de la lista.';
      }
      gameListDetails.value = null;
    }
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
    description: editListForm.description,
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
    const response = await updateMyUserGameList(props.listPublicId, dto);
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

// --- NUEVO: Lógica para Crear/Obtener TierList desde GameList ---
const handleGetOrCreateTierListFromGameList = async () => {
  if (!gameListDetails.value?.public_id) {
    tierListCreationError.value = "No se puede procesar la Tier List: ID de GameList no disponible.";
    return;
  }
  isLoadingTierListCreation.value = true;
  tierListCreationError.value = '';
  try {
    // Llama a la función del servicio API que has creado o vas a crear
    // y que internamente llama a GET /api/v1/gamelists/{gameListPublicId}/tierlist
    const response = await getOrCreateTierListFromGameList(props.listPublicId);
    const tierListData = response.data; // Se asume que la respuesta es TierListResponseDTO

    if (tierListData && tierListData.public_id) {
      // Redirige a la vista de detalle de la TierList
      // Asegúrate que 'view-public-tierlist' es el nombre correcto de tu ruta para ver TierLists
      router.push({ name: 'view-public-tierlist', params: { tierListPublicId: tierListData.public_id } });
    } else {
      throw new Error("La respuesta de la API no contiene el ID público de la Tier List.");
    }
  } catch (error) {
    console.error("Error creando/obteniendo Tier List desde GameList:", error);
    if (error.response) {
      tierListCreationError.value = `Error ${error.response.status}: ${error.response.data.message || error.response.data.error || 'No se pudo procesar la Tier List.'}`;
    } else {
      tierListCreationError.value = "Error de red o inesperado al procesar la Tier List.";
    }
  } finally {
    isLoadingTierListCreation.value = false;
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

// --- Lógica para "Eliminar Lista" ---
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

// Devuelve la URL del logo de la plataforma
const getPlatformLogoUrl = (platform) => {
  if (!platform) return '';
  // Asumimos que los nombres de archivo coinciden con los valores del backend en minúsculas
  const platformName = platform.toLowerCase();
  try {
    // Esta sintaxis es necesaria para que Vite encuentre las imágenes dinámicamente
    return new URL(`/src/assets/logo-personal-platform/${platformName}.svg`, import.meta.url).href;
  } catch (error) {
    console.warn(`No se encontró el logo para la plataforma: ${platformName}.svg`);
    // Devuelve un logo genérico si no se encuentra el específico
    return new URL(`/src/assets/logo-personal-platform/other.svg`, import.meta.url).href;
  }
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
<style src="./GameListDetailView.css" scoped></style>