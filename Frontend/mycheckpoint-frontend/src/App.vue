<template>
  <div id="app-container">
    <Header />
    <main class="main-content">
      <RouterView />
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router';
import Header from '@/components/Header/Header.vue';
import Footer from '@/components/Footer/Footer.vue';
</script>

<style scoped>
#app-container {
  display: flex;
  flex-direction: column;
  min-width: 100%;
  min-height: 100%;
}

.main-content {
  flex-grow: 1;
  padding: 1.5rem;
  width: 60%;
  margin: 0 20%;
  padding: 0 2rem;
}

@media (max-width: 1024px) {
  .main-content {
    width: 100%;
    margin: 0;
    padding: 1rem;
  }
}
</style>