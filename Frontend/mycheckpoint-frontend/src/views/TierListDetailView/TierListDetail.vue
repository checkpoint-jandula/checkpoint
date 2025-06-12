<template>
  <div class="edit-tierlist-view">
    <div v-if="isLoading && !tierListDetails" class="loading-message">
      Cargando Tier List...
    </div>
    <div v-else-if="errorMessageApi" class="error-message global-error">
      {{ errorMessageApi }}
    </div>

    <div v-if="tierListDetails" class="tierlist-content-wrapper">
      <section class="tierlist-header-section section-block">
        <div class="header-content">
          <div class="header-info">
            <h1>{{ tierListDetails.name }}</h1>
            <p v-if="tierListDetails.description" class="list-description-detail">
              {{ tierListDetails.description }}
            </p>
            <p v-else class="list-description-detail-empty">
              <em>Esta tier list no tiene descripción.</em>
            </p>
            <div class="list-meta-detail">
              <span :class="[
                'status-chip',
                tierListDetails.is_public ? 'public' : 'private',
              ]">
                {{ tierListDetails.is_public ? "Pública" : "Privada" }}
              </span>
              <span class="meta-separator">|</span>
              <span>Tipo: {{ formatTierListType(tierListDetails.type) }}</span>
              <span v-if="
                tierListDetails.type === 'FROM_GAMELIST' &&
                tierListDetails.source_game_list_public_id
              " class="meta-separator">|</span>
              <span v-if="
                tierListDetails.type === 'FROM_GAMELIST' &&
                tierListDetails.source_game_list_public_id
              ">
                <RouterLink :to="{
                  name: 'gamelist-detail',
                  params: {
                    listPublicId: tierListDetails.source_game_list_public_id,
                  },
                }">
                  Ver GameList Origen
                </RouterLink>
              </span>
              <span class="meta-separator">|</span>
              <span>Creada por: {{ tierListDetails.owner_username }}</span>
              <span v-if="tierListDetails.updated_at" class="meta-separator">|</span>
              <span v-if="tierListDetails.updated_at">
                Última act.:
                {{ formatReadableDate(tierListDetails.updated_at) }}
              </span>
            </div>
          </div>
          <div v-if="isOwner" class="header-actions-tierlist">
            <button v-if="isEditableTierList" @click="openEditTierListMetadataModal" class="action-button secondary">
              Editar Detalles
            </button>
            <button @click="handleDeleteTierList" :disabled="isLoading" class="action-button secondary">
              Eliminar Tier List
            </button>
          </div>
        </div>
      </section>

      <div class="tier-maker-container section-block">
        <div class="add-section-controls" v-if="isOwner">
          <button @click="openAddSectionModal" class="action-button secondary">
            Añadir Nueva Tier (Fila)
          </button>
        </div>

        <div v-for="section in sortedCustomSections" :key="section.internal_id" class="tier-row">
          <div class="tier-label" :style="{ backgroundColor: section.color || getTierColor(section.name) }">
            <span v-if="
              !editingSectionName || editingSectionId !== section.internal_id
            ">{{ section.name }}</span>
            <input v-if="
              isOwner &&
              editingSectionName &&
              editingSectionId === section.internal_id &&
              !section.is_default_unclassified
            " v-model="currentSectionNameEdit" @keyup.enter="saveSectionName(section)"
              @blur="saveSectionName(section.internal_id)" @keyup.esc="cancelEditSectionName" class="section-name-input"
              v-focus />
            <div v-if="isOwner && !section.is_default_unclassified" class="tier-actions">
              <button @click="startEditSectionName(section)" class="icon-button" title="Editar nombre tier">
                &#9998;
              </button>
              <label class="icon-button" title="Cambiar color">
                🎨
                <input type="color" v-model="section.color" @change="handleUpdateSectionDetails(section)" class="color-input-hidden" />
              </label>
              <button @click="confirmRemoveSection(section.internal_id)" class="icon-button danger"
                title="Eliminar tier">
                &times;
              </button>
            </div>
          </div>
          <div class="tier-items-droppable-area" :class="{
            'drag-over-active': dragOverSectionId === section.internal_id && dragOverItemId === null && isOwner,
            'can-drop': isOwner && draggedItemInfo /* Añade clase si se puede soltar */
          }" @dragover.prevent="isOwner ? handleDragOver($event, section.internal_id) : null"
            @dragleave="isOwner ? handleDragLeaveSection($event, section.internal_id) : null"
            @drop="isOwner ? handleDropOnSection($event, section.internal_id) : null">
            <div v-if="!section.items || section.items.length === 0" class="tier-empty-placeholder">
              {{ isOwner ? "Arrastra o añade juegos aquí" : "(Vacío)" }}
            </div>
            <div v-else class="tier-items-grid-horizontal">
              <div v-for="(item, index) in section.items" :key="item.tier_list_item_id" class="tier-item-compact"
                :class="{ 'drag-over-item-target': dragOverItemId === item.tier_list_item_id && isOwner }"
                :draggable="isOwner" @dragstart="isOwner ? handleDragStart($event, item, section.internal_id) : null"
                @dragend="isOwner ? handleDragEnd($event) : null"
                @dragover.prevent.stop="isOwner ? handleDragOver($event, section.internal_id, item.tier_list_item_id) : null"
                @dragleave="isOwner ? handleDragLeaveItemOrSection($event) : null"
                @drop.stop="isOwner ? handleDrop($event, section.internal_id, item.tier_list_item_id) : null">
                <RouterLink :to="{
                  name: 'game-details',
                  params: { igdbId: item.game_igdb_id },
                }" :title="item.game_name">
                  <img :src="getItemCoverUrl(item.game_cover_url, 'cover_big')" :alt="item.game_name"
                    class="tier-item-cover-compact" @error="onTierItemCoverError" />
                </RouterLink>
                <button v-if="isEditableTierList" @click.stop="handleRemoveItemFromTierList(item.tier_list_item_id)"
                  class="remove-item-button-compact" title="Quitar ítem de la tier list">
                  &times;
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="tier-row unclassified-tier-row" v-if="
          tierListDetails.unclassified_section ||
          (isEditableTierList &&
              /*lógica para mostrar juegos de la biblioteca no añadidos*/ true)
        ">
          <div class="tier-label unclassified-label">
            <span>{{
              tierListDetails.unclassified_section
                ? tierListDetails.unclassified_section.name
                : "Juegos Disponibles"
            }}</span>
            <button v-if="isEditableTierList" @click="openAddGamesToUnclassifiedModal"
              class="action-button-small tertiary" title="Añadir juegos de tu biblioteca a esta Tier List">
              Añadir Juegos de Biblioteca
            </button>
          </div>
          <div class="tier-items-droppable-area" :class="{
            'drag-over-active': dragOverSectionId === tierListDetails.unclassified_section.internal_id && dragOverItemId === null && isOwner,
            'can-drop': isOwner && draggedItemInfo
          }"
            @dragover.prevent="isOwner ? handleDragOver($event, tierListDetails.unclassified_section.internal_id) : null"
            @dragleave="isOwner ? handleDragLeaveSection($event, tierListDetails.unclassified_section.internal_id) : null"
            @drop="isOwner ? handleDropOnSection($event, tierListDetails.unclassified_section.internal_id) : null">
            <div v-if="
              !tierListDetails.unclassified_section ||
              !tierListDetails.unclassified_section.items ||
              tierListDetails.unclassified_section.items.length === 0
            " class="tier-empty-placeholder">
              {{
                tierListDetails.type === "FROM_GAMELIST"
                  ? "Arrastra juegos aquí o añádelos desde tu biblioteca."
                  : "(Vacìo)"
              }}
            </div>
            <div v-else class="tier-items-grid-horizontal">
              <div v-for="item in tierListDetails.unclassified_section.items" :key="item.tier_list_item_id"
                class="tier-item-compact"
                :class="{ 'drag-over-item-target': dragOverItemId === item.tier_list_item_id && isOwner }"
                :draggable="isOwner"
                @dragstart="isOwner ? handleDragStart($event, item, tierListDetails.unclassified_section.internal_id) : null"
                @dragend="isOwner ? handleDragEnd($event) : null"
                @dragover.prevent.stop="isOwner ? handleDragOver($event, tierListDetails.unclassified_section.internal_id, item.tier_list_item_id) : null"
                @dragleave="isOwner ? handleDragLeaveItemOrSection($event) : null"
                @drop.stop="isOwner ? handleDrop($event, tierListDetails.unclassified_section.internal_id, item.tier_list_item_id) : null">
                <RouterLink :to="{
                  name: 'game-details',
                  params: { igdbId: item.game_igdb_id },
                }" :title="item.game_name">
                  <img :src="getItemCoverUrl(item.game_cover_url, 'cover_big')" :alt="item.game_name"
                    class="tier-item-cover-compact" @error="onTierItemCoverError" />
                </RouterLink>
                <button v-if="isEditableTierList" @click.stop="
                  handleRemoveItemFromTierList(item.tier_list_item_id)
                  " class="remove-item-button-compact" title="Quitar ítem de la tier list (si es PROFILE_GLOBAL)">
                  &times;
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showAddGamesToUnclassifiedModal && isEditableTierList" class="modal-overlay"
      @click.self="closeAddGamesToUnclassifiedModal">
      <div class="modal-panel add-games-modal-panel">
        <div class="modal-header">
          <h3>Añadir Juegos a "{{ tierListDetails?.unclassified_section?.name || 'Sin Clasificar' }}"</h3>
          <button type="button" @click="closeAddGamesToUnclassifiedModal" class="modal-close-button"
            aria-label="Cerrar">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="isLoadingLibraryForSelection" class="loading-message">Cargando tu biblioteca...</div>
          <div v-if="addItemsErrorMessage" class="error-message modal-error">{{ addItemsErrorMessage }}</div>

          <div v-if="!isLoadingLibraryForSelection && libraryForSelection.length === 0 && !addItemsErrorMessage"
            class="empty-message">
            No hay juegos en tu biblioteca para añadir (o ya están todos en la Tier List).
          </div>

          <ul class="add-games-list" v-if="libraryForSelection.length > 0">
            <li v-for="game in libraryForSelection" :key="game.internal_id"
              :class="{ 'selected-for-add': gamesToAdd.has(game.internal_id) }"
              @click="toggleGameForAdditionInternal(game.internal_id)"> <img
                :src="getItemCoverUrl(game.game_cover.url, 'cover_big')" :alt="game.game_name"
                class="add-game-cover-thumb" @error="onTierItemCoverError" />
              <span class="add-game-name-text">{{ game.game_name || `ID: ${game.game_igdb_id}` }}</span>
              <input type="checkbox" :checked="gamesToAdd.has(game.internal_id)" @click.stop readonly
                class="add-game-checkbox" />
            </li>
          </ul>
        </div>
        <div class="modal-footer">
          <div v-if="addItemsErrorMessage && !isLoadingLibraryForSelection"
            class="action-message modal-action-message error-message">
            {{ addItemsErrorMessage }}
          </div>
          <button type="button" @click="closeAddGamesToUnclassifiedModal" class="action-button secondary"
            :disabled="isLoadingTierItemAction || isLoadingLibraryForSelection">
            Cancelar
          </button>
          <button @click="handleAddSelectedGamesToUnclassified"
            :disabled="isLoadingTierItemAction || isLoadingLibraryForSelection || gamesToAdd.size === 0"
            class="action-button primary">
            {{ isLoadingTierItemAction ? 'Añadiendo...' : `Añadir (${gamesToAdd.size})` }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="!isLoading && !tierListDetails && errorMessageApi" class="no-results-message">
      No se pudo cargar la Tier List o no es accesible.
    </div>
    <div v-if="showEditTierListMetadataModal && isEditableTierList" class="modal-overlay"
      @click.self="closeEditTierListMetadataModal">
      <div class="modal-panel">
        <form @submit.prevent="handleUpdateTierListMetadata" class="edit-tierlist-form-modal">
          <div class="modal-header">
            <h3>Editar Detalles de la Tier List</h3>
            <button type="button" @click="closeEditTierListMetadataModal" class="modal-close-button"
              aria-label="Cerrar">
              &times;
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label for="editTierListName">Nombre de la Tier List:</label>
              <input type="text" id="editTierListName" v-model="editTierListForm.name" required maxlength="150" />
            </div>
            <div class="form-group">
              <label for="editTierListDescription">Descripción (opcional):</label>
              <textarea id="editTierListDescription" v-model="editTierListForm.description" rows="3"
                maxlength="1000"></textarea>
            </div>
            <div class="form-group checkbox-group">
              <input type="checkbox" id="editTierListIsPublic" v-model="editTierListForm.is_public" />
              <label for="editTierListIsPublic">¿Hacer esta Tier List pública?</label>
            </div>
          </div>
          <div class="modal-footer">
            <div v-if="editTierListMetadataMessage" :class="editTierListMetadataError ? 'error-message' : 'success-message'
              " class="action-message modal-action-message">
              {{ editTierListMetadataMessage }}
            </div>
            <button type="button" @click="closeEditTierListMetadataModal" class="action-button secondary"
              :disabled="isLoadingTierListMetadataUpdate">
              Cancelar
            </button>
            <button type="submit" :disabled="isLoadingTierListMetadataUpdate || !editTierListForm.name.trim()
              " class="action-button primary">
              {{
                isLoadingTierListMetadataUpdate
                  ? "Guardando..."
                  : "Guardar Cambios"
              }}
            </button>
          </div>
        </form>
      </div>
    </div>
    <div v-if="showAddSectionModal && isOwner" class="modal-overlay" @click.self="closeAddSectionModal">
      <div class="modal-panel add-section-modal-panel">
        <form @submit.prevent="handleAddSection" class="add-section-form">
          <div class="modal-header">
            <h3>Añadir Nueva Tier</h3>
            <button type="button" @click="closeAddSectionModal" class="modal-close-button" aria-label="Cerrar">
              &times;
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label for="newSectionName">Nombre de la Tier:</label>
              <input type="text" id="newSectionName" v-model="newSectionForm.name" required maxlength="100" />
              <small>Ej: S, A, Buenos, Mis Favoritos...</small>
            </div>
            <div v-if="addSectionErrorMessage" class="error-message modal-error">
              {{ addSectionErrorMessage }}
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" @click="closeAddSectionModal" class="action-button secondary"
              :disabled="isAddingSection">
              Cancelar
            </button>
            <button type="submit" :disabled="isAddingSection || !newSectionForm.name.trim()"
              class="action-button primary">
              {{ isAddingSection ? "Añadiendo..." : "Añadir Tier" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, watch, reactive, computed, nextTick } from "vue";
import { useRoute, RouterLink, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/authStore";
import {
  getTierListDetailsByPublicId,
  updateMyTierListMetadata,
  deleteMyTierList,
  addSectionToMyTierList,
  updateMyTierSection,
  removeSectionFromMyTierList,
  addItemToMyUnclassifiedSection,
  removeItemFromMyTierList,
  moveItemInMyTierList,
  getMyUserGameLibrary,
} from "@/services/apiInstances";
import defaultTierItemCover from "@/assets/img/default-game-cover.png"; // Placeholder para ítems de tier

const props = defineProps({
  tierListPublicId: {
    type: String,
    required: true,
  },
});

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const tierListDetails = ref(null); // TierListResponseDTO
const isLoading = ref(true);
const errorMessageApi = ref("");

// --- ESTADO PARA DRAG AND DROP ---
const draggedItemInfo = ref(null); // { tierListItemId: number, originalSectionId: number | null, originalOrder: number }
const dragOverSectionId = ref(null); // Para feedback visual
const dragOverItemId = ref(null); // NUEVO: Para saber sobre qué ítem se está arrastrando


// --- Estado para editar metadatos de TierList ---
const showEditTierListMetadataModal = ref(false);
const isLoadingTierListMetadataUpdate = ref(false);
const editMetadataMessage = ref(""); // <--- DEFINICIÓN
const editMetadataError = ref(false); // <--- DEFINICIÓN
const editTierListMetadataError = ref(false);
const editTierListMetadataMessage = ref(""); // <--- DEFINICIÓN
const editTierListForm = reactive({
  name: "",
  description: null,
  is_public: false,
});

// --- Estado para editar nombre de sección ---
const editingSectionName = ref(false);
const editingSectionId = ref(null);
const currentSectionNameEdit = ref("");

// Variables de estado para mensajes de error/éxito específicos de la edición de sección
const sectionEditMessage = ref("");
const sectionEditError = ref(false);
const isLoadingSectionAction = ref(false);

// --- NUEVO: Estado para Modal de "Añadir Sección" ---
const showAddSectionModal = ref(false);
const newSectionForm = reactive({ name: "", color: "" }); // TierSectionRequestDTO tiene 'name'
const isAddingSection = ref(false);
const addSectionErrorMessage = ref("");

// --- ESTADO PARA MODAL DE AÑADIR JUEGOS (ahora específico para "Sin Clasificar") ---
const showAddGamesToUnclassifiedModal = ref(false); // Nuevo nombre para claridad
const isLoadingLibraryForSelection = ref(false); //
const libraryForSelection = ref([]);             //
const gamesToAdd = ref(new Set());      // Set de internal_id de UserGame a añadir
const addItemsErrorMessage = ref('');           // (Renombrado de addGamesErrorMessage)
const isLoadingTierItemAction = ref(false);     // (Renombrado de isLoadingActionOnGame)

const isOwner = computed(() => {
  if (
    !authStore.isAuthenticated ||
    !authStore.currentUser ||
    !tierListDetails.value
  ) {
    return false;
  }
  return (
    authStore.currentUser.nombre_usuario ===
    tierListDetails.value.owner_username
  );
});

const isEditableTierList = computed(() => {
  return isOwner.value && tierListDetails.value?.type === "PROFILE_GLOBAL";
});

const sortedCustomSections = computed(() => {
  if (tierListDetails.value && tierListDetails.value.sections) {
    return [...tierListDetails.value.sections]
      .filter((section) => !section.is_default_unclassified)
      .sort((a, b) => (a.order || 0) - (b.order || 0));
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
  errorMessageApi.value = "";
  tierListDetails.value = null;
  try {
    const response = await getTierListDetailsByPublicId(id);
    tierListDetails.value = response.data;
    console.log(
      "Detalles de la Tier List cargados:",
      JSON.parse(JSON.stringify(tierListDetails.value))
    );
  } catch (error) {
    console.error(
      `Error cargando detalles de la Tier List (ID: ${id}):`,
      error
    );
    if (error.response) {
      errorMessageApi.value = `Error ${error.response.status}: ${error.response.data.message ||
        error.response.data.error ||
        "No se pudieron cargar los detalles."
        }`;
      if (error.response.status === 403 || error.response.status === 404) {
        router.push({ name: "home" });
      }
    } else {
      errorMessageApi.value = "Error de red al cargar los detalles.";
    }
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchTierListDetails(props.tierListPublicId);
});

watch(
  () => props.tierListPublicId,
  (newId, oldId) => {
    if (newId && newId !== oldId) {
      fetchTierListDetails(newId);
    }
  }
);

watch(
  () => newSectionForm.name,
  (newName) => {
    // Cuando el usuario escribe el nombre de la nueva sección,
    // actualizamos el color sugerido en tiempo real.
    newSectionForm.color = getTierColor(newName);
  }
);


const vFocus = {
  mounted: (el) => {
    // `nextTick` espera a que Vue haya actualizado el DOM
    nextTick(() => {
      el.focus();
    });
  },
  // Podrías necesitar 'updated' si el input se reutiliza y solo cambia su visibilidad,
  // pero con v-if, 'mounted' debería ser suficiente cuando el input se crea.
  updated: (el, binding) => {
    if (binding.value && binding.oldValue !== binding.value) {
      nextTick(() => {
        el.focus();
      });
    }
  },
};

// --- Lógica para Editar Metadatos de la Tier List ---
const openEditTierListMetadataModal = () => {
  if (!isEditableTierList.value || !tierListDetails.value) {
    alert(
      "Esta Tier List no puede editar sus metadatos (no eres el propietario o no es de tipo 'Perfil Global')."
    );
    return;
  }
  editTierListForm.name = tierListDetails.value.name || "";
  editTierListForm.description = tierListDetails.value.description || null;
  editTierListForm.is_public = tierListDetails.value.is_public || false;
  // Resetear mensajes del modal
  editMetadataMessage.value = ""; // <--- Se usa aquí
  editMetadataError.value = false; // <--- Se usa aquí
  editTierListMetadataMessage.value = ""; // Resetea el mensaje
  editTierListMetadataError.value = false; // Resetea el estado de error
  showEditTierListMetadataModal.value = true;
};

const closeEditTierListMetadataModal = () => {
  showEditTierListMetadataModal.value = false;
};

const handleUpdateTierListMetadata = async () => {
  if (!isEditableTierList.value) {
    editTierListMetadataMessage.value =
      "No tienes permiso para editar esta Tier List";
    editTierListMetadataError.value = true;
    return;
  }
  if (!editTierListForm.name || editTierListForm.name.trim() === "") {
    editTierListMetadataMessage.value =
      "El nombre de la Tier List es obligatorio.";
    editTierListMetadataError.value = true;
    return;
  }
  isLoadingTierListMetadataUpdate.value = true;
  editTierListMetadataMessage.value = "";
  editTierListMetadataError.value = false;

  const requestDTO = {
    name: editTierListForm.name,
    description: editTierListForm.description || null,
    is_public: editTierListForm.is_public,
  };

  const changes = {};
  let hasChanges = false;
  if (requestDTO.name !== tierListDetails.value.name) {
    changes.name = requestDTO.name;
    hasChanges = true;
  }
  if (requestDTO.description !== (tierListDetails.value.description || null)) {
    changes.description = requestDTO.description;
    hasChanges = true;
  }
  if (requestDTO.is_public !== tierListDetails.value.is_public) {
    changes.is_public = requestDTO.is_public;
    hasChanges = true;
  }

  if (!hasChanges) {
    editTierListMetadataMessage.value = "No se han realizado cambios.";
    editTierListMetadataError.value = false;
    isLoadingTierListMetadataUpdate.value = false;
    return;
  }

  try {
    const response = await updateMyTierListMetadata(
      props.tierListPublicId,
      requestDTO
    );
    tierListDetails.value = response.data;
    editTierListMetadataMessage.value =
      "¡Detalles de la Tier List actualizados!";
    editTierListMetadataError.value = false;
    setTimeout(() => closeEditTierListMetadataModal(), 1500);
  } catch (error) {
    console.error("Error actualizando metadatos de la Tier List:", error);
    editTierListMetadataError.value = true;
    if (error.response?.data) {
      editTierListMetadataMessage.value =
        error.response.data.errors?.join(", ") ||
        error.response.data.message ||
        error.response.data.error ||
        "No se pudo actualizar la Tier List.";
    } else {
      editTierListMetadataMessage.value =
        "Error de red al actualizar la Tier List.";
    }
  } finally {
    isLoadingTierListMetadataUpdate.value = false;
  }
};

// --- Lógica para Eliminar Tier List ---
const handleDeleteTierList = async () => {
  if (!tierListDetails.value || !isOwner.value) {
    alert("No puedes eliminar esta Tier List.");
    return;
  }
  if (
    window.confirm(
      `¿Estás seguro de que quieres eliminar la Tier List "${tierListDetails.value.name}"? Esta acción no se puede deshacer.`
    )
  ) {
    isLoading.value = true;
    errorMessageApi.value = "";
    try {
      console.log(
        "Intentando eliminar Tier List con listPublicId:",
        props.tierListPublicId
      );
      if (!props.tierListPublicId) {
        errorMessageApi.value =
          "Error: ID de Tier List no disponible para eliminación.";
        isLoading.value = false;
        return;
      }
      await deleteMyTierList(props.tierListPublicId);
      alert("Tier List eliminada exitosamente.");
      if (authStore.currentUser?.public_id) {
        router.push({
          name: "profile",
          params: { publicId: authStore.currentUser.public_id },
          query: { tab: "my-tierlists" },
        });
      } else {
        router.push({ name: "home" });
      }
    } catch (error) {
      console.error("Error eliminando la Tier List:", error);
      if (error.response?.data) {
        errorMessageApi.value =
          error.response.data.message ||
          error.response.data.error ||
          "No se pudo eliminar la Tier List.";
      } else if (error.message && error.message.includes("status code 404")) {
        errorMessageApi.value =
          "No se pudo eliminar la Tier List: Recurso no encontrado (404).";
      } else {
        errorMessageApi.value =
          "Error de red o inesperado al eliminar la Tier List.";
      }
    } finally {
      isLoading.value = false;
    }
  }
};

// --- LÓGICA PARA EDITAR SECCIONES ---
const startEditSectionName = (section) => {
  if (!isOwner.value) return; 
  editingSectionId.value = section.internal_id;
  currentSectionNameEdit.value = section.name;
  editingSectionName.value = true;
  sectionEditMessage.value = ""; 
  sectionEditError.value = false;
  // El foco se maneja con v-focus en el template
};

const cancelEditSectionName = () => {
  editingSectionName.value = false;
  editingSectionId.value = null;
  currentSectionNameEdit.value = "";
  sectionEditMessage.value = "";
  sectionEditError.value = false;
};

const handleUpdateSectionDetails = async (section) => {
  if (!isOwner.value || section.is_default_unclassified || !section.internal_id) {
    console.error("Actualización prevenida: Sin permisos, o la sección o su ID no son válidos.");
    cancelEditSectionName();
    return;
  }

  // Determina el nombre final.
  const newName = (editingSectionId.value === section.internal_id)
    ? currentSectionNameEdit.value.trim()
    : section.name;

  if (newName === "") {
    alert("El nombre de la sección no puede estar vacío.");
    return;
  }

  // Salir del modo edición de texto
  if (editingSectionId.value === section.internal_id) {
    editingSectionName.value = false;
    editingSectionId.value = null;
  }

  const requestDTO = { 
    name: newName,
    color: section.color || '#CCCCCC' // Aseguramos que el color nunca es nulo
  };

  console.log('Enviando actualización para sección ID:', section.internal_id, 'con datos:', JSON.stringify(requestDTO));
  isLoadingSectionAction.value = true;
  sectionEditMessage.value = "";

  try {
    const response = await updateMyTierSection(
      props.tierListPublicId,
      section.internal_id, // Usamos la propiedad correcta: 'internal_id'
      requestDTO
    );
    
    tierListDetails.value = response.data;
    sectionEditMessage.value = "Sección actualizada.";
    setTimeout(() => { sectionEditMessage.value = "" }, 3000);

  } catch (error) {
    console.error(`Error actualizando sección ID ${section.internal_id}:`, error);
    const errorMessage = error.response?.data?.message || "No se pudo actualizar la sección.";
    sectionEditError.value = true;
    sectionEditMessage.value = errorMessage;
  } finally {
    isLoadingSectionAction.value = false;
  }
};

const saveSectionName = (section) => {
  handleUpdateSectionDetails(section);
};

// --- LÓGICA ACTUALIZADA PARA ELIMINAR SECCIÓN ---
const confirmRemoveSection = async (sectionId) => {
  // La edición/eliminación de secciones está disponible para ambos tipos si es el propietario
  if (!isOwner.value) {
    alert("Solo el propietario puede eliminar secciones.");
    return;
  }

  const section = tierListDetails.value?.sections?.find(
    (s) => s.internal_id === sectionId
  );
  if (!section) {
    alert("Sección no encontrada.");
    return;
  }
  // No se puede eliminar la sección por defecto "Sin Clasificar" (esta lógica ya previene que aparezca en sortedCustomSections)
  // Adicionalmente, la API no permite eliminar la última sección personalizable.
  if (sortedCustomSections.value.length <= 1) {
    alert(
      "No puedes eliminar la última sección. Una Tier List debe tener al menos una tier personalizable."
    );
    return;
  }

  if (
    window.confirm(
      `¿Seguro que quieres eliminar la tier "${section.name}"? Los juegos en ella se moverán a "Sin Clasificar".`
    )
  ) {
    isLoadingSectionAction.value = true; // Usar un loader específico para acciones de sección/item
    errorMessageApi.value = ""; // Limpiar errores globales
    try {
      const response = await removeSectionFromMyTierList(
        props.tierListPublicId,
        sectionId
      );
      tierListDetails.value = response.data; // La API devuelve la TierList actualizada
      // Opcionalmente, podrías mostrar un mensaje de éxito temporal
    } catch (error) {
      console.error(`Error eliminando sección ID ${sectionId}:`, error);
      if (error.response?.data) {
        // Mostrar error en un lugar visible, tal vez un toast o un ref específico para errores de sección
        alert(
          `Error: ${error.response.data.message ||
          error.response.data.error ||
          "No se pudo eliminar la sección."
          }`
        );
      } else {
        alert("Error de red al eliminar la sección.");
      }
    } finally {
      isLoadingSectionAction.value = false;
    }
  }
};

// --- NUEVA: Lógica para Añadir Nueva Sección (Tier) ---
const openAddSectionModal = () => {
  if (!isOwner.value) {
    // Solo para PROFILE_GLOBAL y si es dueño
    alert("Solo se pueden añadir secciones a Tier Lists que te pertenezcan.");
    return;
  }
  newSectionForm.name = ""; // Resetear el nombre
  newSectionForm.color = "#CCCCCC";
  addSectionErrorMessage.value = "";
  showAddSectionModal.value = true;
};

const closeAddSectionModal = () => {
  showAddSectionModal.value = false;
};

const handleAddSection = async () => {
  if (!newSectionForm.name || newSectionForm.name.trim() === "") {
    addSectionErrorMessage.value = "El nombre de la sección es obligatorio.";
    return;
  }

  isAddingSection.value = true;
  addSectionErrorMessage.value = "";

   const requestDTO = { 
      name: newSectionForm.name.trim(),
      color: newSectionForm.color 
  }; // TierSectionRequestDTO

  try {
    const response = await addSectionToMyTierList(
      props.tierListPublicId,
      requestDTO
    );
    tierListDetails.value = response.data; // La API devuelve la TierList actualizada
    closeAddSectionModal();
  } catch (error) {
    console.error("Error añadiendo sección a la Tier List:", error);
    if (error.response?.data) {
      addSectionErrorMessage.value =
        error.response.data.errors?.join(", ") ||
        error.response.data.message ||
        error.response.data.error ||
        "No se pudo añadir la sección.";
    } else {
      addSectionErrorMessage.value = "Error de red al añadir la sección.";
    }
  } finally {
    isAddingSection.value = false;
  }
};

// --- LÓGICA ACTUALIZADA PARA AÑADIR JUEGOS A "SIN CLASIFICAR" ---
const fetchLibraryForUnclassifiedSelection = async () => { // Nombre más específico
  isLoadingLibraryForSelection.value = true;
  addItemsErrorMessage.value = '';
  libraryForSelection.value = [];
  try {
    const libraryResponse = await getMyUserGameLibrary();
    if (!libraryResponse.data) throw new Error("No se recibieron datos de la biblioteca.");

    const allItemsInTierListSet = new Set();
    tierListDetails.value?.unclassified_section?.items?.forEach(item => allItemsInTierListSet.add(item.user_game_id)); //
    tierListDetails.value?.sections?.forEach(section => {
      section.items?.forEach(item => allItemsInTierListSet.add(item.user_game_id));
    });

    console.log("[AddGamesToUnclassifiedModal] UserGame IDs ya en la Tier List:", Array.from(allItemsInTierListSet));

    libraryForSelection.value = libraryResponse.data.filter(libraryGame => {
      // Asumimos que libraryGame (UserGameResponseDTO) tiene 'internal_id' que es el 'user_game_id'
      const hasValidId = libraryGame.internal_id != null;
      const notAlreadyInTierList = !allItemsInTierListSet.has(libraryGame.internal_id);
      return hasValidId && notAlreadyInTierList;
    });
    console.log("[AddGamesToUnclassifiedModal] Juegos de biblioteca disponibles:", JSON.parse(JSON.stringify(libraryForSelection.value)));

    if (libraryForSelection.value.length === 0) {
      addItemsErrorMessage.value = libraryResponse.data.length > 0 ?
        "Todos los juegos de tu biblioteca ya están en esta Tier List o falta información de ID." :
        "No hay juegos en tu biblioteca para añadir.";
    }
  } catch (error) { /* ... (manejo de error como antes) ... */ }
  finally { isLoadingLibraryForSelection.value = false; }
};

const openAddGamesToUnclassifiedModal = () => {
  if (!isEditableTierList.value) {
    alert("Solo se pueden añadir juegos a Tier Lists de tipo 'Perfil Global' que te pertenezcan.");
    return;
  }
  gamesToAdd.value.clear();
  fetchLibraryForUnclassifiedSelection(); // Llama a la función específica
  showAddGamesToUnclassifiedModal.value = true; // Usa el ref específico para este modal
};

const closeAddGamesToUnclassifiedModal = () => { // Función específica para cerrar
  showAddGamesToUnclassifiedModal.value = false;
};

const toggleGameForAdditionInternal = (userGameInternalId) => { // Renombrada para evitar confusión con la del template
  if (!userGameInternalId) return;
  if (gamesToAdd.value.has(userGameInternalId)) {
    gamesToAdd.value.delete(userGameInternalId);
  } else {
    gamesToAdd.value.add(userGameInternalId);
  }
};

const handleAddSelectedGamesToUnclassified = async () => { // Nombre específico
  if (gamesToAdd.value.size === 0) {
    addItemsErrorMessage.value = "Selecciona al menos un juego."; return;
  }
  isLoadingTierItemAction.value = true;
  addItemsErrorMessage.value = '';

  const promises = [];
  gamesToAdd.value.forEach(userGameId => {
    const dto = { user_game_id: userGameId, order: null }; // TierListItemAddRequestDTO
    promises.push(addItemToMyUnclassifiedSection(props.tierListPublicId, dto)); //
  });

  try {
    const results = await Promise.allSettled(promises);
    const someSucceeded = results.some(result => result.status === 'fulfilled');
    if (someSucceeded) {
      await fetchTierListDetails(props.tierListPublicId);
      closeAddGamesToUnclassifiedModal();
    }
    const failedCount = results.filter(result => result.status === 'rejected').length;
    if (failedCount > 0) {
      addItemsErrorMessage.value = `No se pudieron añadir ${failedCount} juego(s).`;
    }
  } catch (error) { /* ... (manejo de error como antes) ... */ }
  finally { isLoadingTierItemAction.value = false; }
};

// --- LÓGICA ACTUALIZADA PARA QUITAR ÍTEM DE TIER LIST (PROFILE_GLOBAL) ---
const handleRemoveItemFromTierList = async (tierListItemId) => {
  // Esta acción solo está permitida para Tier Lists de tipo PROFILE_GLOBAL y si el usuario es el propietario.
  if (!isEditableTierList.value) {
    alert("Solo puedes quitar ítems directamente de Tier Lists de tipo 'Perfil Global' que te pertenezcan.");
    return;
  }
  if (!tierListItemId) {
    console.error("ID del ítem de la tier list no proporcionado.");
    // Podrías mostrar un mensaje de error al usuario aquí si lo deseas
    return;
  }

  if (window.confirm("¿Estás seguro de que quieres quitar este juego de la tier list?")) {
    isLoadingTierItemAction.value = true;
    // Podrías tener un mensaje específico para esta acción si quieres
    // editTierListMetadataMessage.value = ''; 
    // editTierListMetadataError.value = false;
    try {
      console.log(`Quitando ítem con ID ${tierListItemId} de la Tier List ${props.tierListPublicId}`);
      const response = await removeItemFromMyTierList(props.tierListPublicId, tierListItemId);
      tierListDetails.value = response.data; // La API devuelve la TierList actualizada
      // alert("Ítem quitado de la Tier List."); // O un mensaje de toast/notificación
    } catch (error) {
      console.error(`Error quitando ítem ${tierListItemId} de la Tier List:`, error);
      // Mostrar error al usuario
      alert(`Error: ${error.response?.data?.message || error.response?.data?.error || 'No se pudo quitar el ítem.'}`);
    } finally {
      isLoadingTierItemAction.value = false;
    }
  }
};

// --- LÓGICA DE DRAG AND DROP ---
const handleDragStart = (event, item, originalSectionInternalId) => {
  if (!isOwner.value) { event.preventDefault(); return; }
  event.dataTransfer.effectAllowed = 'move';
  // Guardamos el ID del TierListItem y su sección original
  event.dataTransfer.setData('application/json', JSON.stringify({
    tierListItemId: item.tier_list_item_id,
    originalSectionId: originalSectionInternalId
  }));
  draggedItemInfo.value = {
    tierListItemId: item.tier_list_item_id,
    originalSectionId: originalSectionInternalId
  };
  event.target.closest('.tier-item-compact')?.classList.add('dragging-item');
};

// Ahora handleDragOver puede recibir un itemId si se está sobre un ítem
const handleDragOver = (event, targetSectionId, targetItemId = null) => {
  if (!isOwner.value || !draggedItemInfo.value) return;
  event.preventDefault();
  event.dataTransfer.dropEffect = 'move';
  dragOverSectionId.value = targetSectionId;
  dragOverItemId.value = targetItemId; // Guardar sobre qué ítem estamos
};

const handleDragLeaveItemOrSection = (event) => {
  // Lógica simple para quitar el feedback visual.
  // Puede necesitar ser más robusta si hay muchos elementos anidados.
  // Si el relatedTarget (hacia dónde va el mouse) no es parte del área de drop, reseteamos.
  if (event.relatedTarget && !event.currentTarget.contains(event.relatedTarget)) {
    dragOverSectionId.value = null;
    dragOverItemId.value = null;
  } else if (!event.relatedTarget) { // A veces relatedTarget es null al salir del navegador o a un iframe
    dragOverSectionId.value = null;
    dragOverItemId.value = null;
  }
};

const handleDrop = async (event, targetSectionId, beforeItemId = null) => {
  event.preventDefault();
  if (!draggedItemInfo.value || !isOwner.value) return;

  const draggedData = JSON.parse(event.dataTransfer.getData('application/json'));
  const tierListItemIdToMove = draggedData.tierListItemId;
  const originalSectionId = draggedData.originalSectionId;

  let targetSection = tierListDetails.value?.sections?.find(s => s.internal_id === targetSectionId);
  if (!targetSection) {
    if (tierListDetails.value?.unclassified_section?.internal_id === targetSectionId) {
      targetSection = tierListDetails.value.unclassified_section;
    } else {
      console.error("Sección destino no encontrada en drop");
      handleDragEnd(); return;
    }
  }

  let newOrder = 0;
  const itemsInTargetSection = targetSection.items || [];

  if (beforeItemId !== null) {
    // Se soltó sobre un ítem específico, encontrar su orden para insertar antes
    const beforeItemIndex = itemsInTargetSection.findIndex(item => item.tier_list_item_id === beforeItemId);
    if (beforeItemIndex !== -1) {
      // Si el ítem arrastrado viene de la misma sección y se mueve ANTES de su posición original, el índice no cambia.
      // Si se mueve DESPUÉS de su posición original, el índice es el del 'beforeItem'.
      // Esta lógica puede ser compleja y a menudo el backend la maneja mejor.
      // Por ahora, asumimos que el backend reordena si le damos el índice del elemento "antes de".
      newOrder = itemsInTargetSection[beforeItemIndex].item_order;
    } else {
      // No se encontró el item 'before', añadir al final
      newOrder = itemsInTargetSection.length;
    }
  } else {
    // Se soltó en el área de la sección, no sobre un ítem específico -> añadir al final
    newOrder = itemsInTargetSection.length;
  }

  // Ajuste si se mueve dentro de la misma sección hacia una posición posterior
  // Si el item se movió de la misma sección Y su orden original era menor que el nuevo orden de destino,
  // y el nuevo orden se calculó como 'length', hay que restar 1 porque el item "desaparece" temporalmente.
  // Esta lógica de frontend para el 'newOrder' puede ser complicada y propensa a errores.
  // Es más robusto si el backend puede aceptar un "target_item_id" y una posición ("before" o "after").
  // Por ahora, el DTO solo pide 'new_order'. Un 'new_order' simple (como el índice) debería ser manejado por el backend.

  console.log(`Drop: Mover item ID ${tierListItemIdToMove} (originalmente en ${originalSectionId}) a sección ID ${targetSectionId}. ¿Antes del item ID ${beforeItemId}? Nuevo orden objetivo: ${newOrder}`);

  isLoadingTierItemAction.value = true;
  errorMessageApi.value = '';

  const moveDTO = {
    target_section_internal_id: targetSectionId,
    new_order: newOrder
  };

  try {
    const response = await moveItemInMyTierList(props.tierListPublicId, tierListItemIdToMove, moveDTO);
    tierListDetails.value = response.data; // Actualizar con la respuesta completa
  } catch (error) {
    console.error("Error moviendo ítem en Tier List:", error);
    if (error.response?.data) {
      errorMessageApi.value = error.response.data.message || error.response.data.error || "No se pudo mover el ítem.";
    } else {
      errorMessageApi.value = "Error de red al mover el ítem.";
    }
    // Recargar los detalles si la actualización local es compleja o falla
    await fetchTierListDetails(props.tierListPublicId);
  } finally {
    isLoadingTierItemAction.value = false;
    handleDragEnd(event); // Pasar el evento
  }
};

const handleDragEnd = (event) => { // event puede ser opcional si se llama desde otros sitios sin él
  console.log('Drag End');
  // Limpiar cualquier clase 'dragging-item' de todos los items
  document.querySelectorAll('.tier-item-compact.dragging-item').forEach(el => {
    el.classList.remove('dragging-item');
  });
  draggedItemInfo.value = null;
  dragOverSectionId.value = null;
  dragOverItemId.value = null;
};

const handleDragLeaveSection = (event, sectionId) => {
  // Si el cursor sale del área de drop de esta sección, quitar el highlight
  if (dragOverSectionId.value === sectionId) {
    // Podrías necesitar una lógica más compleja si hay elementos anidados
    // Por ahora, simple:
    dragOverSectionId.value = null;
  }
};

const handleDropOnSection = async (event, targetSectionId) => {
  event.preventDefault();
  if (!draggedItemInfo.value) return;

  const tierListItemIdToMove = draggedItemInfo.value.tierListItemId;

  // Determinar el nuevo orden (por defecto al final de la nueva sección)
  let newOrder = 0;
  let targetSection;
  if (targetSectionId === tierListDetails.value?.unclassified_section?.internal_id) {
    targetSection = tierListDetails.value.unclassified_section;
  } else {
    targetSection = tierListDetails.value?.sections?.find(s => s.internal_id === targetSectionId);
  }
  if (targetSection && targetSection.items) {
    newOrder = targetSection.items.length;
    // Si el ítem se mueve dentro de la misma sección, y no es el último, su nuevo orden podría ser su longitud - 1
    if (draggedItemInfo.value.originalSectionId === targetSectionId) {
      newOrder = Math.max(0, targetSection.items.length - 1);
    }
  }

  console.log(`Drop: Mover item ID ${tierListItemIdToMove} a sección ID ${targetSectionId}, nuevo orden (estimado): ${newOrder}`);

  isLoadingTierItemAction.value = true; // Usar un loader
  errorMessageApi.value = '';

  const moveDTO = { // TierListItemMoveRequestDTO
    target_section_internal_id: targetSectionId,
    new_order: newOrder
  };

  try {
    const response = await moveItemInMyTierList(props.tierListPublicId, tierListItemIdToMove, moveDTO);
    tierListDetails.value = response.data; // Actualizar con la respuesta completa
  } catch (error) {
    console.error("Error moviendo ítem en Tier List:", error);
    if (error.response?.data) {
      errorMessageApi.value = error.response.data.message || error.response.data.error || "No se pudo mover el ítem.";
    } else {
      errorMessageApi.value = "Error de red al mover el ítem.";
    }
    // Podrías querer recargar los detalles si la actualización local es compleja o falla
    await fetchTierListDetails(props.tierListPublicId);
  } finally {
    isLoadingTierItemAction.value = false;
    handleDragEnd(event); // Limpiar estado de drag
  }
};


// Función para obtener URL de carátula de un item de la Tier List
const getItemCoverUrl = (itemCoverUrl, targetSize = "cover_small") => {
  // TierListItemGameInfoDTO ya provee game_cover_url
  if (typeof itemCoverUrl === "string" && itemCoverUrl.trim() !== "") {
    let imageUrl = itemCoverUrl;
    if (imageUrl.startsWith("//")) {
      imageUrl = `https:${imageUrl}`;
    }
    // Aplicar transformación de tamaño si es necesario
    const regex = /(\/t_)[a-zA-Z0-9_-]+(\/)/;
    if (regex.test(imageUrl)) {
      // Si ya tiene un /t_.../
      imageUrl = imageUrl.replace(regex, `$1${targetSize}$2`);
    } else if (imageUrl.includes("/igdb/image/upload/")) {
      if (!imageUrl.includes("/igdb/image/upload/" + targetSize + "/")) {
        imageUrl = imageUrl.replace("/upload/", `/upload/${targetSize}/`);
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
  if (!isoDateString) return "";
  try {
    return new Date(isoDateString).toLocaleDateString(undefined, {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  } catch (e) {
    return isoDateString;
  }
};

const formatTierListType = (type) => {
  if (!type) return "Tipo Desconocido";
  const typeMap = {
    PROFILE_GLOBAL: "De Perfil",
    FROM_GAMELIST: "Desde Lista de Juegos",
  };
  return typeMap[String(type).toUpperCase()] || String(type);
};

const getTierColor = (sectionName) => {
  if (!sectionName) return "#CCCCCC"; // Un gris neutro por defecto
  const name = String(sectionName).toUpperCase();
  
  // Paleta de colores hexadecimales
  if (name.includes("S")) return "#FF7F7F"; // Rojo claro
  if (name.includes("A")) return "#FFBF7F"; // Naranja
  if (name.includes("B")) return "#FFFF7F"; // Amarillo
  if (name.includes("C")) return "#BFFF7F"; // Verde claro
  if (name.includes("D")) return "#7FBFFF"; // Azul claro
  if (name.includes("E") || name.includes("F")) return "#BDB0D0"; // Lavanda grisáceo
  
  return "#CCCCCC"; // Gris por defecto para nombres no reconocidos
};
</script>
<style src="./TierListDetail.css" scoped></style>
