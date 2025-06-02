<template>
  <div class="view-single-public-gamelist">
    <h1>Lista de Juegos Pública (Detalle)</h1>
    <p>Mostrando detalles para la lista con ID público: {{ listPublicId }}</p>
    <p>(Contenido detallado de la lista pública pendiente de implementación)</p>
    <RouterLink to="/public-gamelists">Volver a todas las listas públicas</RouterLink>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
import { RouterLink } from 'vue-router';

const props = defineProps({
  listPublicId: String
});
</script>

<style scoped>
.view-single-public-gamelist {
  padding: 2rem;
  text-align: center;
}
</style>