<template>
  <header class="main-header">
    <div class="header-content">
      <RouterLink :to="{ name: 'home' }" class="logo-container">
        <Logo alt="MyCheckPoint Logo" class="logo" />
        <span class="app-title">MyCheckPoint</span>
      </RouterLink>

      

      <nav class="main-nav" :class="{ 'is-open': isMenuOpen }">
        <RouterLink to="/">Home</RouterLink>
        <RouterLink v-if="authStore.isAuthenticated" :to="{ name: 'friends' }">Amigos</RouterLink>
        <RouterLink to="/public-gamelists">Listas Públicas</RouterLink>
        <RouterLink to="/public-tierlists">Tier Lists Públicas</RouterLink>
        <RouterLink to="/search-users">Buscar Usuarios</RouterLink>
      </nav>

      <div class="actions-container">

        <button @click="isMenuOpen = !isMenuOpen" class="menu-toggle" aria-label="Abrir menú">
        <span class="hamburger-icon"></span>
      </button>


        <form @submit.prevent="executeSearch" class="search-container">
          <input type="search" v-model="searchQuery" placeholder="Buscar por nombre..." @keyup.enter="performSearch" />
          <button @click="performSearch" class="search-button" aria-label="Buscar">
            <span class="icon-search" aria-label="Buscar">🔍</span>
          </button>
        </form>

        <button @click="toggleFilterPanel" class="filter-toggle-button" aria-label="Filtros avanzados">
          <span class="filter-icon"></span>
        </button>

        <div class="user-auth-group">
          <template v-if="authStore.isAuthenticated && authStore.currentUser">
            <div class="user-menu-container">
              <RouterLink :to="{ name: 'profile', params: { publicId: authStore.currentUser.public_id } }"
                class="user-profile-link" title="Mi Perfil">
                <img :src="profilePictureUrl" alt="Mi perfil" class="user-avatar-header">
              </RouterLink>
              <a role="button" href="#" @click.prevent="handleLogout" class="logout-button">Salir</a>
            </div>
          </template>
          <template v-else>
            <RouterLink to="/login" class="action-button secondary">Iniciar Sesión</RouterLink>
            <RouterLink to="/register" class="action-button primary">Registrarse</RouterLink>
          </template>
        </div>
      </div>
    </div>

    <div v-if="showFilterPanel" class="filter-panel">
      <form @submit.prevent="executeSearch" class="search-container search-filter-form">
          <input type="search" v-model="searchQuery" placeholder="Buscar por nombre..." @keyup.enter="performSearch" />
          <button @click="performSearch" class="search-button" aria-label="Buscar">
            <span class="icon-search" aria-label="Buscar">🔍</span>
          </button>
        </form>
      <form @submit.prevent="applyFilters" class="filter-form">
        <div class="filter-group">
          <label for="filter-genre">Género:</label>
          <select id="filter-genre" v-model="filters.genreId">
            <option :value="null">Cualquiera</option>
            <option v-for="genre in genres" :key="genre.id" :value="genre.id">{{ genre.name }}</option>
          </select>
        </div>
        <div class="filter-group">
          <label for="filter-theme">Tema:</label>
          <select id="filter-theme" v-model="filters.themeId">
            <option :value="null">Cualquiera</option>
            <option v-for="theme in themes" :key="theme.id" :value="theme.id">{{ theme.name }}</option>
          </select>
        </div>
        <div class="filter-group">
          <label for="filter-gamemode">Modo de Juego:</label>
          <select id="filter-gamemode" v-model="filters.gameModeId">
            <option :value="null">Cualquiera</option>
            <option v-for="mode in gameModes" :key="mode.id" :value="mode.id">{{ mode.name }}</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Lanzado Después de:</label>
          <input type="date" v-model="filters.releaseDateStart" />
        </div>
        <div class="filter-group">
          <label>Lanzado Antes de:</label>
          <input type="date" v-model="filters.releaseDateEnd" />
        </div>
        <div class="filter-actions">
          <button type="button" @click="clearFilters" class="action-button secondary">Limpiar</button>
          <button type="submit" class="action-button primary">Filtrar</button>
        </div>
      </form>
    </div>
  </header>
</template>

<script setup>
import Logo from '@/components/Logo/Logo.vue';

import { ref, reactive, onMounted, computed } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { findAllGenres, findAllThemes, findAllGameModes } from '@/services/apiInstances';
import { BASE_PATH } from '@/api-client/base';
import defaultAvatar from '@/assets/img/default-avatar.png';

const isMenuOpen = ref(false);



const authStore = useAuthStore();
const router = useRouter();

const searchQuery = ref('');

// Lógica del panel de filtros
const showFilterPanel = ref(false);
const genres = ref([]);
const themes = ref([]);
const gameModes = ref([]);
const filters = reactive({
  genreId: null,
  themeId: null,
  gameModeId: null,
  releaseDateStart: '',
  releaseDateEnd: ''
});

onMounted(async () => {
  try {
    const [genresRes, themesRes, modesRes] = await Promise.all([
      findAllGenres(),
      findAllThemes(),
      findAllGameModes()
    ]);
    genres.value = genresRes.data;
    themes.value = themesRes.data;
    gameModes.value = modesRes.data;

    // --- LOG DE DEBUG 1 ---
    // Revisa aquí si los objetos tienen la propiedad 'igdbId' o 'id'.
    console.log('Datos para filtros cargados:', {
      genres: genres.value,
      themes: themes.value,
      gameModes: gameModes.value
    });

  } catch (error) {
    console.error("Error cargando datos para filtros:", error);
  }
});

const profilePictureUrl = computed(() => {
  const fotoPerfil = authStore.currentUser?.foto_perfil;
  if (fotoPerfil) {
    if (fotoPerfil.startsWith('http')) {
      return fotoPerfil;
    }
    const baseApiUrl = BASE_PATH ? BASE_PATH.replace(/\/$/, '') : 'http://localhost:8080';
    return `${baseApiUrl}/profile-pictures/${fotoPerfil}`;
  }
  return defaultAvatar;
});

const handleLogout = () => {
  authStore.logout();
  router.push({ name: 'login' });
};

const performSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ name: 'search-games', query: { q: searchQuery.value.trim() } });
  }
};

const toggleFilterPanel = () => {
  showFilterPanel.value = !showFilterPanel.value;
};

const applyFilters = () => {
  // --- LOG DE DEBUG 2 ---
  // Revisa aquí el valor de `filters.genreId` después de seleccionar una opción.
  console.log('Estado de los filtros al aplicar:', JSON.parse(JSON.stringify(filters)));

  const queryParams = { filter: 'true' };
  if (filters.genreId) queryParams.id_genero = filters.genreId;
  if (filters.themeId) queryParams.id_tema = filters.themeId;
  if (filters.gameModeId) queryParams.id_modo_juego = filters.gameModeId;
  if (filters.releaseDateStart) {
    queryParams.fecha_inicio = Math.floor(new Date(filters.releaseDateStart).getTime() / 1000);
  }
  if (filters.releaseDateEnd) {
    queryParams.fecha_fin = Math.floor(new Date(filters.releaseDateEnd).getTime() / 1000);
  }

  // --- LOG DE DEBUG 3 ---
  // Revisa si el objeto `queryParams` contiene `id_genero` con el valor correcto.
  console.log('Navegando con los siguientes queryParams:', queryParams);

  router.push({ name: 'search-games', query: queryParams });
  showFilterPanel.value = false;
};

const clearFilters = () => {
  filters.genreId = null;
  filters.themeId = null;
  filters.gameModeId = null;
  filters.releaseDateStart = '';
  filters.releaseDateEnd = '';
};
</script>

<style src="./Header.css" scoped></style>