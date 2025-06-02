<template>
  <div class="view-single-public-tierlist">
    <h1>Tier List Pública (Detalle)</h1>
    <p>Mostrando detalles para la tier list con ID público: {{ tierListPublicId }}</p>
    <p>(Contenido detallado de la tier list pública pendiente de implementación)</p>
    <RouterLink to="/public-tierlists">Volver a todas las tier lists públicas</RouterLink>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
import { RouterLink } from 'vue-router';

const props = defineProps({
  tierListPublicId: String
});
</script>

<style scoped>
.view-single-public-tierlist {
  padding: 2rem;
  text-align: center;
}
</style>