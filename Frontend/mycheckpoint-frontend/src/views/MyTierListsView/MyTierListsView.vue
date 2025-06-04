<template>
  <div class="my-tier-lists-view section-block">
    <div class="header-actions">
      <h2>Mis Tier Lists</h2>
      <button @click="openCreateTierListModal" class="action-button primary">Crear Nueva Tier List (Perfil)</button>
    </div>

    <div v-if="isLoading" class="loading-message">Cargando mis tier lists...</div>
    <div v-if="errorMessageApi" class="error-message">{{ errorMessageApi }}</div>

    <div v-if="!isLoading && myTierLists.length === 0 && !errorMessageApi" class="empty-message">
      Aún no has creado ninguna Tier List.
    </div>

    <div class="tierlists-grid-profile" v-if="!isLoading && myTierLists.length > 0">
      <div v-for="list in myTierLists" :key="list.public_id" class="tierlist-card-profile">
        <RouterLink :to="{ name: 'view-public-tierlist', params: { tierListPublicId: list.public_id } }" class="card-link-profile">
          <div class="card-header-profile">
            <h3 class="list-name-profile">{{ list.name }}</h3>
            <span class="list-status-chip-profile" :class="list.is_public ? 'public' : 'private'">
              {{ list.is_public ? 'Pública' : 'Privada' }}
            </span>
          </div>
          <p v-if="list.description" class="list-description-profile">{{ truncateText(list.description, 100) }}</p>
          <p v-else class="list-description-empty-profile"><em>Sin descripción.</em></p>
          <div class="list-footer-profile">
            <span class="type-info">Tipo: {{ formatTierListType(list.type) }}</span>
            <span class="last-updated-profile" v-if="list.updated_at">Última act.: {{ formatReadableDate(list.updated_at) }}</span>
          </div>
        </RouterLink>
      </div>
    </div>

    <div v-if="showCreateTierListModal" class="modal-overlay" @click.self="closeCreateTierListModal">
      <div class="modal-panel">
        <form @submit.prevent="handleCreateTierList" class="create-list-form">
          <div class="modal-header">
            <h3>Crear Nueva Tier List de Perfil</h3>
            <button type="button" @click="closeCreateTierListModal" class="modal-close-button" aria-label="Cerrar">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label for="tierListName">Nombre de la Tier List:</label>
              <input type="text" id="tierListName" v-model="newTierListForm.name" required maxlength="150">
            </div>
            <div class="form-group">
              <label for="tierListDescription">Descripción (opcional):</label>
              <textarea id="tierListDescription" v-model="newTierListForm.description" rows="3" maxlength="1000"></textarea>
            </div>
            <div class="form-group checkbox-group">
              <input type="checkbox" id="tierListIsPublic" v-model="newTierListForm.is_public">
              <label for="tierListIsPublic">¿Hacer esta Tier List pública?</label>
            </div>
            <div v-if="createTierListErrorMessage" class="error-message modal-error">{{ createTierListErrorMessage }}</div>
          </div>
          <div class="modal-footer">
            <button type="button" @click="closeCreateTierListModal" class="action-button secondary" :disabled="isCreatingTierList">
              Cancelar
            </button>
            <button type="submit" :disabled="isCreatingTierList || !newTierListForm.name" class="action-button primary">
              {{ isCreatingTierList ? 'Creando...' : 'Crear Tier List' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, reactive } from 'vue'; // reactive estaba faltando o mal colocado
import { RouterLink, useRouter } from 'vue-router'; // useRouter por si se necesita para navegación futura
import { getMyProfileTierLists, createMyProfileTierList } from '@/services/apiInstances';
// No es estrictamente necesario authStore si los endpoints son /me/ y la vista está protegida por guardias de ruta

const myTierLists = ref([]);
const isLoading = ref(true);
const errorMessageApi = ref('');

// Estado para el modal de creación - ASEGÚRATE QUE ESTÉ DEFINIDO ASÍ
const showCreateTierListModal = ref(false);
const isCreatingTierList = ref(false);
const createTierListErrorMessage = ref('');
const newTierListForm = reactive({ // <--- DEFINICIÓN CORRECTA
  name: '',
  description: null,
  is_public: false,
});

const loadMyTierLists = async () => {
  isLoading.value = true;
  errorMessageApi.value = '';
  try {
    const response = await getMyProfileTierLists(); 
    myTierLists.value = response.data;
    console.log("Mis Tier Lists cargadas (todos los tipos):", myTierLists.value);
  } catch (error) {
    console.error("Error cargando mis tier lists:", error);
    errorMessageApi.value = "Error al cargar tus tier lists.";
    if (error.response) {
      errorMessageApi.value = `Error ${error.response.status}: ${error.response.data.message || error.response.data.error || 'No se pudieron cargar las tier lists.'}`;
    }
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  loadMyTierLists();
});

const openCreateTierListModal = () => {
  newTierListForm.name = '';
  newTierListForm.description = null; 
  newTierListForm.is_public = false;
  createTierListErrorMessage.value = '';
  showCreateTierListModal.value = true;
};

const closeCreateTierListModal = () => {
  showCreateTierListModal.value = false;
};

const handleCreateTierList = async () => {
  if (!newTierListForm.name || newTierListForm.name.trim() === '') {
    createTierListErrorMessage.value = "El nombre de la tier list es obligatorio.";
    return;
  }
  isCreatingTierList.value = true;
  createTierListErrorMessage.value = '';

  const requestDTO = {
    name: newTierListForm.name,
    description: newTierListForm.description || null,
    is_public: newTierListForm.is_public,
  };

  try {
    const response = await createMyProfileTierList(requestDTO);
    myTierLists.value.unshift(response.data); 
    closeCreateTierListModal();
  } catch (error) {
    console.error("Error creando tier list:", error);
    if (error.response?.data) {
      createTierListErrorMessage.value = error.response.data.errors?.join(', ') || error.response.data.message || error.response.data.error || "No se pudo crear la tier list.";
    } else {
      createTierListErrorMessage.value = "Error de red al crear la tier list.";
    }
  } finally {
    isCreatingTierList.value = false;
  }
};

const formatReadableDate = (isoDateString) => {
  if (!isoDateString) return '';
  try {
    return new Date(isoDateString).toLocaleDateString(undefined, { 
      year: 'numeric', month: 'short', day: 'numeric' 
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

const formatTierListType = (type) => { 
  if (!type) return 'Tipo Desconocido';
  const typeMap = {
    'PROFILE_GLOBAL': 'De Perfil',
    'FROM_GAMELIST': 'Desde Lista de Juegos'
  };
  return typeMap[type.toUpperCase()] || type; 
};
</script>
<style src="./MyTierListsView.css" scoped>

</style>