<template>
  <header>
    <div class="logo-container">
      <img alt="MyCheckPoint Logo" class="logo" src="@/assets/logo.svg" width="40" height="40" />
      <span class="app-title">MyCheckPoint</span>
    </div>
    <nav>
      <RouterLink to="/">Home</RouterLink>
      <RouterLink to="/search-games">Buscar Juegos</RouterLink>
      <RouterLink to="/public-gamelists">Listas Públicas</RouterLink>
      <RouterLink to="/public-tierlists">Tier Lists Públicas</RouterLink>
      <RouterLink to="/search-users">Buscar Usuarios</RouterLink>

      <template v-if="authStore.isAuthenticated && authStore.currentUser">
        <RouterLink to="/friends">Amigos</RouterLink>
        <RouterLink :to="`/profile/${authStore.currentUser.public_id}`" class="user-profile-link">
          {{ authStore.currentUser.nombre_usuario }}
        </RouterLink>
        <RouterLink to="/settings">Ajustes (Dev)</RouterLink> 
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
import { RouterView, RouterLink } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'vue-router';
// import defaultProfileIcon from '@/assets/default-profile-icon.svg'; // Ejemplo si tienes un icono por defecto

const authStore = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
header {
  background-color: var(--color-background-mute); /* */
  padding: 0.75rem 2rem; /* Ajustado para ser un poco menos alto */
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--color-border); /* */
  flex-wrap: wrap; /* Para mejor responsive si hay muchos items */
}

.logo-container {
  display: flex;
  align-items: center;
  margin-right: 2rem; /* Espacio entre logo y primer item de nav */
}

.logo {
  margin-right: 0.75rem; /* Espacio entre el svg y el título */
  height: 40px; /* Coincidir con el width/height del img tag */
  width: 40px;
}

.app-title {
  font-size: 1.25rem; /* Ligeramente más pequeño */
  font-weight: 600; /* Un poco menos bold */
  color: var(--color-heading); /* */
}

nav {
  display: flex;
  align-items: center;
  gap: 0.75rem; /* Espacio reducido entre links */
  flex-wrap: wrap; /* Para que los links pasen a la siguiente línea en pantallas pequeñas */
}

nav a, nav .logout-button { /* Aplicar estilos también al botón de logout */
  text-decoration: none;
  color: var(--color-text); /* */
  padding: 0.4rem 0.8rem; /* Padding ajustado */
  border-radius: 4px;
  transition: background-color 0.2s, color 0.2s;
  white-space: nowrap; /* Evitar que el texto del enlace se parta */
  font-size: 0.9rem; /* Tamaño de fuente de los enlaces */
}

nav a:hover,
nav a.router-link-exact-active,
nav .logout-button:hover {
  color: hsla(160, 100%, 37%, 1); /* */
  background-color: var(--color-background-soft); /* */
}

.user-profile-link {
  /* Estilos adicionales si quieres destacar el nombre de usuario */
  font-weight: 500;
}

.logout-button {
  background: none;
  border: none;
  cursor: pointer;
  /* Hereda padding y otros estilos de nav a */
}

/* Opcional: para iconos de perfil en la navegación */
/*
.profile-icon-nav {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  margin-right: 0.5em;
  vertical-align: middle;
}
*/

main {
  padding: 2rem;
}

/* Media query para mejorar la visualización en pantallas más pequeñas */
@media (max-width: 768px) {
  header {
    flex-direction: column;
    align-items: flex-start;
  }
  nav {
    margin-top: 1rem;
    width: 100%;
    justify-content: flex-start; /* O center, según preferencia */
    gap: 0.5rem;
  }
  nav a, nav .logout-button {
    padding: 0.5rem; /* Ajustar padding para más items */
  }
  .app-title {
    font-size: 1.1rem;
  }
}
</style>