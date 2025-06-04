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
            <p
              v-if="tierListDetails.description"
              class="list-description-detail"
            >
              {{ tierListDetails.description }}
            </p>
            <p v-else class="list-description-detail-empty">
              <em>Esta tier list no tiene descripción.</em>
            </p>
            <div class="list-meta-detail">
              <span
                :class="[
                  'status-chip',
                  tierListDetails.is_public ? 'public' : 'private',
                ]"
              >
                {{ tierListDetails.is_public ? "Pública" : "Privada" }}
              </span>
              <span class="meta-separator">|</span>
              <span>Tipo: {{ formatTierListType(tierListDetails.type) }}</span>
              <span
                v-if="
                  tierListDetails.type === 'FROM_GAMELIST' &&
                  tierListDetails.source_game_list_public_id
                "
                class="meta-separator"
                >|</span
              >
              <span
                v-if="
                  tierListDetails.type === 'FROM_GAMELIST' &&
                  tierListDetails.source_game_list_public_id
                "
              >
                <RouterLink
                  :to="{
                    name: 'gamelist-detail',
                    params: {
                      listPublicId: tierListDetails.source_game_list_public_id,
                    },
                  }"
                >
                  Ver GameList Origen
                </RouterLink>
              </span>
              <span class="meta-separator">|</span>
              <span>Creada por: {{ tierListDetails.owner_username }}</span>
              <span v-if="tierListDetails.updated_at" class="meta-separator"
                >|</span
              >
              <span v-if="tierListDetails.updated_at">
                Última act.:
                {{ formatReadableDate(tierListDetails.updated_at) }}
              </span>
            </div>
          </div>
          <div v-if="isOwner" class="header-actions-tierlist">
            <button
              v-if="isEditableTierList"
              @click="openEditTierListMetadataModal"
              class="action-button secondary"
            >
              Editar Detalles
            </button>
            <button
              @click="handleDeleteTierList"
              :disabled="isLoading"
              class="action-button secondary"
            >
              Eliminar Tier List
            </button>
          </div>
        </div>
      </section>

      <div class="tier-maker-container section-block">
        <div class="add-section-controls" v-if="isEditableTierList">
          <button @click="openAddSectionModal" class="action-button secondary">
            Añadir Nueva Tier (Fila)
          </button>
        </div>

        <div
          v-for="section in sortedCustomSections"
          :key="section.internal_id"
          class="tier-row"
        >
          <div
            class="tier-label"
            :style="{ backgroundColor: getTierColor(section.name) }"
          >
            <span
              v-if="
                !editingSectionName || editingSectionId !== section.internal_id
              "
              >{{ section.name }}</span
            >
            <input
              v-if="
                isEditableTierList &&
                editingSectionName &&
                editingSectionId === section.internal_id
              "
              v-model="currentSectionNameEdit"
              @keyup.enter="saveSectionName(section.internal_id)"
              @blur="saveSectionName(section.internal_id)"
              class="section-name-input"
            />
            <div v-if="isEditableTierList" class="tier-actions">
              <button
                @click="startEditSectionName(section)"
                class="icon-button"
                title="Editar nombre tier"
              >
                &#9998;
              </button>
              <button
                @click="confirmRemoveSection(section.internal_id)"
                class="icon-button danger"
                title="Eliminar tier"
              >
                &times;
              </button>
            </div>
          </div>
          <div class="tier-items-droppable-area">
            <div
              v-if="!section.items || section.items.length === 0"
              class="tier-empty-placeholder"
            >
              Arrastra juegos aquí
            </div>
            <div v-else class="tier-items-grid-horizontal">
              <div
                v-for="item in section.items"
                :key="item.tier_list_item_id"
                class="tier-item-compact"
              >
                <RouterLink
                  :to="{
                    name: 'game-details',
                    params: { igdbId: item.game_igdb_id },
                  }"
                  :title="item.game_name"
                >
                  <img
                    :src="getItemCoverUrl(item.game_cover_url, 'cover_big')"
                    :alt="item.game_name"
                    class="tier-item-cover-compact"
                    @error="onTierItemCoverError"
                  />
                </RouterLink>
                <button
                  v-if="
                    isEditableTierList ||
                    (isOwner &&
                      tierListDetails.type ===
                        'FROM_GAMELIST') /*y se permite mover*/
                  "
                  @click.stop="
                    handleRemoveItemFromTier(
                      item.tier_list_item_id,
                      section.internal_id
                    )
                  "
                  class="remove-item-button-compact"
                  title="Quitar ítem de esta tier"
                >
                  &times;
                </button>
              </div>
            </div>
          </div>
        </div>

        <div
          class="tier-row unclassified-tier-row"
          v-if="
            tierListDetails.unclassified_section ||
            (isEditableTierList &&
              /*lógica para mostrar juegos de la biblioteca no añadidos*/ true)
          "
        >
          <div class="tier-label unclassified-label">
            <span>{{
              tierListDetails.unclassified_section
                ? tierListDetails.unclassified_section.name
                : "Juegos Disponibles"
            }}</span>
            <button
              v-if="isEditableTierList"
              @click="openAddGamesToTierModal(null)"
              class="action-button-small tertiary"
              title="Añadir juegos de tu biblioteca a esta Tier List"
            >
              Añadir Juegos de Biblioteca
            </button>
          </div>
          <div class="tier-items-droppable-area">
            <div
              v-if="
                !tierListDetails.unclassified_section ||
                !tierListDetails.unclassified_section.items ||
                tierListDetails.unclassified_section.items.length === 0
              "
              class="tier-empty-placeholder"
            >
              {{
                tierListDetails.type === "FROM_GAMELIST"
                  ? "Los juegos de la GameList base aparecerán aquí si no están en una tier."
                  : "Arrastra juegos aquí o añádelos desde tu biblioteca."
              }}
            </div>
            <div v-else class="tier-items-grid-horizontal">
              <div
                v-for="item in tierListDetails.unclassified_section.items"
                :key="item.tier_list_item_id"
                class="tier-item-compact"
              >
                <RouterLink
                  :to="{
                    name: 'game-details',
                    params: { igdbId: item.game_igdb_id },
                  }"
                  :title="item.game_name"
                >
                  <img
                    :src="getItemCoverUrl(item.game_cover_url, 'cover_big')"
                    :alt="item.game_name"
                    class="tier-item-cover-compact"
                    @error="onTierItemCoverError"
                  />
                </RouterLink>
                <button
                  v-if="isEditableTierList"
                  @click.stop="
                    handleRemoveItemFromTierList(item.tier_list_item_id)
                  "
                  class="remove-item-button-compact"
                  title="Quitar ítem de la tier list (si es PROFILE_GLOBAL)"
                >
                  &times;
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="!isLoading && !tierListDetails && errorMessageApi"
      class="no-results-message"
    >
      No se pudo cargar la Tier List o no es accesible.
    </div>
    <div
      v-if="showEditTierListMetadataModal && isEditableTierList"
      class="modal-overlay"
      @click.self="closeEditTierListMetadataModal"
    >
      <div class="modal-panel">
        <form
          @submit.prevent="handleUpdateTierListMetadata"
          class="edit-tierlist-form-modal"
        >
          <div class="modal-header">
            <h3>Editar Detalles de la Tier List</h3>
            <button
              type="button"
              @click="closeEditTierListMetadataModal"
              class="modal-close-button"
              aria-label="Cerrar"
            >
              &times;
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label for="editTierListName">Nombre de la Tier List:</label>
              <input
                type="text"
                id="editTierListName"
                v-model="editTierListForm.name"
                required
                maxlength="150"
              />
            </div>
            <div class="form-group">
              <label for="editTierListDescription"
                >Descripción (opcional):</label
              >
              <textarea
                id="editTierListDescription"
                v-model="editTierListForm.description"
                rows="3"
                maxlength="1000"
              ></textarea>
            </div>
            <div class="form-group checkbox-group">
              <input
                type="checkbox"
                id="editTierListIsPublic"
                v-model="editTierListForm.is_public"
              />
              <label for="editTierListIsPublic"
                >¿Hacer esta Tier List pública?</label
              >
            </div>
          </div>
          <div class="modal-footer">
            <div
              v-if="editTierListMetadataMessage"
              :class="
                editTierListMetadataError ? 'error-message' : 'success-message'
              "
              class="action-message modal-action-message"
            >
              {{ editTierListMetadataMessage }}
            </div>
            <button
              type="button"
              @click="closeEditTierListMetadataModal"
              class="action-button secondary"
              :disabled="isLoadingTierListMetadataUpdate"
            >
              Cancelar
            </button>
            <button
              type="submit"
              :disabled="
                isLoadingTierListMetadataUpdate || !editTierListForm.name.trim()
              "
              class="action-button primary"
            >
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
  </div>
</template>
<script setup>
import { ref, onMounted, watch, reactive, computed} from "vue";
import { useRoute, RouterLink, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/authStore";
import {
  getTierListDetailsByPublicId,
  updateMyTierListMetadata,
  deleteMyTierList,
  // --- Funciones API placeholder para futuras implementaciones ---
  // addSectionToMyTierList,
  // updateMySectionName,
  // removeSectionFromMyTierList,
  // addItemToMyTierListSection,
  // addItemToMyUnclassifiedSection,
  // removeItemFromMyTierList,
  // moveItemInMyTierList,
  // getMyUserGameLibrary // Para el modal de añadir ítems
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


// --- Estado para editar metadatos de TierList ---
const showEditTierListMetadataModal = ref(false);
const isLoadingTierListMetadataUpdate = ref(false);
const editMetadataMessage = ref(""); // <--- DEFINICIÓN
const editMetadataError = ref(false); // <--- DEFINICIÓN
const editTierListMetadataError = ref(false);
const editTierListMetadataMessage = ref(''); // <--- DEFINICIÓN
const editTierListForm = reactive({
  name: "",
  description: null,
  is_public: false,
});

// --- Estado para editar nombre de sección ---
const editingSectionName = ref(false);
const editingSectionId = ref(null);
const currentSectionNameEdit = ref("");

// (Placeholders para otros estados de modales como añadir sección, añadir ítems)

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
      errorMessageApi.value = `Error ${error.response.status}: ${
        error.response.data.message ||
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
  editTierListMetadataMessage.value = ''; // Resetea el mensaje
  editTierListMetadataError.value = false;   // Resetea el estado de error
  showEditTierListMetadataModal.value = true;
};

const closeEditTierListMetadataModal = () => {
  showEditTierListMetadataModal.value = false;
};

const handleUpdateTierListMetadata = async () => {
  if (!isEditableTierList.value) {
    editTierListMetadataMessage.value = "No tienes permiso para editar esta Tier List o no es editable.";
    editTierListMetadataError.value = true;
    return;
  }
  if (!editTierListForm.name || editTierListForm.name.trim() === "") {
    editTierListMetadataMessage.value = "El nombre de la Tier List es obligatorio.";
    editTierListMetadataError.value = true;
    return;
  }
  isLoadingTierListMetadataUpdate.value = true;
  editTierListMetadataMessage.value = '';
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
      changes
    );
    tierListDetails.value = response.data;
    editTierListMetadataMessage.value = "¡Detalles de la Tier List actualizados!";
    editTierListMetadataError.value = false;
    setTimeout(() => closeEditTierListMetadataModal(), 1500);
  } catch (error) {
    console.error("Error actualizando metadatos de la Tier List:", error);
    editTierListMetadataError.value = true;
    if (error.response?.data) {
       editTierListMetadataMessage.value = error.response.data.errors?.join(', ') || error.response.data.message || error.response.data.error || "No se pudo actualizar la Tier List.";
    } else {
      editTierListMetadataMessage.value = "Error de red al actualizar la Tier List.";
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

// --- Lógica para Editar Nombre de Sección (Placeholders) ---
const startEditSectionName = (section) => {
  if (!isEditableTierList.value) return;
  editingSectionId.value = section.internal_id;
  currentSectionNameEdit.value = section.name;
  editingSectionName.value = true;
};

const saveSectionName = async (sectionId) => {
  if (!editingSectionName.value || editingSectionId.value !== sectionId) return;
  const section =
    tierListDetails.value?.sections?.find((s) => s.internal_id === sectionId) ||
    (tierListDetails.value?.unclassified_section?.internal_id === sectionId
      ? tierListDetails.value.unclassified_section
      : null);
  if (
    !section ||
    currentSectionNameEdit.value.trim() === "" ||
    currentSectionNameEdit.value === section.name
  ) {
    editingSectionName.value = false;
    editingSectionId.value = null;
    return;
  }
  if (section.is_default_unclassified) {
    alert("El nombre de la sección 'Sin Clasificar' no se puede cambiar.");
    editingSectionName.value = false;
    editingSectionId.value = null;
    return;
  }
  alert(
    `TODO: API Call - Guardar nombre "${currentSectionNameEdit.value}" para sección ID ${sectionId}`
  );
  await fetchTierListDetails(props.tierListPublicId);
  editingSectionName.value = false;
  editingSectionId.value = null;
};

const confirmRemoveSection = async (sectionId) => {
  if (!isEditableTierList.value) return;
  const section = tierListDetails.value?.sections?.find(
    (s) => s.internal_id === sectionId
  );
  if (!section || section.is_default_unclassified) {
    alert("No se puede eliminar esta sección.");
    return;
  }
  if (
    window.confirm(
      `¿Seguro que quieres eliminar la tier "${section.name}"? Los juegos se moverán a 'Sin Clasificar'.`
    )
  ) {
    alert(`TODO: API Call - Eliminar sección ID ${sectionId}`);
    await fetchTierListDetails(props.tierListPublicId);
  }
};

// --- Lógica para Añadir/Quitar Ítems (Placeholders) ---
const openAddSectionModal = () => {
  if (!isEditableTierList.value) return;
  alert("TODO: Abrir modal para añadir nueva sección (tier).");
};

const openAddGamesToTierModal = (sectionId) => {
  if (!isEditableTierList.value) {
    alert("No puedes añadir juegos a este tipo de Tier List.");
    return;
  }
  alert(
    `TODO: Abrir modal para añadir juegos a sección ID ${
      sectionId !== null ? sectionId : "Sin Clasificar"
    }`
  );
};

const handleRemoveItemFromTierList = async (tierListItemId) => {
  if (!isEditableTierList.value) {
    alert("No puedes quitar ítems de este tipo de Tier List directamente.");
    return;
  }
  if (window.confirm("¿Seguro que quieres quitar este juego de la tier?")) {
    alert(`TODO: API Call - Quitar ítem ${tierListItemId}`);
    await fetchTierListDetails(props.tierListPublicId);
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
  if (!sectionName) return "rgba(200, 200, 200, 0.5)";
  const name = String(sectionName).toUpperCase();
  if (name.includes("S")) return "rgba(255, 100, 100, 0.7)";
  if (name.includes("A")) return "rgba(255, 170, 85, 0.7)";
  if (name.includes("B")) return "rgba(255, 223, 100, 0.7)";
  if (name.includes("C")) return "rgba(150, 220, 130, 0.7)";
  if (name.includes("D")) return "rgba(120, 170, 255, 0.7)";
  if (name.includes("E") || name.includes("F"))
    return "rgba(180, 150, 220, 0.7)";
  return "rgba(200, 200, 200, 0.5)";
};
</script>
<style src="./TierListDetail.css" scoped>
</style>
