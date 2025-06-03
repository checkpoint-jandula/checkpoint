import { createRouter, createWebHistory } from "vue-router";
import HomeView from "../views/HomeView/HomeView.vue";
import LoginView from "../views/LoginView/LoginView.vue";
import RegisterView from "../views/RegisterView/RegisterView.vue";
import UserSettingsView from "../views/UserSettingsView/UserSettingsView.vue";
import UserProfileView from "../views/UserProfileView/UserProfileView.vue";
// MyLibraryView, MyGameListsView, MyTierListsView serán ahora parte de UserProfileView o cargados por él
import FriendsPageView from "../views/FriendsPageView/FriendsPageView.vue";
import RequestPasswordResetView from "../views/RequestPasswordResetView/RequestPasswordResetView.vue";
import ResetPasswordView from "../views/ResetPasswordView/ResetPasswordView.vue";
import SearchGamesView from "../views/SearchGamesView/SearchGamesView.vue";
import GameDetailsView from "../views/GameDetails/GameDetailsView.vue";

// Vistas para las listas/tiers públicos y búsqueda de usuarios
import ViewPublicGameListsView from "../views/ViewPublicGameListsView/ViewPublicGameListsView.vue"; // Crear este placeholder
import ViewPublicTierListsView from "../views/ViewPublicTierListsView/ViewPublicTierListsView.vue"; // Crear este placeholder
import ViewSinglePublicTierListView from "../views/ViewSinglePublicTierListView/ViewSinglePublicTierListView.vue"; // Crear este placeholder
import GameListDetailView from "../views/GameListDetailView/GameListDetailView.vue"; // Crear este placeholder
import SearchUsersView from "../views/SearchUsersView/SearchUsersView.vue"; // Crear este placeholder

import { useAuthStore } from "@/stores/authStore";

const routes = [
  { path: "/", name: "home", component: HomeView },
  {
    path: "/login",
    name: "login",
    component: LoginView,
    meta: { guestOnly: true },
  },
  {
    path: "/register",
    name: "register",
    component: RegisterView,
    meta: { guestOnly: true },
  },
  {
    path: "/request-password-reset",
    name: "request-password-reset",
    component: RequestPasswordResetView,
    meta: { guestOnly: true },
  },
  {
    path: "/resetear-password",
    name: "reset-password",
    component: ResetPasswordView,
    meta: { guestOnly: true },
  },
  {
    path: "/profile/:publicId",
    name: "profile",
    component: UserProfileView,
    props: true,
    // Dentro de UserProfileView se manejará la lógica para mostrar
    // la biblioteca, listas, tiers y ajustes si es el perfil del usuario logueado.
  },
  {
    path: "/settings", // Esta ruta ahora se accederá desde el perfil del usuario.
    name: "settings",
    component: UserSettingsView, // UserSettingsView se podría cargar dentro de UserProfileView
    meta: { requiresAuth: true },
  },
  {
    path: "/friends",
    name: "friends",
    component: FriendsPageView,
    meta: { requiresAuth: true },
  },
  {
    path: "/search-games",
    name: "search-games",
    component: SearchGamesView,
  },
  {
    path: "/games/:igdbId/details",
    name: "game-details",
    component: GameDetailsView,
    props: true,
  },
  {
    path: "/public-gamelists",
    name: "public-gamelists",
    component: ViewPublicGameListsView, // Crear este componente placeholder
  },
  {
    path: "/gamelists/public/:listPublicId", // RUTA para ver una lista pública
    name: "view-public-gamelist",
    component: GameListDetailView, 
    props: true,
  },
  {
    path: "/my-gamelists/:listPublicId/detail", // Ruta para una lista específica del usuario
    name: "gamelist-detail",
    component: GameListDetailView,
    props: true, // Para pasar listPublicId como prop al componente
    meta: { requiresAuth: true }, // Esta vista requiere autenticación
  },
  {
    path: "/public-tierlists",
    name: "public-tierlists",
    component: ViewPublicTierListsView, // Crear este componente placeholder
  },
  {
    path: "/tierlists/public/:tierListPublicId", // NUEVA RUTA para ver una tier list pública
    name: "view-public-tierlist",
    component: ViewSinglePublicTierListView, // Placeholder por ahora
    props: true,
  },
  {
    path: "/search-users",
    name: "search-users",
    component: SearchUsersView, // Crear este componente placeholder
  },
  // Las rutas como /my-library, /my-gamelists, /my-tierlists ya no son necesarias aquí
  // como rutas de primer nivel si se integran en UserProfileView.
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();

  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (!authStore.isAuthenticated) {
      next({ name: "login", query: { redirect: to.fullPath } });
    } else {
      next();
    }
  } else if (to.matched.some((record) => record.meta.guestOnly)) {
    if (authStore.isAuthenticated) {
      next({ name: "home" });
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router;
