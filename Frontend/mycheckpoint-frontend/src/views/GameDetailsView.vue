<template>
  <div class="game-details-view">
    <div v-if="isLoading" class="loading-message">Cargando detalles del juego...</div>
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

    <div v-if="!isLoading && gameDetail && gameDetail.game_info" class="game-content-wrapper">
      <section class="game-main-header section-block">
        <img 
          :src="getCoverUrl(gameDetail.game_info.cover, 't_720p', 'mainCover')"
          :alt="`Carátula de ${gameDetail.game_info.name || 'Juego'}`" 
          class="game-cover-main" 
          @error="onImageError" 
        />
        <div class="game-title-meta">
          <h1>{{ gameDetail.game_info.name }}</h1>
          <div class="meta-grid">
            <div v-if="gameDetail.game_info.game_type" class="meta-item">
              <strong>Tipo:</strong>
              <span>{{ formatGameType(gameDetail.game_info.game_type) }}</span>
            </div>
            <div v-if="gameDetail.game_info.first_release_date" class="meta-item">
              <strong>Lanzamiento:</strong>
              <span>{{ formatTimestampToDate(gameDetail.game_info.first_release_date) }}</span>
            </div>
            <div v-if="gameDetail.game_info.first_release_status !== null && gameDetail.game_info.first_release_status !== undefined" class="meta-item">
              <strong>Estado:</strong>
              <span>{{ formatReleaseStatus(gameDetail.game_info.first_release_status) }}</span>
            </div>
            <div v-if="gameDetail.game_info.total_rating" class="meta-item">
              <strong>Puntuación IGDB:</strong> 
              <span>{{ Math.round(gameDetail.game_info.total_rating) }}/100</span>
              <small v-if="gameDetail.game_info.total_rating_count"> ({{ gameDetail.game_info.total_rating_count }} votos)</small>
            </div>
          </div>
          <div class="tags-container header-tags">
            <span v-for="genre in gameDetail.game_info.genres" :key="genre.id" class="tag genre-tag">
              {{ genre.name }}
            </span>
            <span v-for="theme in gameDetail.game_info.themes" :key="theme.id" class="tag theme-tag">
              {{ theme.name }}
            </span>
          </div>
        </div>
      </section>

      <nav class="tabs-navigation">
        <button @click="setActiveTab('details')" :class="{ 'active-tab': activeTab === 'details' }">
          Detalles
        </button>
        <button @click="setActiveTab('multimedia')" :class="{ 'active-tab': activeTab === 'multimedia' }">
          Multimedia
        </button>
        <button @click="setActiveTab('related')" :class="{ 'active-tab': activeTab === 'related' }">
          Contenido Relacionado
        </button>
        <button @click="setActiveTab('community')" :class="{ 'active-tab': activeTab === 'community' }">
          Comunidad y Mis Datos
        </button>
      </nav>

      <div class="tab-content">
        <div v-show="activeTab === 'details'" class="tab-pane">
          <section class="game-summary section-block" v-if="gameDetail.game_info.summary">
            <h2>Resumen</h2>
            <p>{{ gameDetail.game_info.summary }}</p>
          </section>

          <section class="game-storyline section-block" v-if="gameDetail.game_info.storyline">
            <h2>Argumento</h2>
            <p>{{ gameDetail.game_info.storyline }}</p>
          </section>

          <section class="metadata-lists section-block">
            <div v-if="gameDetail.game_info.platforms && gameDetail.game_info.platforms.length" class="metadata-group">
              <h3>Plataformas</h3>
              <div class="tags-container">
                <span v-for="platform in gameDetail.game_info.platforms" :key="platform.id" class="tag platform-tag">
                  {{ platform.name }}
                </span>
              </div>
            </div>
            <div v-if="gameDetail.game_info.game_modes && gameDetail.game_info.game_modes.length" class="metadata-group">
              <h3>Modos de Juego</h3>
              <div class="tags-container">
                <span v-for="mode in gameDetail.game_info.game_modes" :key="mode.id" class="tag mode-tag">
                  {{ mode.name }}
                </span>
              </div>
            </div>
            <div v-if="gameDetail.game_info.game_engines && gameDetail.game_info.game_engines.length" class="metadata-group">
              <h3>Motores Gráficos</h3>
              <div class="tags-container">
                <span v-for="engine in gameDetail.game_info.game_engines" :key="engine.id" class="tag engine-tag">
                  {{ engine.name }}
                </span>
              </div>
            </div>
             <div v-if="gameDetail.game_info.franchises && gameDetail.game_info.franchises.length" class="metadata-group">
              <h3>Franquicias</h3>
              <div class="tags-container">
                <span v-for="franchise in gameDetail.game_info.franchises" :key="franchise.id" class="tag franchise-tag">
                  {{ franchise.name }}
                </span>
              </div>
            </div>
          </section>

          <section class="involved-companies-section section-block" v-if="gameDetail.game_info.involved_companies && gameDetail.game_info.involved_companies.length">
            <h2>Compañías Involucradas</h2>
            <ul class="companies-list">
              <li v-for="involvement in gameDetail.game_info.involved_companies" :key="involvement.id" class="company-item">
                <span class="company-name">{{ involvement.company.name }}</span>
                <span v-if="getCompanyRoles(involvement)" class="company-roles"> ({{ getCompanyRoles(involvement) }})</span>
              </li>
            </ul>
          </section>

          <section class="keywords-section section-block" v-if="gameDetail.game_info.keywords && gameDetail.game_info.keywords.length">
            <h2>Palabras Clave</h2>
            <div class="tags-container">
              <span v-for="keyword in gameDetail.game_info.keywords" :key="keyword.id" class="tag keyword-tag">
                {{ keyword.name }}
              </span>
            </div>
          </section>

          <section class="websites-section section-block" v-if="gameDetail.game_info.websites && gameDetail.game_info.websites.length">
            <h2>Sitios Web</h2>
            <ul class="websites-list">
              <li v-for="website in gameDetail.game_info.websites" :key="website.id">
                <a :href="website.url" target="_blank" rel="noopener noreferrer" class="website-link">
                  {{ getWebsiteDisplayName(website.url) }}
                </a>
              </li>
            </ul>
          </section>
        </div>

        <div v-show="activeTab === 'multimedia'" class="tab-pane">
          <section class="artworks-section section-block" v-if="gameDetail.game_info.artworks && gameDetail.game_info.artworks.length">
            <h2>Ilustraciones</h2>
            <div class="gallery-grid">
              <div v-for="artwork in gameDetail.game_info.artworks" :key="artwork.id" class="gallery-item">
                <a :href="getCoverUrl(artwork, 't_original', 'artwork')" target="_blank" rel="noopener noreferrer">
                  <img :src="getCoverUrl(artwork, 't_screenshot_med', 'artwork')" :alt="`Artwork ${artwork.id}`" class="gallery-image"/> 
                </a>
              </div>
            </div>
          </section>

          <section class="screenshots-section section-block" v-if="gameDetail.game_info.screenshots && gameDetail.game_info.screenshots.length">
            <h2>Capturas de Pantalla</h2>
            <div class="gallery-grid">
              <div v-for="screenshot in gameDetail.game_info.screenshots" :key="screenshot.id" class="gallery-item">
                <a :href="getCoverUrl(screenshot, 't_original', 'screenshot')" target="_blank" rel="noopener noreferrer">
                  <img :src="getCoverUrl(screenshot, 't_screenshot_med', 'screenshot')" :alt="`Screenshot ${screenshot.id}`" class="gallery-image"/>
                </a>
              </div>
            </div>
          </section>

          <section class="videos-section section-block" v-if="gameDetail.game_info.videos && gameDetail.game_info.videos.length">
            <h2>Vídeos</h2>
            <div class="videos-grid">
              <div v-for="video in gameDetail.game_info.videos" :key="video.id" class="video-item">
                <h4 class="video-name" v-if="video.name">{{ video.name }}</h4>
                <div class="video-embed-container" v-if="video.video_id">
                  <iframe :src="getYouTubeEmbedUrl(video.video_id)" :title="video.name || 'Vídeo del juego'" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
                </div>
                <p v-else class="no-video-id">ID de vídeo no disponible.</p>
              </div>
            </div>
          </section>
        </div>
        
        <div v-show="activeTab === 'related'" class="tab-pane">
          <section class="related-content-section section-block">
            <div class="related-list" v-if="gameDetail.game_info.parent_game">
              <h3>Juego Principal</h3>
              <div class="related-games-grid single-item-grid">
                  <div class="related-game-card">
                      <RouterLink :to="{ name: 'game-details', params: { igdbId: gameDetail.game_info.parent_game.id } }">
                          <img :src="getCoverUrl(gameDetail.game_info.parent_game.cover, 't_cover_small', 'relatedCover')" :alt="gameDetail.game_info.parent_game.name" class="related-game-cover" @error="onImageErrorSmall" />
                          <span class="related-game-name">{{ gameDetail.game_info.parent_game.name }}</span>
                      </RouterLink>
                  </div>
              </div>
            </div>
            <div class="related-list" v-if="gameDetail.game_info.version_parent">
              <h3>Versión Principal</h3>
               <div class="related-games-grid single-item-grid">
                  <div class="related-game-card">
                      <RouterLink :to="{ name: 'game-details', params: { igdbId: gameDetail.game_info.version_parent.id } }">
                          <img :src="getCoverUrl(gameDetail.game_info.version_parent.cover, 't_cover_small', 'relatedCover')" :alt="gameDetail.game_info.version_parent.name" class="related-game-cover" @error="onImageErrorSmall" />
                          <span class="related-game-name">{{ gameDetail.game_info.version_parent.name }}</span>
                      </RouterLink>
                  </div>
              </div>
            </div>

            <div class="related-list" v-if="gameDetail.game_info.dlcs && gameDetail.game_info.dlcs.length">
              <h3>DLCs</h3>
              <div class="related-games-grid">
                <div v-for="dlc in gameDetail.game_info.dlcs" :key="dlc.id" class="related-game-card">
                  <RouterLink :to="{ name: 'game-details', params: { igdbId: dlc.id } }">
                    <img :src="getCoverUrl(dlc.cover, 't_cover_small', 'relatedCover')" :alt="dlc.name" class="related-game-cover" @error="onImageErrorSmall" />
                    <span class="related-game-name">{{ dlc.name }}</span>
                  </RouterLink>
                </div>
              </div>
            </div>
            
            <div class="related-list" v-if="gameDetail.game_info.expansions && gameDetail.game_info.expansions.length">
              <h3>Expansiones</h3>
              <div class="related-games-grid">
                <div v-for="expansion in gameDetail.game_info.expansions" :key="expansion.id" class="related-game-card">
                  <RouterLink :to="{ name: 'game-details', params: { igdbId: expansion.id } }">
                    <img :src="getCoverUrl(expansion.cover, 't_cover_small', 'relatedCover')" :alt="expansion.name" class="related-game-cover" @error="onImageErrorSmall" />
                    <span class="related-game-name">{{ expansion.name }}</span>
                  </RouterLink>
                </div>
              </div>
            </div>

            <div class="related-list" v-if="gameDetail.game_info.bundles && gameDetail.game_info.bundles.length">
              <h3>Paquetes (Bundles)</h3>
              <div class="related-games-grid">
                <div v-for="bundleItem in gameDetail.game_info.bundles" :key="bundleItem.id" class="related-game-card">
                  <RouterLink :to="{ name: 'game-details', params: { igdbId: bundleItem.id } }">
                    <img :src="getCoverUrl(bundleItem.cover, 't_cover_small', 'relatedCover')" :alt="bundleItem.name" class="related-game-cover" @error="onImageErrorSmall" />
                    <span class="related-game-name">{{ bundleItem.name }}</span>
                  </RouterLink>
                </div>
              </div>
            </div>

            <div class="related-list" v-if="gameDetail.game_info.remakes && gameDetail.game_info.remakes.length">
              <h3>Remakes</h3>
              <div class="related-games-grid">
                <div v-for="remake in gameDetail.game_info.remakes" :key="remake.id" class="related-game-card">
                  <RouterLink :to="{ name: 'game-details', params: { igdbId: remake.id } }">
                    <img :src="getCoverUrl(remake.cover, 't_cover_small', 'relatedCover')" :alt="remake.name" class="related-game-cover" @error="onImageErrorSmall" />
                    <span class="related-game-name">{{ remake.name }}</span>
                  </RouterLink>
                </div>
              </div>
            </div>

            <div class="related-list" v-if="gameDetail.game_info.remasters && gameDetail.game_info.remasters.length">
              <h3>Remasters</h3>
              <div class="related-games-grid">
                <div v-for="remaster in gameDetail.game_info.remasters" :key="remaster.id" class="related-game-card">
                  <RouterLink :to="{ name: 'game-details', params: { igdbId: remaster.id } }">
                    <img :src="getCoverUrl(remaster.cover, 't_cover_small', 'relatedCover')" :alt="remaster.name" class="related-game-cover" @error="onImageErrorSmall" />
                    <span class="related-game-name">{{ remaster.name }}</span>
                  </RouterLink>
                </div>
              </div>
            </div>
            
            <div class="related-list" v-if="gameDetail.game_info.similar_games && gameDetail.game_info.similar_games.length">
              <h3>Juegos Similares</h3>
              <div class="related-games-grid">
                <div v-for="similarGame in gameDetail.game_info.similar_games" :key="similarGame.id" class="related-game-card">
                  <RouterLink :to="{ name: 'game-details', params: { igdbId: similarGame.id } }">
                    <img :src="getCoverUrl(similarGame.cover, 't_cover_small', 'relatedCover')" :alt="similarGame.name" class="related-game-cover" @error="onImageErrorSmall" />
                    <span class="related-game-name">{{ similarGame.name }}</span>
                  </RouterLink>
                </div>
              </div>
            </div>
          </section>
        </div>

        <div v-show="activeTab === 'community'" class="tab-pane">
          <section 
            class="user-game-data-section section-block" 
            v-if="authStore.isAuthenticated"
          >
            <div v-if="gameDetail.user_game_data">
              <h2>Mis Datos del Juego</h2>
              <div class="user-data-grid">
                  <div v-if="gameDetail.user_game_data.status" class="data-item"><strong>Estado:</strong> {{ formatUserGameStatus(gameDetail.user_game_data.status) }}</div>
                  <div v-if="gameDetail.user_game_data.score !== null && gameDetail.user_game_data.score !== undefined" class="data-item"><strong>Puntuación:</strong> {{ gameDetail.user_game_data.score }}/10</div>
                  <div v-if="gameDetail.user_game_data.personal_platform" class="data-item"><strong>Plataforma:</strong> {{ formatPersonalPlatform(gameDetail.user_game_data.personal_platform) }}</div>
                  <div v-if="gameDetail.user_game_data.has_possession !== null && gameDetail.user_game_data.has_possession !== undefined " class="data-item"><strong>Lo Tengo:</strong> {{ gameDetail.user_game_data.has_possession ? 'Sí' : 'No' }}</div>
                  <div v-if="gameDetail.user_game_data.start_date" class="data-item"><strong>Empezado:</strong> {{ formatDateSimple(gameDetail.user_game_data.start_date) }}</div>
                  <div v-if="gameDetail.user_game_data.end_date" class="data-item"><strong>Terminado:</strong> {{ formatDateSimple(gameDetail.user_game_data.end_date) }}</div>
                  <div v-if="gameDetail.user_game_data.story_duration_hours" class="data-item"><strong>Horas (Historia):</strong> {{ gameDetail.user_game_data.story_duration_hours }}h</div>
                  <div v-if="gameDetail.user_game_data.story_secondary_duration_hours" class="data-item"><strong>Horas (Main+Sides):</strong> {{ gameDetail.user_game_data.story_secondary_duration_hours }}h</div>
                  <div v-if="gameDetail.user_game_data.completionist_duration_hours" class="data-item"><strong>Horas (Completista):</strong> {{ gameDetail.user_game_data.completionist_duration_hours }}h</div>
                  <div v-if="gameDetail.user_game_data.comment" class="data-item full-width"><strong>Comentario Público:</strong><p class="user-comment">{{ gameDetail.user_game_data.comment }}</p></div>
                  <div v-if="gameDetail.user_game_data.private_comment" class="data-item full-width"><strong>Comentario Privado:</strong><p class="user-comment">{{ gameDetail.user_game_data.private_comment }}</p></div>
              </div>
            </div>
            <div v-else-if="!showLibraryForm" class="add-to-library-prompt"> <p>Este juego no está en tu biblioteca.</p>
            </div>

            <div class="library-actions">
              <button 
                v-if="!showLibraryForm" 
                @click="toggleLibraryForm(true, !gameDetail.user_game_data)" 
                class="action-button primary"
              >
                {{ gameDetail.user_game_data ? 'Editar Mis Datos' : 'Añadir a Mi Biblioteca' }}
              </button>
              <button 
                v-if="!showLibraryForm && gameDetail.user_game_data" 
                @click="handleRemoveFromLibrary" 
                :disabled="isLoadingLibraryAction" 
                class="action-button danger"
              >
                {{ isLoadingLibraryAction ? 'Eliminando...' : 'Eliminar de Mi Biblioteca' }}
              </button>
            </div>
            </section>

          <section class="public-comments-section section-block" v-if="gameDetail.public_comments && gameDetail.public_comments.length">
            <h2>Comentarios de la Comunidad</h2>
            <ul class="comments-list">
              <li v-for="comment in gameDetail.public_comments" :key="comment.username + comment.comment_date" class="comment-item">
                <strong class="comment-author">{{ comment.username }}</strong>
                <small class="comment-date"> ({{ formatTimestampToDate(comment.comment_date) }})</small> 
                <p class="comment-text">{{ comment.comment_text }}</p>
              </li>
            </ul>
          </section>
          <section v-else class="public-comments-section section-block">
             <h2>Comentarios de la Comunidad</h2>
            <p>Aún no hay comentarios para este juego.</p>
          </section>
        </div>
      </div>

    </div> <div v-if="showLibraryForm" class="modal-overlay" @click.self="toggleLibraryForm(false, false)"> <div class="modal-panel">
        <form @submit.prevent="handleSaveToLibrary" class="library-form-modal">
          <div class="modal-header">
            <h3>{{ isAddingNewLibraryEntry ? 'Añadir a' : 'Editar en' }} Mi Biblioteca: {{ gameDetail?.game_info?.name }}</h3>
            <button type="button" @click="toggleLibraryForm(false, false)" class="modal-close-button" aria-label="Cerrar">&times;</button>
          </div>
          
          <div class="modal-body">
            <div class="form-grid">
              <div class="form-group">
                <label for="lib-status">Estado:</label>
                <select id="lib-status" v-model="libraryForm.status">
                  <option :value="null">-- Sin especificar --</option>
                  <option v-for="opt in gameStatusOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
                </select>
              </div>
              <div class="form-group">
                <label for="lib-score">Puntuación (0-10):</label>
                <input type="number" id="lib-score" v-model.number="libraryForm.score" min="0" max="10" step="0.5" />
              </div>
              <div class="form-group">
                <label for="lib-platform">Plataforma Personal:</label>
                <select id="lib-platform" v-model="libraryForm.personal_platform">
                  <option :value="null">-- Sin especificar --</option>
                  <option v-for="opt in personalPlatformOptions" :key="opt.value" :value="opt.value">{{ opt.text }}</option>
                </select>
              </div>
              <div class="form-group checkbox-group">
                <input type="checkbox" id="lib-possession" v-model="libraryForm.has_possession" />
                <label for="lib-possession">¿Lo tengo?</label>
              </div>
              <div class="form-group">
                <label for="lib-start-date">Fecha de Inicio:</label>
                <input type="date" id="lib-start-date" v-model="libraryForm.start_date" />
              </div>
              <div class="form-group">
                <label for="lib-end-date">Fecha de Fin:</label>
                <input type="date" id="lib-end-date" v-model="libraryForm.end_date" />
              </div>
              <div class="form-group">
                <label for="lib-hours-story">Horas (Historia):</label>
                <input type="number" id="lib-hours-story" v-model.number="libraryForm.story_duration_hours" min="0" />
              </div>
              <div class="form-group">
                <label for="lib-hours-sides">Horas (Main+Sides):</label>
                <input type="number" id="lib-hours-sides" v-model.number="libraryForm.story_secondary_duration_hours" min="0" />
              </div>
              <div class="form-group">
                <label for="lib-hours-completionist">Horas (Completista):</label>
                <input type="number" id="lib-hours-completionist" v-model.number="libraryForm.completionist_duration_hours" min="0" />
              </div>
            </div>
            <div class="form-group full-width-form-group">
              <label for="lib-public-comment">Comentario Público:</label>
              <textarea id="lib-public-comment" v-model="libraryForm.comment" rows="3"></textarea>
            </div>
            <div class="form-group full-width-form-group">
              <label for="lib-private-comment">Comentario Privado:</label>
              <textarea id="lib-private-comment" v-model="libraryForm.private_comment" rows="3"></textarea>
            </div>
          </div>
          
          <div class="modal-footer">
            <div v-if="libraryActionMessage" :class="libraryActionError ? 'error-message' : 'success-message'" class="action-message modal-action-message">
              {{ libraryActionMessage }}
            </div>
            <button type="button" @click="toggleLibraryForm(false, false)" class="action-button secondary" :disabled="isLoadingLibraryAction">
              Cancelar
            </button>
            <button type="submit" :disabled="isLoadingLibraryAction" class="action-button primary">
              {{ isLoadingLibraryAction ? 'Guardando...' : 'Guardar Cambios' }}
            </button>
          </div>
        </form>
      </div>
    </div>
    
    <div v-if="!isLoading && !gameDetail && !errorMessage" class="no-results-message">
      No se pudieron cargar los detalles del juego.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue';
import { useRoute, RouterLink } from 'vue-router'; // RouterLink importado aquí
import { fetchGameDetailsByIgdbId, addOrUpdateGameInUserLibrary, removeGameFromUserLibrary  } from '@/services/apiInstances';
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

const activeTab = ref('details'); // Pestaña activa por defecto: 'details', 'multimedia', 'community'

const setActiveTab = (tabName) => {
  activeTab.value = tabName;
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

const getWebsiteDisplayName = (urlString) => {
  if (!urlString) return 'Enlace';
  try {
    const url = new URL(urlString);
    let hostname = url.hostname;
    // Quitar 'www.' si existe
    if (hostname.startsWith('www.')) {
      hostname = hostname.substring(4);
    }
    // Capitalizar y mejorar algunos nombres comunes
    if (hostname.includes('steampowered.com')) return 'Steam';
    if (hostname.includes('gog.com')) return 'GOG';
    if (hostname.includes('youtube.com')) return 'YouTube';
    if (hostname.includes('wikipedia.org')) return 'Wikipedia';
    if (hostname.includes('epicgames.com')) return 'Epic Games Store';
    if (hostname.includes('twitch.tv')) return 'Twitch';
    // Capitalizar el primer caracter del hostname como fallback
    return hostname.charAt(0).toUpperCase() + hostname.slice(1);
  } catch (e) {
    // Si la URL no es válida, mostrar una parte o un genérico
    return urlString.length > 30 ? urlString.substring(0, 27) + '...' : urlString;
  }
};

const getYouTubeEmbedUrl = (videoId) => {
  if (!videoId) return '';
  return `https://www.youtube.com/embed/${videoId}`;
};

const formatGameType = (gameType) => {
  if (!gameType) return 'No especificado';

  // Mapeo de los valores numéricos/string de la API a texto legible
  // Basado en IGDB GameCategoryEnum y los ejemplos
  const typeMap = {
    "0": "Juego Principal", // MAIN_GAME
    "MAIN_GAME": "Juego Principal",
    "1": "DLC / Add-on", // DLC_ADDON
    "DLC_ADDON": "DLC / Add-on",
    "2": "Expansión",    // EXPANSION
    "EXPANSION": "Expansión",
    "3": "Bundle",      // BUNDLE
    "BUNDLE": "Bundle",
    "4": "Expansión Independiente", // STANDALONE_EXPANSION
    "STANDALONE_EXPANSION": "Expansión Independiente",
    "5": "Mod",         // MOD
    "MOD": "Mod",
    "6": "Episodio",    // EPISODE
    "EPISODE": "Episodio",
    "7": "Temporada",   // SEASON
    "SEASON": "Temporada",
    "8": "Remake",      // REMAKE
    "REMAKE": "Remake",
    "9": "Remaster",    // REMASTER
    "REMASTER": "Remaster",
    "10": "Juego Expandido", // EXPANDED_GAME
    "EXPANDED_GAME": "Juego Expandido",
    "11": "Port",        // PORT
    "PORT": "Port",
    "12": "Fork",        // FORK (Bifurcación)
    "FORK": "Fork",
    "13": "Pack",        // PACK
    "PACK": "Pack",
    "14": "Actualización", // UPDATE
    "UPDATE": "Actualización",
    // Añade más mapeos según sea necesario, o si tu API devuelve otros strings
  };

  return typeMap[String(gameType).toUpperCase()] || String(gameType); // Devuelve el mapeado o el valor original si no se encuentra
};

const getCompanyRoles = (involvedCompany) => {
  if (!involvedCompany) return '';
  const roles = [];
  if (involvedCompany.developer) roles.push('Desarrollador'); //
  if (involvedCompany.publisher) roles.push('Editor'); //
  if (involvedCompany.porting) roles.push('Porting'); //
  if (involvedCompany.supporting) roles.push('Soporte'); //
  
  return roles.join(', ');
};

const formatReleaseStatus = (statusCode) => {
  if (statusCode === null || statusCode === undefined) return 'No especificado';

  // Mapeo de los valores numéricos (como string) a texto legible
  // Basado en IGDB ReleaseDateStatusEnum (los valores -1 y 15 no son estándar IGDB para este enum específico)
  // La documentación de GameDto.md para first_release_status menciona:
  // 0=Released, 2=Alpha, 3=Beta, 4=Early Access, 5=Offline, 6=Cancelled, 7=Rumored, -1=TBD
  const statusMap = {
    "-1": "Por Determinar (TBD)",
    "0": "Lanzado",
    "2": "Alpha",
    "3": "Beta",
    "4": "Acceso Anticipado (Early Access)",
    "5": "Offline", // (Desconectado/No disponible)
    "6": "Cancelado",
    "7": "Rumoreado",
    // Los valores 1 (Offline IGDB), 8 (Custom/Fanmade) no están en tu DTO para first_release_status,
    // pero los añado por si acaso o para GameStatusDto.id
    "1": "Offline (IGDB)", 
    "8": "Custom/Fanmade (No oficial)" 
  };

  return statusMap[String(statusCode)] || `Estado Desconocido (${statusCode})`;
};

const formatPersonalPlatform = (platform) => {
  if (!platform) return 'No especificada';
  // Mapeo basado en UserPersonalPlatform enum (del backend) o UserGameDataDTO.md / UserGameResponseDTO.md
  const platformMap = {
    'STEAM': 'Steam',
    'EPIC_GAMES': 'Epic Games Store',
    'GOG_GALAXY': 'GOG Galaxy',
    'XBOX': 'Xbox',
    'PLAYSTATION': 'PlayStation',
    'NINTENDO': 'Nintendo',
    'BATTLE_NET': 'Battle.net',
    'EA_APP': 'EA App',
    'UBISOFT_CONNECT': 'Ubisoft Connect',
    'OTHER': 'Otra'
    // Añade más si tu enum tiene más valores
  };
  return platformMap[platform] || platform;
};

const formatUserGameStatus = (status) => {
  if (!status) return 'No especificado';
  // Mapeo basado en UserGameStatus enum (del backend) o UserGameDataDTO.md / UserGameResponseDTO.md
  const statusMap = {
    'COMPLETED': 'Completado',
    'COMPLETED_MAIN_STORY': 'Historia Principal Completada',
    'COMPLETED_MAIN_AND_SIDES': 'Principal + Secundarias Importantes Completado',
    'COMPLETED_100_PERCENT': 'Completado al 100%',
    'ARCHIVED': 'Archivado',
    'ARCHIVED_ABANDONED': 'Archivado (Abandonado)',
    'ARCHIVED_NOT_PLAYING': 'Archivado (Sin Jugar)',
    'WISHLIST': 'En Lista de Deseos',
    'PLAYING': 'Jugando',
    'PLAYING_PAUSED': 'Jugando (En Pausa)',
    'PLAYING_ENDLESS': 'Jugando (Sin Fin / Rejugable)'
    // Añade más si tu enum tiene más valores
  };
  return statusMap[status] || status; // Devuelve el mapeado o el valor original
};

// Función para formatear fechas YYYY-MM-DD a un formato más legible si es necesario,
// o simplemente para mostrarlas si ya están bien.
const formatDateSimple = (dateString) => {
  if (!dateString) return '';
  // Asumiendo que dateString es YYYY-MM-DD
  // Si necesitas un formato diferente, puedes usar new Date(dateString).toLocaleDateString(...)
  return dateString; 
};

// --- NUEVO ESTADO para el formulario de la biblioteca ---
const showLibraryForm = ref(false);
const libraryForm = reactive({
  status: null, //
  personal_platform: null, //
  has_possession: false, //
  score: null, //
  comment: '', //
  private_comment: '', //
  start_date: '', // (YYYY-MM-DD)
  end_date: '', // (YYYY-MM-DD)
  story_duration_hours: null, //
  story_secondary_duration_hours: null, //
  completionist_duration_hours: null, //
});
const isLoadingLibraryAction = ref(false);
const libraryActionMessage = ref('');
const libraryActionError = ref(false);

// Opciones para los selectores del formulario (basadas en tus enums/DTOs)
const gameStatusOptions = [ //
  { value: 'PLAYING', text: 'Jugando' },
  { value: 'PLAYING_PAUSED', text: 'Jugando (En Pausa)' },
  { value: 'PLAYING_ENDLESS', text: 'Jugando (Sin Fin)' },
  { value: 'COMPLETED_MAIN_STORY', text: 'Historia Principal Completada' },
  { value: 'COMPLETED_MAIN_AND_SIDES', text: 'Principal + Secundarias Completado' },
  { value: 'COMPLETED_100_PERCENT', text: 'Completado al 100%' },
  { value: 'WISHLIST', text: 'En Lista de Deseos' },
  { value: 'ARCHIVED_ABANDONED', text: 'Archivado (Abandonado)' },
  { value: 'ARCHIVED_NOT_PLAYING', text: 'Archivado (Sin Jugar)' },
  { value: 'ARCHIVED', text: 'Archivado (Otro)' },
];
const personalPlatformOptions = [ //
  { value: 'STEAM', text: 'Steam' },
  { value: 'EPIC_GAMES', text: 'Epic Games Store' },
  { value: 'GOG_GALAXY', text: 'GOG Galaxy' },
  { value: 'XBOX', text: 'Xbox' },
  { value: 'PLAYSTATION', text: 'PlayStation' },
  { value: 'NINTENDO', text: 'Nintendo' },
  { value: 'BATTLE_NET', text: 'Battle.net' },
  { value: 'EA_APP', text: 'EA App' },
  { value: 'UBISOFT_CONNECT', text: 'Ubisoft Connect' },
  { value: 'OTHER', text: 'Otra' },
];



// --- Lógica para Formulario de Biblioteca ---
const toggleLibraryForm = (show, isNew = false) => {
  showLibraryForm.value = show;
  isAddingNewLibraryEntry.value = isNew;
  libraryActionMessage.value = '';
  libraryActionError.value = false;

  if (show) {
    if (!isNew && gameDetail.value?.user_game_data) {
      const data = gameDetail.value.user_game_data;
      Object.keys(libraryForm).forEach(key => {
        libraryForm[key] = data[key] !== undefined && data[key] !== null ? data[key] : (typeof libraryForm[key] === 'boolean' ? false : (typeof libraryForm[key] === 'string' ? '' : null));
      });
      libraryForm.has_possession = !!data.has_possession; // Asegurar booleano
    } else {
      Object.keys(libraryForm).forEach(key => {
        libraryForm[key] = (typeof libraryForm[key] === 'boolean' ? false : (typeof libraryForm[key] === 'string' ? '' : null));
      });
      libraryForm.has_possession = false;
    }
  }
};

const handleSaveToLibrary = async () => {
  if (!igdbId.value) return;
  isLoadingLibraryAction.value = true;
  libraryActionMessage.value = '';
  libraryActionError.value = false;

  const dto = { ...libraryForm };
  // Limpieza y conversión de tipos
  ['score', 'story_duration_hours', 'story_secondary_duration_hours', 'completionist_duration_hours'].forEach(key => {
    if (dto[key] === '' || dto[key] === null || dto[key] === undefined) dto[key] = null;
    else dto[key] = Number(dto[key]);
    if (isNaN(dto[key])) dto[key] = null; // Si la conversión falla, poner a null
  });
  if (dto.start_date === '') dto.start_date = null;
  if (dto.end_date === '') dto.end_date = null;
  
  // Crear un objeto DTO limpio solo con los campos que tienen valor o son booleanos
  const cleanDto = {};
  let hasDataToSend = false;
  for (const key in dto) {
    if (dto[key] !== null && dto[key] !== undefined && dto[key] !== '') { // No enviar strings vacíos como valor, solo si el backend los espera
      cleanDto[key] = dto[key];
      hasDataToSend = true;
    } else if (typeof dto[key] === 'boolean') { // Enviar booleanos siempre
      cleanDto[key] = dto[key];
      // hasDataToSend = true; // Un booleano por sí solo puede no ser un "cambio" si es el default
    } else if (dto[key] === null && gameDetail.value?.user_game_data?.[key] !== undefined) {
      // Si el campo existía y ahora es null, enviamos null para borrarlo en el backend si así se desea.
      cleanDto[key] = null; 
      hasDataToSend = true;
    }
  }
  // Asegurar que has_possession se envíe si es el único campo
   if (Object.keys(cleanDto).length === 0 && typeof dto.has_possession === 'boolean'){
       cleanDto.has_possession = dto.has_possession;
       // No marcamos hasDataToSend = true aquí necesariamente,
       // el backend debe poder manejar un DTO solo con has_possession.
       // O, podríamos necesitar al menos un status para crear una nueva entrada.
   }
    // Si es una nueva entrada y no se ha especificado nada, ¿qué enviar?
    // Por ahora, si cleanDto está vacío, podríamos no enviar nada o enviar un DTO por defecto.
    // Tu UserGameDataDTO tiene todos los campos como opcionales.

  try {
    console.log("Enviando a la biblioteca (add/update):", cleanDto, "para igdbId:", igdbId.value);
    const response = await addOrUpdateGameInUserLibrary(Number(igdbId.value), cleanDto);
    if (gameDetail.value) {
        gameDetail.value.user_game_data = response.data;
    }
    libraryActionMessage.value = '¡Juego guardado en tu biblioteca!';
    libraryActionError.value = false;
    showLibraryForm.value = false;
  } catch (error) {
    libraryActionError.value = true;
    if (error.response) {
      libraryActionMessage.value = `Error ${error.response.status}: ${error.response.data.message || error.response.data.error || 'No se pudo guardar el juego.'}`;
    } else { libraryActionMessage.value = 'Error de red al guardar en la biblioteca.'; }
  } finally {
    isLoadingLibraryAction.value = false;
  }
};

const handleRemoveFromLibrary = async () => {
  if (!igdbId.value || !gameDetail.value?.user_game_data) {
    libraryActionMessage.value = "No hay juego que eliminar."; libraryActionError.value = true; return;
  }
  if (!window.confirm("¿Seguro que quieres eliminar este juego de tu biblioteca?")) return;

  isLoadingLibraryAction.value = true;
  libraryActionMessage.value = ''; libraryActionError.value = false;
  try {
    await removeGameFromUserLibrary(Number(igdbId.value));
    if (gameDetail.value) gameDetail.value.user_game_data = null;
    libraryActionMessage.value = 'Juego eliminado de tu biblioteca.';
    showLibraryForm.value = false;
    isAddingNewLibraryEntry.value = true; // Si se elimina, el siguiente clic al botón será para "añadir"
  } catch (error) {
    libraryActionError.value = true;
    if (error.response) {
      libraryActionMessage.value = `Error ${error.response.status}: ${error.response.data.message || error.response.data.error || 'No se pudo eliminar.'}`;
    } else { libraryActionMessage.value = 'Error de red.'; }
  } finally {
    isLoadingLibraryAction.value = false;
  }
};

</script>

<style scoped>
.game-details-view {
  max-width: 1000px;
  margin: 1rem auto;
  padding: 1rem;
  font-size: 1rem;
}

.loading-message,
.error-message,
.no-results-message {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
  color: var(--color-text);
}

.error-message {
  color: #d8000c;
  background-color: #ffbaba;
  border: 1px solid #d8000c;
  border-radius: 4px;
}

.no-results-message {
  color: var(--color-text-light-2);
}

.game-content-wrapper {
  background-color: var(--color-background);
  padding: 0;
  border-radius: 8px;
}

/* Estilo general para cada bloque de sección */
.section-block {
  padding: 1.5rem;
  margin-bottom: 1.5rem; 
  background-color: var(--color-background-soft);
  border-radius: 6px;
  border: 1px solid var(--color-border);
}
.tab-pane > .section-block:last-child { /* El último bloque dentro de una pestaña no necesita margen inferior */
  margin-bottom: 0;
}
/* Si un section-block es el único contenido directo de un tab-pane, no necesita margen superior tampoco */
.tab-pane > .section-block:first-child:last-child {
    margin-top: 0; /* O ajusta el padding del .tab-pane en su lugar */
}


.section-block h2 {
  font-size: 1.6rem;
  color: var(--color-heading);
  margin-top: 0;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--color-border-hover);
}
.section-block h3 { 
  font-size: 1.3rem;
  color: var(--color-heading);
  margin-top: 0;
  margin-bottom: 0.75rem;
}
.section-block p {
  line-height: 1.7;
  color: var(--color-text);
  white-space: pre-wrap; 
}

/* Cabecera Principal del Juego */
.game-main-header {
  display: flex;
  flex-direction: column; 
  gap: 1.5rem;
  align-items: center; 
  text-align: center; 
  border-bottom: 2px solid var(--color-border-hover); 
  padding-bottom: 1.5rem;
  margin-bottom: 0; /* La navegación de pestañas irá justo debajo */
  border-bottom-left-radius: 0; /* Para que se una bien con las pestañas */
  border-bottom-right-radius: 0;
}

@media (min-width: 768px) { 
  .game-main-header {
    flex-direction: row;
    align-items: flex-start;
    text-align: left;
  }
}

.game-cover-main {
  width: 100%; 
  max-width: 280px; 
  height: auto;
  aspect-ratio: 3/4;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  background-color: var(--color-background-mute);
  align-self: center; 
}
@media (min-width: 768px) {
  .game-cover-main {
    width: 250px; 
    min-width: 220px;
    align-self: flex-start; 
  }
}

.game-title-meta { flex: 1; }
.game-title-meta h1 { font-size: 2.2rem; margin-bottom: 0.75rem; line-height: 1.2; }
.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0.5rem 1rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}
.meta-item { color: var(--color-text); }
.meta-item strong { color: var(--color-text-light-2); margin-right: 0.4em; }
.meta-item small { font-size: 0.85em; }

.tags-container { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 0.5rem; }
.header-tags { justify-content: center; }
@media (min-width: 768px) { .header-tags { justify-content: flex-start; } }

.tag, .keyword-tag, .genre-tag, .platform-tag, .mode-tag, .franchise-tag, .engine-tag, .theme-tag, .type-tag, .status-tag {
  background-color: var(--color-background-mute);
  color: var(--color-text);
  padding: 0.35em 0.75em;
  border-radius: 16px; 
  font-size: 0.8rem;
  border: 1px solid var(--color-border);
  white-space: nowrap;
  display: inline-block;
  transition: background-color 0.2s, color 0.2s;
}
.tag:hover { background-color: hsla(160, 100%, 37%, 0.2); border-color: hsla(160, 100%, 37%, 0.5); }

/* Estilos para Pestañas */
.tabs-navigation {
  display: flex;
  gap: 0.2rem; /* Espacio pequeño entre botones de pestaña */
  margin-bottom: 0; /* Quitar margen si el tab-content tiene su propio padding/borde superior */
  background-color: var(--color-background-soft); /* Fondo para la barra de pestañas */
  padding: 0.5rem 1.5rem 0 1.5rem; /* Padding para alinear con section-blocks */
  border-bottom: 1px solid var(--color-border-hover);
  /* Si game-main-header tiene border-radius, ajusta esto para que no se vea raro */
  /* border-top-left-radius: 0; 
     border-top-right-radius: 0; */
}
.tabs-navigation button {
  padding: 0.8rem 1.2rem;
  font-size: 0.95rem; /* Ligeramente más pequeño */
  font-weight: 500;
  border: none;
  background-color: transparent;
  color: var(--color-text-light-2);
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: color 0.2s, border-color 0.2s;
  border-top-left-radius: 4px; /* Esquinas superiores redondeadas para los botones */
  border-top-right-radius: 4px;
}
.tabs-navigation button:hover {
  color: var(--color-text);
  background-color: var(--color-background-mute);
}
.tabs-navigation button.active-tab {
  color: hsla(160, 100%, 37%, 1);
  border-bottom-color: hsla(160, 100%, 37%, 1);
  background-color: var(--color-background); /* Para que parezca conectada al contenido */
}

.tab-content {
    /* El padding y borde se manejan por los .section-block internos */
}
.tab-pane {
  /* Si los section-block dentro de tab-pane no deben tener su propio fondo/borde,
     o si el tab-pane debe tener un padding general.
     Por ahora, los section-block manejan su propio estilo. */
}
.tab-pane > .section-block:first-child {
    /* border-top-left-radius: 0;
    border-top-right-radius: 0; */
}


/* Sección de Datos del Usuario */
.user-game-data-section .user-data-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 0.8rem 1.2rem;
  background-color: var(--color-background); 
  padding: 1rem;
  border-radius: 6px;
  border: 1px solid var(--color-border);
}
.user-game-data-section .data-item { font-size: 0.95rem; }
.user-game-data-section .data-item.full-width { grid-column: 1 / -1; }
.user-game-data-section .user-comment {
  margin-top: 0.2rem; padding: 0.6rem; background-color: var(--color-background-soft);
  border: 1px solid var(--color-border); border-radius: 4px; white-space: pre-wrap; font-size: 0.9rem;
}
.library-actions { text-align: center; margin-top: 1rem; margin-bottom: 0.5rem; } /* Ajustado margen inferior */
.user-game-data-section .add-to-library-prompt { text-align: center; padding: 1rem; color: var(--color-text-light-2); }


/* Sección de Metadatos Adicionales */
.metadata-lists .metadata-group { margin-bottom: 1.5rem; }
.metadata-lists .metadata-group:last-child { margin-bottom: 0; }
.metadata-lists h3 { 
  font-size: 1.1rem; color: var(--color-text); 
  margin-bottom: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em;
  padding-bottom: 0.3rem; border-bottom: 1px dashed var(--color-border);
}


/* Compañías Involucradas */
.companies-list { list-style-type: none; padding-left: 0; }
.company-item { margin-bottom: 0.5rem; padding: 0.4rem 0; border-bottom: 1px dashed var(--color-border-hover); }
.company-item:last-child { border-bottom: none; }
.company-name { font-weight: 500; }
.company-roles { font-size: 0.85em; color: var(--color-text-light-2); margin-left: 0.5em; }


/* Galerías (Artworks, Screenshots) */
.gallery-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem; }
.gallery-item { border-radius: 6px; overflow: hidden; background-color: var(--color-background-mute); border: 1px solid var(--color-border); aspect-ratio: 16/9; }
.gallery-item a { display: block; height: 100%;}
.gallery-image { width: 100%; height: 100%; object-fit: cover; display: block; transition: transform 0.3s ease-in-out, opacity 0.3s; }
.gallery-item:hover .gallery-image { transform: scale(1.08); opacity: 0.85; }

/* Vídeos */
.videos-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 1.5rem; }
.video-item { /* No necesita padding si el section-block lo tiene */ }
.video-name { font-size: 1rem; margin-bottom: 0.5rem; font-weight: 500; }
.video-embed-container { position: relative; padding-bottom: 56.25%; height: 0; overflow: hidden; border-radius: 6px; background: #000; }
.video-embed-container iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: 0; }

/* Listas de Juegos Relacionados */
.related-content-section .related-list { margin-bottom: 2rem; }
.related-content-section .related-list:last-child { margin-bottom: 0; }
.related-content-section .related-list h3 {
  font-size: 1.2rem; color: var(--color-text);
  margin-bottom: 0.75rem; padding-bottom: 0.25rem;
  border-bottom: 1px solid var(--color-border);
}
.related-games-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 1rem; }
.related-games-grid.single-item-grid { grid-template-columns: minmax(140px, 200px); justify-content: flex-start; }
.related-game-card {
  background-color: var(--color-background); border-radius: 6px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05); transition: box-shadow 0.2s ease-in-out;
  border: 1px solid var(--color-border);
}
.related-game-card:hover { box-shadow: 0 3px 8px rgba(0,0,0,0.1); }
.related-game-card a { text-decoration: none; color: inherit; display: flex; flex-direction: column; height: 100%;}
.related-game-cover { width: 100%; aspect-ratio: 3/4; object-fit: cover; background-color: var(--color-background-mute); }
.related-game-name { font-size: 0.85rem; padding: 0.6rem 0.5rem; color: var(--color-text); text-align: center; flex-grow: 1; line-height: 1.3; }


/* Sitios Web */
.websites-list { list-style-type: none; padding-left: 0; }
.websites-list li { margin-bottom: 0.5rem; }
.website-link { color: hsla(160, 100%, 37%, 1); text-decoration: none; font-weight: 500; }
.website-link:hover { text-decoration: underline; color: hsla(160, 100%, 30%, 1); }

/* Comentarios públicos */
.public-comments-section .comments-list { list-style: none; padding: 0; }
.public-comments-section .comment-item {
  background-color: var(--color-background);
  padding: 1rem; border-radius: 4px; margin-bottom: 1rem;
  border: 1px solid var(--color-border);
}
.public-comments-section .comment-author { font-weight: bold; }
.public-comments-section .comment-date { font-size: 0.8em; color: var(--color-text-light-2); margin-left: 0.5em;}
.public-comments-section .comment-text { margin-top: 0.5rem; white-space: pre-wrap; }

/* Estilos para el Modal del formulario de biblioteca (copiados de UserSettingsView y adaptados) */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background-color: rgba(0, 0, 0, 0.7); display: flex;
  justify-content: center; align-items: center; z-index: 1000;
}
.modal-panel {
  background-color: var(--color-background-soft); padding: 1.5rem 2rem;
  border-radius: 8px; box-shadow: 0 5px 15px rgba(0,0,0,0.3);
  width: 90%; max-width: 700px; /* Un poco más ancho para el formulario de biblioteca */
  max-height: 90vh; display: flex; flex-direction: column;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid var(--color-border); padding-bottom: 0.75rem; margin-bottom: 1rem;
}
.modal-header h3 { margin: 0; font-size: 1.3rem; color: var(--color-heading); }
.modal-close-button {
  background: none; border: none; font-size: 1.8rem; line-height: 1;
  color: var(--color-text-light-2); cursor: pointer;
}
.modal-close-button:hover { color: var(--color-text); }
.modal-body { overflow-y: auto; flex-grow: 1; padding-right: 0.5rem; /* Espacio para scrollbar */ }

.library-form-modal .form-grid {
  display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); /* Reducido minmax */
  gap: 0.8rem 1rem;
}
.library-form-modal .form-group { display: flex; flex-direction: column; }
.library-form-modal .form-group label { margin-bottom: 0.3rem; font-size: 0.85rem; color: var(--color-text-light-2); }
.library-form-modal .form-group input,
.library-form-modal .form-group select,
.library-form-modal .form-group textarea {
  padding: 0.6rem; border: 1px solid var(--color-border); border-radius: 4px;
  background-color: var(--color-background); color: var(--color-text); font-size: 0.9rem;
}
.library-form-modal .checkbox-group { flex-direction: row; align-items: center; }
.library-form-modal .checkbox-group label { margin-bottom: 0; margin-left: 0.4rem; font-size: 0.9rem; }
.library-form-modal .full-width-form-group { grid-column: 1 / -1; }

.modal-footer {
  margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid var(--color-border);
  display: flex; justify-content: flex-end; align-items: center; gap: 0.75rem;
}
.modal-footer .action-message { margin: 0; padding: 0; flex-grow: 1; text-align: left; font-size: 0.9em; }
.modal-footer .action-button { margin-left: 0.5rem; }

.action-button {
  padding: 0.6em 1.2em; font-size: 0.95rem; border-radius: 4px;
  cursor: pointer; transition: background-color 0.2s, color 0.2s;
  border: 1px solid transparent;
}
.action-button.primary {
  background-color: hsla(160, 100%, 37%, 1); color: white;
  border-color: hsla(160, 100%, 37%, 1);
}
.action-button.primary:hover:not(:disabled) { background-color: hsla(160, 100%, 30%, 1); }
.action-button.secondary {
  background-color: var(--color-background-mute); color: var(--color-text);
  border-color: var(--color-border);
}
.action-button.secondary:hover:not(:disabled) { background-color: var(--color-border); }
.action-button.danger {
  background-color: #d9534f; color: white; border-color: #d43f3a;
}
.action-button.danger:hover:not(:disabled) { background-color: #c9302c; }
.action-button:disabled { opacity: 0.6; cursor: not-allowed; }

</style>