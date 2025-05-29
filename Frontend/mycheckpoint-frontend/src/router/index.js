import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import UserSettingsView from '../views/UserSettingsView.vue';
import UserProfileView from '../views/UserProfileView.vue';
// MyLibraryView, MyGameListsView, MyTierListsView serán ahora parte de UserProfileView o cargados por él
import FriendsPageView from '../views/FriendsPageView.vue';
import RequestPasswordResetView from '../views/RequestPasswordResetView.vue';
import ResetPasswordView from '../views/ResetPasswordView.vue';
import SearchGamesView from '../views/SearchGamesView.vue';
import GameDetailsView from '../views/GameDetailsView.vue';

// Vistas para las listas/tiers públicos y búsqueda de usuarios
import ViewPublicGameListsView from '../views/ViewPublicGameListsView.vue'; // Crear este placeholder
import ViewPublicTierListsView from '../views/ViewPublicTierListsView.vue'; // Crear este placeholder
import SearchUsersView from '../views/SearchUsersView.vue';         // Crear este placeholder


import { useAuthStore } from '@/stores/authStore';

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/login', name: 'login', component: LoginView, meta: { guestOnly: true } },
  { path: '/register', name: 'register', component: RegisterView, meta: { guestOnly: true } },
  { 
    path: '/request-password-reset', 
    name: 'request-password-reset', 
    component: RequestPasswordResetView,
    meta: { guestOnly: true }
  },
  { 
    path: '/resetear-password',
    name: 'reset-password', 
    component: ResetPasswordView,
    meta: { guestOnly: true }
  },
  { 
    path: '/profile/:publicId',
    name: 'profile', 
    component: UserProfileView,
    props: true 
    // Dentro de UserProfileView se manejará la lógica para mostrar
    // la biblioteca, listas, tiers y ajustes si es el perfil del usuario logueado.
  },
  { 
    path: '/settings', // Esta ruta ahora se accederá desde el perfil del usuario.
    name: 'settings', 
    component: UserSettingsView, // UserSettingsView se podría cargar dentro de UserProfileView
    meta: { requiresAuth: true } 
  },
  { 
    path: '/friends', 
    name: 'friends', 
    component: FriendsPageView,
    meta: { requiresAuth: true }
  },
  {
    path: '/search-games',
    name: 'search-games',
    component: SearchGamesView
  },
  {
    path: '/games/:igdbId/details',
    name: 'game-details',
    component: GameDetailsView,
    props: true
  },
  {
    path: '/public-gamelists',
    name: 'public-gamelists',
    component: ViewPublicGameListsView // Crear este componente placeholder
  },
  {
    path: '/public-tierlists',
    name: 'public-tierlists',
    component: ViewPublicTierListsView // Crear este componente placeholder
  },
  {
    path: '/search-users',
    name: 'search-users',
    component: SearchUsersView // Crear este componente placeholder
  }
  // Las rutas como /my-library, /my-gamelists, /my-tierlists ya no son necesarias aquí
  // como rutas de primer nivel si se integran en UserProfileView.
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();

  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!authStore.isAuthenticated) {
      next({ name: 'login', query: { redirect: to.fullPath } });
    } else {
      next();
    }
  } else if (to.matched.some(record => record.meta.guestOnly)) {
    if (authStore.isAuthenticated) {
      next({ name: 'home' });
    } else {
      next();
    }
  }
  else {
    next();
  }
});

export default router;