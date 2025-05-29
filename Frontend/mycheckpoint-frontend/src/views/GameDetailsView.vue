<template>
  <div class="game-details-view">
    <div v-if="!isLoading && gameDetail && gameDetail.game_info" class="game-content">
      <div class="game-header">
        <img 
          :src="getCoverUrl(gameDetail.game_info.cover, true)" 
          :alt="`Carátula de ${gameDetail.game_info.name}`" 
          class="game-cover-large" 
          @error="onImageError" 
        />
        <div class="game-header-info">
          <h1>{{ gameDetail.game_info.name }}</h1>
          <p class="release-date" v-if="gameDetail.game_info.first_release_date">
            Lanzamiento: {{ formatTimestampToDate(gameDetail.game_info.first_release_date) }}
          </p>
          <div class="genres" v-if="gameDetail.game_info.genres && gameDetail.game_info.genres.length">
            <strong>Géneros:</strong>
            <span v-for="(genre, index) in gameDetail.game_info.genres" :key="genre.id" class="genre-tag">
              {{ genre.name }}{{ index < gameDetail.game_info.genres.length - 1 ? ', ' : '' }}
            </span>
          </div>
          <div class="platforms" v-if="gameDetail.game_info.platforms && gameDetail.game_info.platforms.length">
            <strong>Plataformas:</strong>
            <span v-for="(platform, index) in gameDetail.game_info.platforms" :key="platform.id" class="platform-tag">
              {{ platform.name }}{{ index < gameDetail.game_info.platforms.length - 1 ? ', ' : '' }}
            </span>
          </div>
           <div class="rating" v-if="gameDetail.game_info.total_rating">
            <strong>Puntuación IGDB:</strong> {{ Math.round(gameDetail.game_info.total_rating) }}/100
            <small v-if="gameDetail.game_info.total_rating_count"> ({{ gameDetail.game_info.total_rating_count }} votos)</small>
          </div>
        </div>
      </div>

      <div class="game-summary section-block" v-if="gameDetail.game_info.summary">
        <h2>Resumen</h2>
        <p>{{ gameDetail.game_info.summary }}</p>
      </div>

      <div class="game-storyline section-block" v-if="gameDetail.game_info.storyline">
        <h2>Argumento</h2>
        <p>{{ gameDetail.game_info.storyline }}</p>
      </div>


      <div class="related-games-section section-block" v-if="gameDetail.game_info.similar_games && gameDetail.game_info.similar_games.length">
        <h2>Juegos Similares</h2>
        <div class="related-games-grid">
          <div v-for="similarGame in gameDetail.game_info.similar_games" :key="similarGame.id" class="related-game-card">
            <RouterLink :to="{ name: 'game-details', params: { igdbId: similarGame.id } }">
              <img :src="getCoverUrl(similarGame.cover, false)" :alt="`Carátula de ${similarGame.name}`" class="related-game-cover" @error="onImageErrorSmall" />
              <span class="related-game-name">{{ similarGame.name }}</span>
            </RouterLink>
          </div>
        </div>
      </div>

      <div class="related-games-section section-block" v-if="gameDetail.game_info.dlcs && gameDetail.game_info.dlcs.length">
        <h2>DLCs</h2>
        <div class="related-games-grid">
          <div v-for="dlc in gameDetail.game_info.dlcs" :key="dlc.id" class="related-game-card">
            <RouterLink :to="{ name: 'game-details', params: { igdbId: dlc.id } }">
              <img :src="getCoverUrl(dlc.cover, false)" :alt="`Carátula de ${dlc.name}`" class="related-game-cover" @error="onImageErrorSmall" />
              <span class="related-game-name">{{ dlc.name }}</span>
            </RouterLink>
          </div>
        </div>
      </div>
      
      <div class="related-games-section section-block" v-if="gameDetail.game_info.expansions && gameDetail.game_info.expansions.length">
        <h2>Expansiones</h2>
        <div class="related-games-grid">
          <div v-for="expansion in gameDetail.game_info.expansions" :key="expansion.id" class="related-game-card">
            <RouterLink :to="{ name: 'game-details', params: { igdbId: expansion.id } }">
              <img :src="getCoverUrl(expansion.cover, false)" :alt="`Carátula de ${expansion.name}`" class="related-game-cover" @error="onImageErrorSmall" />
              <span class="related-game-name">{{ expansion.name }}</span>
            </RouterLink>
          </div>
        </div>
      </div>
      </div>
    </div>
</template>

<script setup>
// ... (el script setup existente, incluyendo getCoverUrl, onImageError, formatTimestampToDate)
import { ref, onMounted, watch } from 'vue';
import { useRoute, RouterLink } from 'vue-router'; // RouterLink importado aquí
import { fetchGameDetailsByIgdbId } from '@/services/apiInstances';
import { useAuthStore } from '@/stores/authStore';
import defaultGameCoverLarge from '@/assets/default-game-cover-large.png'; // Placeholder para portada principal
import defaultRelatedCover from '@/assets/default-related-cover.png'; // Placeholder para portadas pequeñas

const route = useRoute();
const authStore = useAuthStore();

const gameDetail = ref(null);
const isLoading = ref(true);
const errorMessage = ref('');
const igdbId = ref(null);

const loadGameDetails = async (id) => {
  // ... (lógica existente)
  if (!id) {
    errorMessage.value = "ID del juego no proporcionado.";
    isLoading.value = false;
    return;
  }
  isLoading.value = true;
  errorMessage.value = '';
  gameDetail.value = null;

  try {
    const response = await fetchGameDetailsByIgdbId(Number(id));
    gameDetail.value = response.data;
    console.log("Detalles del juego recibidos:", gameDetail.value);
  } catch (error) {
    console.error(`Error cargando detalles del juego (ID: ${id}):`, error);
    if (error.response) {
      errorMessage.value = `Error ${error.response.status}: ${error.response.data.message || error.response.data.error || 'No se pudieron cargar los detalles.'}`;
    } else {
      errorMessage.value = 'Error de red al cargar los detalles del juego.';
    }
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  igdbId.value = route.params.igdbId;
  loadGameDetails(igdbId.value);
});

watch(() => route.params.igdbId, (newId) => {
  if (newId && newId !== igdbId.value) {
    igdbId.value = newId;
    loadGameDetails(newId);
  }
});

const getCoverUrl = (coverInfo, isMainCover = false) => {
  const placeholder = isMainCover ? defaultGameCoverLarge : defaultRelatedCover;
  
  if (coverInfo && typeof coverInfo.url === 'string' && coverInfo.url.trim() !== '') {
    let imageUrl = coverInfo.url;

    // 1. Asegurar el protocolo HTTPS si la URL empieza con //
    if (imageUrl.startsWith('//')) {
      imageUrl = `https:${imageUrl}`;
    }

    // 2. Intentar cambiar el tamaño de la imagen.
    // Para la portada principal, queremos un tamaño grande.
    // Para las relacionadas, uno más pequeño.
    const targetSize = isMainCover ? 't_cover_big' : 't_cover_small'; // O 't_720p' para principal, 't_thumb' o 't_cover_small' para relacionadas.

    // Lista de tamaños pequeños comunes que podríamos querer reemplazar
    const smallSizes = ['/t_thumb/', '/t_cover_small/', '/t_micro/', '/t_logo_med/'];
    let replaced = false;

    for (const smallSize of smallSizes) {
      if (imageUrl.includes(smallSize)) {
        imageUrl = imageUrl.replace(smallSize, `/${targetSize}/`);
        replaced = true;
        break; 
      }
    }

    // Si no se reemplazó ningún tamaño pequeño (ej. la URL ya tiene un tamaño grande o es un formato diferente)
    // y *no* es la imagen principal, podríamos querer forzar un tamaño más pequeño si la URL parece ser de una imagen original muy grande.
    // Pero esto es más complejo. Por ahora, si no se reemplaza, usamos la URL como está.
    // Si es la imagen principal y queremos asegurar un tamaño grande específico (y no es ya uno), podríamos añadir lógica,
    // pero el reemplazo de los pequeños a 't_cover_big' o 't_720p' suele ser suficiente.
    // Si es la imagen principal y queremos 't_720p' y la original no tenía un especificador de tamaño:
    if (isMainCover && !replaced && !imageUrl.includes(`/${targetSize}/`) && targetSize === 't_720p') {
        // Esto es más especulativo: si la URL es de tipo /upload/IMAGE_ID.jpg
        // podríamos intentar insertar /t_720p/.
        // Ejemplo: imageUrl.replace('/upload/', '/upload/t_720p/');
        // Pero por ahora, nos enfocaremos en reemplazar tamaños conocidos.
    }


    // console.log(`getCoverUrl: URL procesada para ${isMainCover ? 'principal' : 'relacionada'}: ${imageUrl}`);
    return imageUrl;
  }
  
  // console.warn(`getCoverUrl: Devolviendo placeholder para ${isMainCover ? 'principal' : 'relacionada'}. CoverInfo:`, coverInfo);
  return placeholder;
};


const onImageError = (event) => {
  console.warn("Error al cargar imagen de portada grande:", event.target.src);
  event.target.src = defaultGameCoverLarge;
};

const onImageErrorSmall = (event) => { // Nueva función para placeholders de imágenes más pequeñas
  console.warn("Error al cargar imagen de portada relacionada:", event.target.src);
  event.target.src = defaultRelatedCover;
};

const formatTimestampToDate = (timestamp) => {
  if (!timestamp) return 'Fecha desconocida';
  return new Date(Number(timestamp) * 1000).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

</script>

<style scoped>
/* ... (estilos existentes para .game-details-view, .game-header, etc.) ... */
.game-details-view {
  max-width: 960px;
  margin: 0 auto;
  padding: 1rem;
}
.loading-message, .error-message, .no-results-message {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
}
.error-message { color: red; }
.game-content {
  background-color: var(--color-background-soft);
  padding: 1.5rem;
  border-radius: 8px;
}
.game-header {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  align-items: flex-start;
}
.game-cover-large {
  width: 250px;
  min-width: 200px; /* Para evitar que se encoja demasiado */
  aspect-ratio: 3/4;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background-color: var(--color-background-mute);
}
.game-header-info { flex: 1; }
.game-header-info h1 { margin-top: 0; margin-bottom: 0.75rem; color: var(--color-heading); }
.release-date, .genres, .platforms, .rating { font-size: 0.9rem; color: var(--color-text); margin-bottom: 0.5rem; }
.section-block { margin-bottom: 1.5rem; padding-top: 1rem; border-top: 1px solid var(--color-border-hover); }
.section-block h2 { margin-top: 0; margin-bottom: 0.75rem; font-size: 1.3rem; }
.section-block p { line-height: 1.6; white-space: pre-wrap; }

/* Nuevos estilos para las secciones de juegos relacionados */
.related-games-section h2 {
  font-size: 1.4rem;
  margin-bottom: 1rem;
  color: var(--color-heading);
}

.related-games-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr)); /* Tarjetas más pequeñas */
  gap: 1rem;
}

.related-game-card {
  background-color: var(--color-background-mute);
  border-radius: 6px;
  overflow: hidden;
  text-align: center;
  transition: transform 0.2s ease-in-out;
}
.related-game-card:hover {
  transform: translateY(-4px);
}

.related-game-card a {
  text-decoration: none;
  color: inherit;
  display: block;
}

.related-game-cover {
  width: 100%;
  aspect-ratio: 3 / 4; /* Mantener proporción */
  object-fit: cover;
  background-color: var(--color-background); /* Un placeholder más oscuro */
  margin-bottom: 0.5rem;
}

.related-game-name {
  font-size: 0.85rem;
  padding: 0 0.5rem 0.5rem 0.5rem;
  display: block; /* Para asegurar que el padding funcione bien */
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>