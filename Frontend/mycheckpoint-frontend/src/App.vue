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
  min-height: 100vh;
}

.main-content {
  flex-grow: 1;
  padding: 1.5rem;
  width: 80%;
  margin: 0 10%;
  padding: 0 2rem;
}

@media (max-width: 1324px) {
  .main-content {
    width: 90%;
    margin: 0 5%;
    padding: 1rem;
  }
}
</style>