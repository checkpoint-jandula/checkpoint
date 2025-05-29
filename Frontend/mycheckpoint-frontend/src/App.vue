<template>
  <header>
    <div class="logo-container">
      <img alt="MyCheckPoint Logo" class="logo" src="@/assets/logo.svg" width="40" height="40" />
      <span class="app-title">MyCheckPoint</span>
    </div>
    <nav>
      <RouterLink to="/">Home</RouterLink>
      <RouterLink to="/public-gamelists">Listas Públicas</RouterLink>
      <RouterLink to="/public-tierlists">Tier Lists Públicas</RouterLink>
      <RouterLink to="/search-users">Buscar Usuarios</RouterLink>

      <div class="search-container">
        <input 
          type="search" 
          v-model="searchQuery" 
          placeholder="Buscar juegos..." 
          @keyup.enter="performSearch" 
        />
        <button @click="performSearch" class="search-button" aria-label="Buscar">🔎</button>
      </div>

      <template v-if="authStore.isAuthenticated && authStore.currentUser">
        <RouterLink to="/friends">Amigos</RouterLink>
        <RouterLink :to="`/profile/${authStore.currentUser.public_id}`" class="user-profile-link">
          {{ authStore.currentUser.nombre_usuario }}
        </RouterLink>
        <RouterLink v-if="isDevelopment" to="/settings">Ajustes (Dev)</RouterLink> 
        <a role="button" href="#" @click.prevent="handleLogout" class="logout-button">Cerrar Sesión</a>
      </template>
      <template v-else>
        <RouterLink to="/login">Login</RouterLink>
        <RouterLink to="/register">Registro</RouterLink>
      </template>
    </nav>
  </header>

  <main>
    <RouterView />
  </main>
</template>

<script setup>
import { ref } from 'vue';
import { RouterView, RouterLink, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();
const router = useRouter();

const searchQuery = ref('');
const isDevelopment = import.meta.env.DEV; // Para mostrar el enlace de Ajustes (Dev)

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};

const performSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ name: 'search-games', query: { q: searchQuery.value.trim() } });
    // Opcional: limpiar el campo después de la búsqueda
    // searchQuery.value = ''; 
  }
};
</script>

<style scoped>
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1.5rem; /* Ajustado para más espacio horizontal */
  background-color: var(--color-background-mute);
  border-bottom: 1px solid var(--color-border);
  width: 100%;
  box-sizing: border-box;
}

.logo-container {
  display: flex;
  align-items: center;
  margin-right: 1rem; 
}

.logo {
  margin-right: 0.75rem;
  height: 35px; /* Ligeramente más pequeño */
  width: 35px;
}

.app-title {
  font-size: 1.15rem; /* Ligeramente más pequeño */
  font-weight: 600;
  color: var(--color-heading);
  white-space: nowrap;
}

nav {
  display: flex;
  align-items: center;
  gap: 0.5rem; /* Espacio reducido entre elementos principales de nav */
  flex-wrap: nowrap; /* Evitar que la barra de búsqueda y links principales se envuelvan prematuramente */
  flex-grow: 1; /* Permitir que nav crezca para empujar login/registro a la derecha */
  justify-content: flex-start; /* Alinear items a la izquierda dentro de nav */
}

nav a, nav .logout-button, nav .search-button {
  text-decoration: none;
  color: var(--color-text);
  padding: 0.4rem 0.7rem; /* Padding ajustado */
  border-radius: 4px;
  transition: background-color 0.2s, color 0.2s;
  white-space: nowrap;
  font-size: 0.85rem; /* Ligeramente más pequeño */
}

nav a:hover,
nav a.router-link-exact-active,
nav .logout-button:hover,
nav .search-button:hover {
  color: hsla(160, 100%, 37%, 1);
  background-color: var(--color-background-soft);
}

.search-container {
  display: flex;
  align-items: center;
  margin: 0 1rem; /* Margen alrededor del buscador */
  flex-grow: 2; /* Darle más espacio para crecer que a otros links */
  max-width: 400px; /* Limitar el ancho máximo del buscador */
}

.search-container input[type="search"] {
  padding: 0.4rem 0.7rem;
  border: 1px solid var(--color-border);
  border-right: none; /* Unir con el botón */
  border-radius: 4px 0 0 4px; /* Redondear solo esquinas izquierdas */
  background-color: var(--color-background);
  color: var(--color-text);
  font-size: 0.85rem;
  width: 100%; /* Ocupar el espacio disponible en su contenedor */
  min-width: 150px; /* Ancho mínimo */
}
.search-container input[type="search"]:focus {
  outline: none;
  border-color: hsla(160, 100%, 37%, 1);
}

.search-button {
  padding: 0.4rem 0.7rem;
  border: 1px solid var(--color-border);
  border-left: 1px solid hsla(160, 100%, 37%, 0.5); /* Borde para unir visualmente */
  background-color: var(--color-background-soft);
  cursor: pointer;
  border-radius: 0 4px 4px 0; /* Redondear solo esquinas derechas */
  font-size: 0.85rem; /* Para que el icono no sea muy grande */
}


.user-profile-link {
  font-weight: 500;
}

.logout-button {
  background: none;
  border: none;
  cursor: pointer;
}

/* Colocar los enlaces de autenticación y usuario al final */
nav > .search-container + .router-link-active, /* Amigos */
nav > .router-link-active + .user-profile-link,
nav > .user-profile-link + .router-link-active, /* Ajustes Dev */
nav > .router-link-active + .logout-button,
nav > template { /* Contenedor de los links de auth/no-auth */
  margin-left: auto; /* Empuja estos elementos a la derecha del nav */
}


main {
  padding: 2rem;
}
</style>