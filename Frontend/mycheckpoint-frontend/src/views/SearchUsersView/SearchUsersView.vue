<template>
  <div class="search-users-view">
    <h1>Buscar Usuarios</h1>
    
    <form @submit.prevent="executeSearch" class="search-form">
      <input 
        type="text" 
        v-model="searchQuery" 
        placeholder="Escribe un nombre de usuario..."
        class="search-input"
        :disabled="isLoading"
      />
      <button type="submit" class="search-button" :disabled="isLoading || searchQuery.length < 2">
        {{ isLoading ? 'Buscando...' : 'Buscar' }}
      </button>
    </form>
    <small v-if="searchQuery.length > 0 && searchQuery.length < 2" class="input-hint">
      Necesitas al menos 2 caracteres para buscar.
    </small>

    <div v-if="isLoading" class="loading-message">Buscando usuarios...</div>
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    <div v-if="searched && !isLoading && searchResults.length === 0 && !errorMessage" class="no-results-message">
      No se encontraron usuarios que coincidan con tu búsqueda.
    </div>

    <div class="results-grid" v-if="searchResults.length > 0">
      <div v-for="user in searchResults" :key="user.public_id" class="user-card">
        <RouterLink :to="{ name: 'profile', params: { publicId: user.public_id } }">
          <img 
            :src="getProfilePictureUrl(user.foto_perfil)" 
            alt="Avatar del usuario" 
            class="user-avatar"
            @error="onAvatarError"
          />
          <div class="user-info">
            <h3 class="user-name">{{ user.nombre_usuario }}</h3>
          </div>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { RouterLink } from 'vue-router';
import { searchUsers } from '@/services/apiInstances'; // <-- Necesitarás añadir esta función
import { BASE_PATH } from '@/api-client/base';
import defaultAvatar from '@/assets/img/default-avatar.png';

const searchQuery = ref('');
const searchResults = ref([]);
const isLoading = ref(false);
const errorMessage = ref('');
const searched = ref(false); // Para saber si ya se realizó una búsqueda

const executeSearch = async () => {
  if (searchQuery.value.length < 2) {
    errorMessage.value = "Por favor, introduce al menos 2 caracteres para buscar.";
    return;
  }
  
  isLoading.value = true;
  errorMessage.value = '';
  searchResults.value = [];
  searched.value = true;

  try {
    const response = await searchUsers(searchQuery.value);
    searchResults.value = response.data;
  } catch (error) {
    console.error("Error buscando usuarios:", error);
    if (error.response && error.response.status === 404) {
      errorMessage.value = `No se encontraron usuarios para "${searchQuery.value}".`;
    } else if (error.response) {
      errorMessage.value = `Error ${error.response.status}: ${error.response.data.message || 'No se pudo realizar la búsqueda.'}`;
    } else {
      errorMessage.value = "Error de red al realizar la búsqueda.";
    }
    searchResults.value = [];
  } finally {
    isLoading.value = false;
  }
};

const getProfilePictureUrl = (fotoPerfil) => {
  if (fotoPerfil) {
    if (fotoPerfil.startsWith('http')) {
      return fotoPerfil;
    }
    const baseApiUrl = BASE_PATH.endsWith('/') ? BASE_PATH.slice(0, -1) : BASE_PATH;
    const relativeImagePath = fotoPerfil.startsWith('/') ? fotoPerfil.substring(1) : fotoPerfil;
    return `${baseApiUrl}/profile-pictures/${relativeImagePath}`;
  }
  return defaultAvatar;
};

const onAvatarError = (event) => {
  event.target.src = defaultAvatar;
};
</script>

<style scoped>
.search-users-view {
  max-width: 900px;
  margin: 1rem auto;
  padding: 1rem;
}

h1 {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--color-heading);
}

.search-form {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.search-input {
  flex-grow: 1;
  padding: 0.75rem;
  font-size: 1rem;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background-color: var(--color-background);
  color: var(--color-text);
}

.search-button {
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  border: none;
  border-radius: 4px;
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  cursor: pointer;
  transition: background-color 0.2s;
}

.search-button:hover:not(:disabled) {
  background-color: hsla(160, 100%, 30%, 1);
}

.search-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.input-hint {
  font-size: 0.8rem;
  color: var(--color-text-light-2);
}

.loading-message, .error-message, .no-results-message {
  text-align: center;
  padding: 2rem;
  margin-top: 1rem;
  font-size: 1.2rem;
  background-color: var(--color-background-soft);
  border-radius: 8px;
}

.error-message {
  color: #d8000c;
  background-color: #ffbaba;
}

.results-grid {
  margin-top: 2rem;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1.5rem;
}

.user-card {
  background-color: var(--color-background-soft);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
  border: 1px solid var(--color-border);
  text-align: center;
}

.user-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.user-card a {
  text-decoration: none;
  color: inherit;
  display: block;
  padding: 1rem;
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--color-border-hover);
  margin-bottom: 0.75rem;
}

.user-name {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}
</style>