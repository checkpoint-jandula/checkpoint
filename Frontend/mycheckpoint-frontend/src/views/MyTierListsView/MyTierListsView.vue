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
<style scoped>
.my-tier-lists-view {
  /* padding: 1.5rem; si no es un section-block en sí mismo cuando se embebe */
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.header-actions h2 { 
  margin: 0; 
  font-size: 1.6rem; 
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
  background-color: var(--color-background-soft);
  padding: 1.5rem;
  border-radius: 6px;
}

.tierlists-grid-profile {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.tierlist-card-profile {
  background-color: var(--color-background);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
  border: 1px solid var(--color-border);
  display: flex; /* Para que el enlace ocupe toda la altura */
  flex-direction: column;
}
.tierlist-card-profile:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}
.card-link-profile {
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
  padding: 1rem 1.2rem;
  height: 100%;
  flex-grow: 1; /* Asegura que el enlace expanda */
}
.card-header-profile {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.5rem;
}
.list-name-profile {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
  line-height: 1.3;
}
.list-status-chip-profile { 
  font-size: 0.75rem; padding: 0.2em 0.6em; border-radius: 10px;
  font-weight: 500; white-space: nowrap; margin-left: 0.5rem;
}
.list-status-chip-profile.public {
  background-color: hsla(160, 100%, 37%, 0.15); color: hsla(160, 100%, 30%, 1);
  border: 1px solid hsla(160, 100%, 37%, 0.3);
}
.list-status-chip-profile.private {
  background-color: var(--color-background-mute); color: var(--color-text-light-2);
  border: 1px solid var(--color-border);
}
.list-description-profile, .list-description-empty-profile {
  font-size: 0.85rem; color: var(--color-text); line-height: 1.5;
  margin-bottom: 0.75rem; flex-grow: 1;
  display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical;
  overflow: hidden; text-overflow: ellipsis; min-height: calc(1.5em * 2);
}
.list-description-empty-profile { font-style: italic; color: var(--color-text-light-2); }
.list-footer-profile {
  margin-top: auto; display: flex; justify-content: space-between;
  font-size: 0.75rem; color: var(--color-text-light-2);
  padding-top: 0.5rem; border-top: 1px solid var(--color-border);
}
.type-info { 
  font-weight: 500;
}

/* Estilos del Modal de Creación (reutilizados/adaptados de MyGameListsView) */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background-color: rgba(0, 0, 0, 0.65); display: flex;
  justify-content: center; align-items: center; z-index: 1050;
}
.modal-panel {
  background-color: var(--color-background-soft); padding: 0;
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
.modal-body { padding: 1.5rem; overflow-y: auto; }
.create-list-form .form-group { margin-bottom: 1rem; }
.create-list-form .form-group:last-of-type { margin-bottom: 0; }
.create-list-form label { display: block; margin-bottom: 0.4rem; font-size: 0.9rem; color: var(--color-text); font-weight:500; }
.create-list-form input[type="text"],
.create-list-form textarea {
  width: 100%; padding: 0.7rem; border: 1px solid var(--color-border);
  border-radius: 4px; background-color: var(--color-background); color: var(--color-text); font-size: 1rem;
}
.create-list-form .checkbox-group { display: flex; align-items: center; margin-top: 0.5rem;}
.create-list-form .checkbox-group input[type="checkbox"] { margin-right: 0.5rem; width: auto; transform: scale(1.1); }
.create-list-form .checkbox-group label { margin-bottom: 0; font-size: 1rem; font-weight: normal; color: var(--color-text); }
.modal-error { font-size: 0.9rem; margin-top: 0.5rem; color: #d8000c; }

.modal-footer {
  padding: 1rem 1.5rem; border-top: 1px solid var(--color-border);
  display: flex; justify-content: flex-end; gap: 0.75rem;
}

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