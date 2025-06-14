<template>
  <div class="game-details-view">
    <div v-if="isLoading" class="loading-message">Cargando detalles del juego...</div>
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

    <div v-if="!isLoading && gameDetail && gameDetail.game_info" class="game-content-wrapper">
      <section class="game-main-header section-block">

        <div class="game-cover-container">

          <img :src="getCoverUrl(gameDetail.game_info.cover, '720p', 'mainCover')"
            :alt="`Carátula de ${gameDetail.game_info.name || 'Juego'}`" class="game-cover-main"
            @error="onImageError" />

          <div v-if="gameDetail.game_info.first_release_date || gameDetail.game_info.first_release_status !== null"
            class="game-release-status-on-cover meta-item" :class="getReleaseStatusClass(gameDetail.game_info)">
            <span>{{ formatReleaseStatus(gameDetail.game_info) }}</span>
          </div>
        </div>


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
            <div
              v-if="gameDetail.game_info.first_release_status !== null && gameDetail.game_info.first_release_status !== undefined"
              class="meta-item">
              <strong>Estado:</strong>
              <span>{{ formatReleaseStatus(gameDetail.game_info.first_release_status) }}</span>
            </div>
            <div v-if="gameDetail.game_info.total_rating" class="meta-item">
              <strong>Puntuación IGDB:</strong>
              <span>{{ Math.round(gameDetail.game_info.total_rating) }}/100</span>
              <small v-if="gameDetail.game_info.total_rating_count"> ({{ gameDetail.game_info.total_rating_count }}
                votos)</small>
            </div>
          </div>

          <section class="user-game-data-section" v-if="authStore.isAuthenticated">
            <div v-if="gameDetail.user_game_data">
              <h2>Mis Datos del Juego</h2>

              <div class="library-actions">
                <button v-if="!showLibraryForm" @click="openLibraryModal(false)" class="action-button primary">
                  Editar Mis Datos
                </button>
                <button v-if="!showLibraryForm && gameDetail.user_game_data" @click="openPrivateNoteModal"
                  class="action-button secondary">
                  Añadir/Editar Nota Privada
                </button>
              </div>

              <div class="user-data-grid">
                <div v-if="gameDetail.user_game_data.status" class="data-item"><strong>Estado:</strong> {{
                  formatUserGameStatus(gameDetail.user_game_data.status) }}</div>
                <div v-if="gameDetail.user_game_data.score !== null && gameDetail.user_game_data.score !== undefined"
                  class="data-item"><strong>Puntuación:</strong> {{ gameDetail.user_game_data.score }}/10</div>
                <div v-if="gameDetail.user_game_data.personal_platform" class="data-item"><strong>Plataforma:</strong>
                  {{ formatPersonalPlatform(gameDetail.user_game_data.personal_platform) }}</div>
                <div
                  v-if="gameDetail.user_game_data.has_possession !== null && gameDetail.user_game_data.has_possession !== undefined"
                  class="data-item"><strong>Lo Tengo:</strong> {{ gameDetail.user_game_data.has_possession ? 'Sí' :
                    'No' }}</div>
                <div v-if="gameDetail.user_game_data.start_date" class="data-item"><strong>Empezado:</strong> {{
                  formatDateSimple(gameDetail.user_game_data.start_date) }}</div>
                <div v-if="gameDetail.user_game_data.end_date" class="data-item"><strong>Terminado:</strong> {{
                  formatDateSimple(gameDetail.user_game_data.end_date) }}</div>
                <div v-if="gameDetail.user_game_data.story_duration_hours" class="data-item"><strong>Horas
                    (Historia):</strong> {{ gameDetail.user_game_data.story_duration_hours }}h</div>
                <div v-if="gameDetail.user_game_data.story_secondary_duration_hours" class="data-item"><strong>Horas
                    (Main+Sides):</strong> {{ gameDetail.user_game_data.story_secondary_duration_hours }}h</div>
                <div v-if="gameDetail.user_game_data.completionist_duration_hours" class="data-item"><strong>Horas
                    (Completista):</strong> {{ gameDetail.user_game_data.completionist_duration_hours }}h</div>

                <!--<div v-if="gameDetail.user_game_data.private_comment" class="data-item full-width"><strong>Comentario
                    Privado:</strong>
                  <p class="user-comment">{{ gameDetail.user_game_data.private_comment }}</p>
                </div>-->
              </div>

              <div class="library-actions">
                <button v-if="!showLibraryForm" @click="handleRemoveFromLibrary" :disabled="isLoadingLibraryAction"
                  class="action-button danger">
                  {{ isLoadingLibraryAction ? 'Eliminando...' : 'Eliminar de Mi Biblioteca' }}
                </button>
              </div>
            </div>

            <div v-else class="add-to-library-prompt">
              <div class="library-actions">
                <button v-if="!showLibraryForm" @click="openLibraryModal(true)" class="action-button primary">
                  Añadir a Mi Biblioteca
                </button>
              </div>
            </div>
          </section>

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
      <div class="column-right">
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

            <section class="screenshots-section section-block"
              v-if="gameDetail.game_info.screenshots && gameDetail.game_info.screenshots.length">


              <div class="carousel-container">
                <button v-if="gameDetail.game_info.screenshots && gameDetail.game_info.screenshots.length > 1"
                  @click="prevScreenshot" class="carousel-arrow prev" aria-label="Anterior">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5"
                    stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5" />
                  </svg>
                </button>

                <div class="carousel-viewport" ref="carouselViewportRef">
                  <div class="carousel-slider" :style="carouselSliderStyle" @transitionend="handleTransitionEnd">
                    <div v-for="(screenshot, index) in gameDetail.game_info.screenshots" :key="screenshot.id"
                      class="carousel-item">
                      <img :src="getCoverUrl(screenshot, 'screenshot_med')" :alt="`Screenshot ${screenshot.id}`"
                        class="gallery-image clickable"
                        @click="openLightbox(gameDetail.game_info.screenshots, index)" />
                    </div>
                  </div>
                </div>

                <button v-if="gameDetail.game_info.screenshots && gameDetail.game_info.screenshots.length > 1"
                  @click="nextScreenshot" class="carousel-arrow next" aria-label="Siguiente">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5"
                    stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" d="m8.25 4.5 7.5 7.5-7.5 7.5" />
                  </svg>
                </button>
              </div>
            </section>


            <!--
            <section class="game-summary section-block" v-if="gameDetail.game_info.summary">
              <h2>Resumen</h2>
              <p>{{ gameDetail.game_info.summary }}</p>
            </section>
            -->

            <section class="game-storyline section-block" v-if="gameDetail.game_info.storyline">
              <h2>Argumento</h2>
              <p>{{ gameDetail.game_info.storyline }}</p>
            </section>

            <section class="private-notes-section section-block"
              v-if="gameDetail.user_game_data && gameDetail.user_game_data.private_comment">
              <!--
              <div class="section-header-with-action">
                <h2>Mis Notas Privadas</h2>
                
                <button @click="openPrivateNoteModal" class="action-button secondary small-button">
                  Editar Nota
                </button>
                
              </div>
              -->
              <h2>Mis Notas Privadas</h2>
              <blockquote class="private-comment-quote">
                <p style="white-space: pre-wrap;">{{ displayComment }}</p>
                <button v-if="commentNeedsTruncation" @click="isExpanded = !isExpanded" class="read-more-button">
                  {{ isExpanded ? 'Leer menos' : 'Leer más...' }}
                </button>
              </blockquote>
            </section>

            <section class="metadata-lists section-block">
              <div v-if="gameDetail.game_info.platforms && gameDetail.game_info.platforms.length"
                class="metadata-group">
                <h3>Plataformas</h3>
                <div class="tags-container">
                  <span v-for="platform in gameDetail.game_info.platforms" :key="platform.id" class="tag platform-tag">
                    {{ platform.name }}
                  </span>
                </div>
              </div>
              <div v-if="gameDetail.game_info.game_modes && gameDetail.game_info.game_modes.length"
                class="metadata-group">
                <h3>Modos de Juego</h3>
                <div class="tags-container">
                  <span v-for="mode in gameDetail.game_info.game_modes" :key="mode.id" class="tag mode-tag">
                    {{ mode.name }}
                  </span>
                </div>
              </div>
              <div v-if="gameDetail.game_info.game_engines && gameDetail.game_info.game_engines.length"
                class="metadata-group">
                <h3>Motores Gráficos</h3>
                <div class="tags-container">
                  <span v-for="engine in gameDetail.game_info.game_engines" :key="engine.id" class="tag engine-tag">
                    {{ engine.name }}
                  </span>
                </div>
              </div>
              <div v-if="gameDetail.game_info.franchises && gameDetail.game_info.franchises.length"
                class="metadata-group">
                <h3>Franquicias</h3>
                <div class="tags-container">
                  <span v-for="franchise in gameDetail.game_info.franchises" :key="franchise.id"
                    class="tag franchise-tag">
                    {{ franchise.name }}
                  </span>
                </div>
              </div>
            </section>

            <section class="involved-companies-section section-block"
              v-if="gameDetail.game_info.involved_companies && gameDetail.game_info.involved_companies.length">
              <h2>Compañías Involucradas</h2>
              <ul class="companies-list">
                <li v-for="involvement in gameDetail.game_info.involved_companies" :key="involvement.id"
                  class="company-item">
                  <span class="company-name">{{ involvement.company.name }}</span>
                  <span v-if="getCompanyRoles(involvement)" class="company-roles"> ({{ getCompanyRoles(involvement)
                  }})</span>
                </li>
              </ul>
            </section>

            <section class="keywords-section section-block"
              v-if="gameDetail.game_info.keywords && gameDetail.game_info.keywords.length">
              <h2>Palabras Clave</h2>
              <div class="tags-container">
                <span v-for="keyword in gameDetail.game_info.keywords" :key="keyword.id" class="tag keyword-tag">
                  {{ keyword.name }}
                </span>
              </div>
            </section>

            <section class="websites-section section-block"
              v-if="gameDetail.game_info.websites && gameDetail.game_info.websites.length">
              <h2>Sitios Web</h2>
              <ul v-if="recognizedWebsites.length > 0" class="websites-list recognized-links">
                <li v-for="website in recognizedWebsites" :key="website.id">
                  <a :href="website.url" target="_blank" rel="noopener noreferrer" class="website-link"
                    :title="getWebsiteDisplayName(website.url)"> <img :src="getIconUrl(getWebsiteIconName(website.url))"
                      class="website-icon" alt="" />
                  </a>
                </li>
              </ul>

              <ul v-if="otherWebsites.length > 0" class="websites-list other-links">
                <li v-for="website in otherWebsites" :key="website.id">
                  <a :href="website.url" target="_blank" rel="noopener noreferrer" class="website-link">

                    <img :src="getIconUrl(getWebsiteIconName(website.url))" class="website-icon" alt="" />

                    <span>{{ getWebsiteDisplayName(website.url) }}</span>
                  </a>
                </li>
              </ul>
            </section>
          </div>

          <div v-show="activeTab === 'multimedia'" class="tab-pane">
            <section class="artworks-section section-block"
              v-if="gameDetail.game_info.artworks && gameDetail.game_info.artworks.length">
              <h2>Ilustraciones</h2>
              <div class="gallery-grid">
                <div v-for="(artwork, index) in gameDetail.game_info.artworks" :key="artwork.id" class="gallery-item">
                  <img :src="getCoverUrl(artwork, '720p')" class="gallery-image clickable"
                    @click="openLightbox(gameDetail.game_info.artworks, index)" />
                </div>
              </div>
            </section>



            <section class="videos-section section-block"
              v-if="gameDetail.game_info.videos && gameDetail.game_info.videos.length">
              <h2>Vídeos</h2>
              <div class="videos-grid">
                <div v-for="video in gameDetail.game_info.videos" :key="video.id" class="video-item">
                  <h4 class="video-name" v-if="video.name">{{ video.name }}</h4>
                  <div class="video-embed-container" v-if="video.video_id">
                    <iframe :src="getYouTubeEmbedUrl(video.video_id)" :title="video.name || 'Vídeo del juego'"
                      frameborder="0"
                      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                      allowfullscreen></iframe>
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
                      <img :src="getCoverUrl(gameDetail.game_info.parent_game.cover, '720p', 'relatedCover')"
                        :alt="gameDetail.game_info.parent_game.name" class="related-game-cover"
                        @error="onImageErrorSmall" />
                      <span class="related-game-name">{{ gameDetail.game_info.parent_game.name }}</span>
                    </RouterLink>
                  </div>
                </div>
              </div>
              <div class="related-list" v-if="gameDetail.game_info.version_parent">
                <h3>Versión Principal</h3>
                <div class="related-games-grid single-item-grid">
                  <div class="related-game-card">
                    <RouterLink
                      :to="{ name: 'game-details', params: { igdbId: gameDetail.game_info.version_parent.id } }">
                      <img :src="getCoverUrl(gameDetail.game_info.version_parent.cover, '720p', 'relatedCover')"
                        :alt="gameDetail.game_info.version_parent.name" class="related-game-cover"
                        @error="onImageErrorSmall" />
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
                      <img :src="getCoverUrl(dlc.cover, '720p', 'relatedCover')" :alt="dlc.name"
                        class="related-game-cover" @error="onImageErrorSmall" />
                      <span class="related-game-name">{{ dlc.name }}</span>
                    </RouterLink>
                  </div>
                </div>
              </div>

              <div class="related-list"
                v-if="gameDetail.game_info.expansions && gameDetail.game_info.expansions.length">
                <h3>Expansiones</h3>
                <div class="related-games-grid">
                  <div v-for="expansion in gameDetail.game_info.expansions" :key="expansion.id"
                    class="related-game-card">
                    <RouterLink :to="{ name: 'game-details', params: { igdbId: expansion.id } }">
                      <img :src="getCoverUrl(expansion.cover, '720p', 'relatedCover')" :alt="expansion.name"
                        class="related-game-cover" @error="onImageErrorSmall" />
                      <span class="related-game-name">{{ expansion.name }}</span>
                    </RouterLink>
                  </div>
                </div>
              </div>

              <div class="related-list" v-if="gameDetail.game_info.bundles && gameDetail.game_info.bundles.length">
                <h3>Paquetes (Bundles)</h3>
                <div class="related-games-grid">
                  <div v-for="bundleItem in gameDetail.game_info.bundles" :key="bundleItem.id"
                    class="related-game-card">
                    <RouterLink :to="{ name: 'game-details', params: { igdbId: bundleItem.id } }">
                      <img :src="getCoverUrl(bundleItem.cover, '720p', 'relatedCover')" :alt="bundleItem.name"
                        class="related-game-cover" @error="onImageErrorSmall" />
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
                      <img :src="getCoverUrl(remake.cover, '720p', 'relatedCover')" :alt="remake.name"
                        class="related-game-cover" @error="onImageErrorSmall" />
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
                      <img :src="getCoverUrl(remaster.cover, '720p', 'relatedCover')" :alt="remaster.name"
                        class="related-game-cover" @error="onImageErrorSmall" />
                      <span class="related-game-name">{{ remaster.name }}</span>
                    </RouterLink>
                  </div>
                </div>
              </div>

              <div class="related-list"
                v-if="gameDetail.game_info.similar_games && gameDetail.game_info.similar_games.length">
                <h3>Juegos Similares</h3>
                <div class="related-games-grid">
                  <div v-for="similarGame in gameDetail.game_info.similar_games" :key="similarGame.id"
                    class="related-game-card">
                    <RouterLink :to="{ name: 'game-details', params: { igdbId: similarGame.id } }">
                      <img :src="getCoverUrl(similarGame.cover, '720p', 'relatedCover')" :alt="similarGame.name"
                        class="related-game-cover" @error="onImageErrorSmall" />
                      <span class="related-game-name">{{ similarGame.name }}</span>
                    </RouterLink>
                  </div>
                </div>
              </div>
            </section>
          </div>

          <div v-show="activeTab === 'community'" class="tab-pane">

            <section class="user-game-data-section section-block" v-if="authStore.isAuthenticated">
              <h2>Mi Comentario Público</h2>

              <div v-if="gameDetail.user_game_data && gameDetail.user_game_data.comment">
                <div class="user-data-grid">
                  <div class="data-item full-width">
                    <p class="user-comment">{{ gameDetail.user_game_data.comment }}</p>
                  </div>
                </div>
                <div class="library-actions">
                  <button @click="openCommentModal" class="action-button secondary">
                    Editar Comentario
                  </button>
                </div>
              </div>

              <div v-else-if="gameDetail.user_game_data">
                <div class="add-to-library-prompt">
                  <p>Aún no has añadido un comentario para este juego. ¡Comparte tu opinión con la comunidad!</p>
                </div>
                <div class="library-actions">
                  <button @click="openCommentModal" class="action-button primary">
                    Añadir Comentario
                  </button>
                </div>
              </div>

              <div v-else>
                <div class="add-to-library-prompt">
                  <p>Debes añadir este juego a tu biblioteca para poder dejar un comentario.</p>
                </div>
              </div>
            </section>


            <section class="public-comments-section section-block"
              v-if="gameDetail.public_comments && gameDetail.public_comments.length">
              <h2>Comentarios de la Comunidad</h2>
              <ul class="comments-list">
                <li v-for="comment in gameDetail.public_comments" :key="comment.username + comment.comment_date"
                  class="comment-item">
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
      </div>

    </div>
    <div v-if="showLibraryForm" class="modal-overlay" @click.self="closeLibraryModal">
      <div class="modal-panel library-wizard-modal">
        <div class="modal-header">
          <h3>{{ isAddingNewLibraryEntry ? 'Añadir a' : 'Editar en' }} Mi Biblioteca: {{ gameDetail?.game_info?.name }}
          </h3>
          <button type="button" @click="closeLibraryModal" class="modal-close-button" aria-label="Cerrar">×</button>
        </div>

        <div class="modal-body">
          <div v-if="modalStep === 0" class="wizard-step-0">
            <h4>¿Qué quieres hacer con este juego?</h4>
            <div class="status-selection-grid">
              <button @click="setStatusCategoryAndContinue('playing')"
                :class="{ 'selected': mainStatusCategory === 'playing' }">Jugando</button>
              <button @click="setStatusCategoryAndContinue('completed')"
                :class="{ 'selected': mainStatusCategory === 'completed' }">Terminado</button>
              <button @click="setStatusCategoryAndContinue('archived')"
                :class="{ 'selected': mainStatusCategory === 'archived' }">Archivado</button>
              <button @click="setStatusCategoryAndContinue('wishlist')"
                :class="{ 'selected': mainStatusCategory === 'wishlist' }">Deseado</button>
            </div>
          </div>

          <form v-if="modalStep === 1" @submit.prevent="goToStep(2)" class="library-form-modal">
            <div class="form-grid">
              <div class="form-group">
                <label for="lib-status">Estado específico:</label>
                <select id="lib-status" v-model="libraryForm.status">
                  <option v-for="opt in computedStatusOptions" :key="opt.value" :value="opt.value">{{ opt.text }}
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label for="lib-score">Puntuación (0-10):</label>
                <input type="number" id="lib-score" v-model.number="libraryForm.score" min="0" max="10" step="0.5" />
              </div>
            </div>
            <div class="form-group full-width-form-group">
              <label for="lib-public-comment">Comentario Público:</label>
              <textarea id="lib-public-comment" v-model="libraryForm.comment" rows="4"></textarea>
            </div>

            <div class="modal-footer">
              <button type="button" @click="goToStep(0)" class="action-button secondary">Atrás</button>
              <button type="submit" class="action-button primary">Continuar</button>
            </div>
          </form>

          <form v-if="modalStep === 2" @submit.prevent="handleSaveToLibrary" class="library-form-modal">
            <div class="form-grid">
              <div class="form-group">
                <label for="lib-platform">Plataforma Personal:</label>
                <select id="lib-platform" v-model="libraryForm.personal_platform">
                  <option :value="null">-- Sin especificar --</option>
                  <option v-for="opt in personalPlatformOptions" :key="opt.value" :value="opt.value">{{ opt.text }}
                  </option>
                </select>
              </div>
              <div class="form-group checkbox-group">
                <input type="checkbox" id="lib-possession" v-model="libraryForm.has_possession" />
                <label for="lib-possession">¿Lo tengo en propiedad?</label>
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
                <label for="lib-hours-type">Tipo de Horas:</label>
                <select id="lib-hours-type" v-model="hourInputType">
                  <option value="story_duration_hours">Horas (Historia)</option>
                  <option value="story_secondary_duration_hours">Horas (Main+Sides)</option>
                  <option value="completionist_duration_hours">Horas (Completista)</option>
                </select>
              </div>
              <div class="form-group">
                <label for="lib-hours-value">Horas Jugadas:</label>
                <input type="number" id="lib-hours-value" min="0" v-model.number="libraryForm[hourInputType]" />
              </div>
            </div>
            <div class="form-group full-width-form-group">
              <label for="lib-private-comment">Comentario Privado (Solo visible para ti):</label>
              <textarea id="lib-private-comment" v-model="libraryForm.private_comment" rows="4"></textarea>
            </div>

            <div class="modal-footer">
              <button type="button" @click="goToStep(1)" class="action-button secondary">Atrás</button>
              <button type="submit" :disabled="isLoadingLibraryAction" class="action-button primary">
                {{ isLoadingLibraryAction ? 'Guardando...' : 'Guardar Cambios' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>



    <div v-if="!isLoading && !gameDetail && !errorMessage" class="no-results-message">
      No se pudieron cargar los detalles del juego.
    </div>
  </div>

  <div v-if="showCommentModal" class="modal-overlay" @click.self="showCommentModal = false">
    <div class="modal-panel">
      <form @submit.prevent="handleSaveComment" class="library-form-modal">
        <div class="modal-header">
          <h3>Mi Comentario sobre {{ gameDetail?.game_info?.name }}</h3>
          <button type="button" @click="showCommentModal = false" class="modal-close-button"
            aria-label="Cerrar">×</button>
        </div>

        <div class="modal-body">
          <div class="form-group full-width-form-group">
            <label for="public-comment-input">Tu opinión:</label>
            <textarea id="public-comment-input" v-model="commentFormText" rows="6"
              placeholder="Escribe aquí tu comentario público..."></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <div v-if="commentActionMessage" :class="commentActionError ? 'error-message' : 'success-message'"
            class="action-message modal-action-message">
            {{ commentActionMessage }}
          </div>
          <button type="button" @click="showCommentModal = false" class="action-button secondary"
            :disabled="isSavingComment">
            Cancelar
          </button>
          <button type="submit" :disabled="isSavingComment" class="action-button primary">
            {{ isSavingComment ? 'Guardando...' : 'Guardar Comentario' }}
          </button>
        </div>
      </form>
    </div>
  </div>


  <div v-if="showPrivateNoteModal" class="modal-overlay" @click.self="showPrivateNoteModal = false">
    <div class="modal-panel">
      <form @submit.prevent="handleSavePrivateNote" class="library-form-modal">
        <div class="modal-header">
          <h3>Editar Mi Nota Privada</h3>
          <button type="button" @click="showPrivateNoteModal = false" class="modal-close-button"
            aria-label="Cerrar">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group full-width-form-group">
            <label for="private-note-input">Tu nota privada sobre {{ gameDetail?.game_info?.name }}:</label>
            <textarea id="private-note-input" v-model="privateNoteFormText" rows="8"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" @click="showPrivateNoteModal = false" class="action-button secondary"
            :disabled="isSavingPrivateNote">
            Cancelar
          </button>
          <button type="submit" :disabled="isSavingPrivateNote" class="action-button primary">
            {{ isSavingPrivateNote ? 'Guardando...' : 'Guardar Nota' }}
          </button>
        </div>
      </form>
    </div>
  </div>


  <div v-if="showLightbox && currentImage" class="lightbox-overlay" @click.self="closeLightbox">

    <button v-if="currentGallery.length > 1" @click.stop="prevImage" class="nav-arrow prev"
      aria-label="Imagen anterior">
      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5" />
      </svg>
    </button>

    <div class="lightbox-content">
      <button @click="closeLightbox" class="lightbox-close" aria-label="Cerrar">
        <p>x</p>
      </button>
      <img :src="getCoverUrl(currentImage, '1080p')"
        :alt="`Imagen ampliada ${currentIndex + 1} de ${currentGallery.length}`" class="lightbox-image" />
    </div>

    <button v-if="currentGallery.length > 1" @click.stop="nextImage" class="nav-arrow next"
      aria-label="Siguiente imagen">
      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" d="m8.25 4.5 7.5 7.5-7.5 7.5" />
      </svg>
    </button>

  </div>
</template>

<script setup>
import { ref, onMounted,onUnmounted, reactive, computed, watch } from 'vue';
import { useRoute, RouterLink } from 'vue-router'; // RouterLink importado aquí
import { fetchGameDetailsByIgdbId, addOrUpdateGameInUserLibrary, removeGameFromUserLibrary } from '@/services/apiInstances.js';
import { useAuthStore } from '@/stores/authStore.js';
import defaultGameCoverLarge from '@/assets/img/default-game-cover.svg'; // Placeholder para portada principal
import defaultRelatedCover from '@/assets/img/default-game-cover.svg'; // Placeholder para portadas pequeñas



const showPrivateNoteModal = ref(false);
const privateNoteFormText = ref('');
const isSavingPrivateNote = ref(false);


// Abre el modal y carga la nota privada existente en el formulario
const openPrivateNoteModal = () => {
  privateNoteFormText.value = gameDetail.value?.user_game_data?.private_comment || '';
  isSavingPrivateNote.value = false;
  showPrivateNoteModal.value = true;
};

// Guarda únicamente el campo de la nota privada
const handleSavePrivateNote = async () => {
  if (!igdbId.value) return;
  isSavingPrivateNote.value = true;

  const dto = {
    private_comment: privateNoteFormText.value
  };

  try {
    const response = await addOrUpdateGameInUserLibrary(Number(igdbId.value), dto);
    if (gameDetail.value && gameDetail.value.user_game_data) {
      gameDetail.value.user_game_data = response.data;
    }
    showPrivateNoteModal.value = false; // Cerramos el modal
  } catch (error) {
    console.error("Error al guardar la nota privada:", error);
  } finally {
    isSavingPrivateNote.value = false;
  }
};


// --- ESTADO DEL MODAL ASISTENTE (NUEVO) ---
const modalStep = ref(0); // 0: Selección inicial, 1: Detalles primarios, 2: Detalles secundarios
const mainStatusCategory = ref(null); // 'playing', 'completed', 'archived'
const hourInputType = ref('story_duration_hours'); // Para el desplegable de horas




const showCommentModal = ref(false);
const commentFormText = ref('');
const isSavingComment = ref(false);
const commentActionMessage = ref('');
const commentActionError = ref(false);

const openCommentModal = () => {
  // Rellena el textarea con el comentario existente, o con vacío si no hay.
  commentFormText.value = gameDetail.value?.user_game_data?.comment || '';
  // Resetea cualquier mensaje de acción anterior
  commentActionMessage.value = '';
  commentActionError.value = false;
  // Muestra el modal
  showCommentModal.value = true;
};

const handleSaveComment = async () => {
  if (!igdbId.value) return;
  isSavingComment.value = true;
  commentActionMessage.value = '';
  commentActionError.value = false;

  // Creamos un DTO (Data Transfer Object) solo con el comentario.
  // Esto asegura que solo actualizamos este campo.
  const commentDto = {
    comment: commentFormText.value
  };

  try {
    console.log("Guardando comentario:", commentDto, "para igdbId:", igdbId.value);
    // Llamamos al mismo servicio de la API, pero solo con la información del comentario.
    const response = await addOrUpdateGameInUserLibrary(Number(igdbId.value), commentDto);

    // Actualizamos los datos locales para que la vista refleje el cambio al instante.
    if (gameDetail.value) {
      gameDetail.value.user_game_data = response.data;
    }

    // Cerramos el modal al guardar con éxito.
    showCommentModal.value = false;

  } catch (error) {
    commentActionError.value = true;
    if (error.response) {
      commentActionMessage.value = `Error: ${error.response.data.message || 'No se pudo guardar el comentario.'}`;
    } else {
      commentActionMessage.value = 'Error de red al guardar el comentario.';
    }
    console.error("Error al guardar comentario:", error);
  } finally {
    isSavingComment.value = false;
  }
};




// --- LÓGICA PARA "LEER MÁS" DEL COMENTARIO PRIVADO ---
const isExpanded = ref(false);
const MAX_COMMENT_LENGTH = 350; // Umbral de caracteres para truncar el texto

// Propiedad que determina si el texto es lo suficientemente largo para necesitar el botón
const commentNeedsTruncation = computed(() => {
  const comment = gameDetail.value?.user_game_data?.private_comment;
  return comment && comment.length > MAX_COMMENT_LENGTH;
});

// Propiedad que devuelve el texto a mostrar (completo o truncado)
const displayComment = computed(() => {
  const comment = gameDetail.value?.user_game_data?.private_comment;
  if (!comment) return '';

  if (commentNeedsTruncation.value && !isExpanded.value) {
    return comment.substring(0, MAX_COMMENT_LENGTH) + '...';
  }
  return comment;
});



const screenshotCarouselIndex = ref(0);


const nextScreenshot = () => {
  const screenshots = gameDetail.value?.game_info?.screenshots;
  if (!screenshots || screenshots.length <= 1) return;
  
  if (screenshotCarouselIndex.value >= screenshots.length - 1) {
    screenshotCarouselIndex.value = 0;
  } else {
    screenshotCarouselIndex.value++;
  }
};


const prevScreenshot = () => {
  const screenshots = gameDetail.value?.game_info?.screenshots;
  if (!screenshots || screenshots.length <= 1) return;
  
  if (screenshotCarouselIndex.value <= 0) {
    screenshotCarouselIndex.value = screenshots.length - 1;
  } else {
    screenshotCarouselIndex.value--;
  }
};


const carouselViewportRef = ref(null); 


const carouselItemWidth = ref(0);

// Esta función se encarga de actualizar el ancho
const updateCarouselItemWidth = () => {
  if (carouselViewportRef.value) {
    // El ancho del item es el ancho total del contenedor visible
    carouselItemWidth.value = carouselViewportRef.value.offsetWidth;
  }
};

// Cuando el componente se monta en la página, calculamos el ancho inicial
onMounted(() => {
  updateCarouselItemWidth();
  // Y creamos un observador que recalcule el ancho si el tamaño de la ventana cambia
  window.addEventListener('resize', updateCarouselItemWidth);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateCarouselItemWidth);
});

const carouselSliderStyle = computed(() => {
  if (!gameDetail.value?.game_info?.screenshots?.length) return {};
  
  const itemWidth = 400; // Ancho fijo de cada item 
  const position = screenshotCarouselIndex.value * itemWidth;
  
  return {
    transform: `translateX(-${position}px)`,
    width: `${itemWidth * gameDetail.value.game_info.screenshots.length}px`
  };
});

let isTransitioning = false;

const handleTransitionEnd = () => {
  isTransitioning = false;
};

const showLightbox = ref(false);
const currentGallery = ref([]); // Guardará el array de la galería activa (artworks o screenshots)
const currentIndex = ref(0);

const currentImage = computed(() => {
  if (currentGallery.value.length > 0) {
    return currentGallery.value[currentIndex.value];
  }
  return null;
});

const openLightbox = (gallery, index) => {
  currentGallery.value = gallery;
  currentIndex.value = index;
  showLightbox.value = true;
  document.body.style.overflow = 'hidden';
};

const closeLightbox = () => {
  showLightbox.value = false;
  document.body.style.overflow = '';
  currentGallery.value = [];
  currentIndex.value = 0;
};


const nextImage = () => {
  if (currentGallery.value.length > 0) {
    // Si es la última imagen, vuelve a la primera (efecto carrusel)
    currentIndex.value = (currentIndex.value + 1) % currentGallery.value.length;
  }
};

const prevImage = () => {
  if (currentGallery.value.length > 0) {
    // Si es la primera, va a la última
    currentIndex.value = (currentIndex.value - 1 + currentGallery.value.length) % currentGallery.value.length;
  }
};



const route = useRoute();
const authStore = useAuthStore();

const gameDetail = ref(null);
const isLoading = ref(true);
const errorMessage = ref('');
const igdbId = ref(null);

const loadGameDetails = async (id) => {

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
    console.log("Datos del juego recibido:", response.data);
    gameDetail.value = response.data;
    console.log("Detalles del juego recibidos:", gameDetail.value);
    console.log("DATOS DE ARTWORKS:", gameDetail.value.game_info.artworks);
    console.log("DATOS DE SCREENSHOTS:", gameDetail.value.game_info.screenshots);
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

const activeTab = ref('details'); 

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
    activeTab.value = 'details';
  }
});

const getCoverUrl = (imageInfo, size = 'cover_small') => {
  const isSmall = size.includes('small') || size.includes('thumb');
  const placeholder = isSmall ? defaultRelatedCover : defaultGameCoverLarge;

  if (imageInfo && typeof imageInfo.url === 'string' && imageInfo.url.trim() !== '') {
    let imageUrl = imageInfo.url;

    // 1. Aseguramos el protocolo HTTPS
    if (imageUrl.startsWith('//')) {
      imageUrl = `https:${imageUrl}`;
    }

    // 2. Lógica central para cambiar el tamaño
    const sizePattern = /\/t_[a-zA-Z0-9_]+\//;

    // Si pedimos el tamaño original, simplemente quitamos el especificador de tamaño
    if (size === 'original') {
      return imageUrl.replace(sizePattern, '/');
    }

    // Si la URL ya tiene un tamaño, lo reemplazamos por el que queremos
    if (imageUrl.match(sizePattern)) {
      return imageUrl.replace(sizePattern, `/t_${size}/`);
    }

    // Si no tenía tamaño, intentamos insertarlo (típico de IGDB)
    if (imageUrl.includes('/upload/')) {
      return imageUrl.replace('/upload/', `/upload/t_${size}/`);
    }

    // Si no se pudo modificar, devolvemos la URL tal cual
    return imageUrl;
  }

  // Si no hay imageInfo o URL, devolvemos el placeholder correspondiente
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

const recognizedWebsites = computed(() => {
  const websites = gameDetail.value?.game_info?.websites;
  if (!websites) return [];

  return websites.filter(website => getWebsiteIconName(website.url) !== 'sin-logo.svg');
});


const getWebsiteIconName = (urlString) => {
  if (!urlString) return 'sin-logo.svg'; // Icono por defecto

  // Mapa de dominios a nombres de icono
  const domainIconMap = {
    'steam': 'steam.svg',
    'gog.com': 'gog.svg',
    'epicgames.com': 'epicgames.svg',
    'ubisoft.com': 'ubisoft.svg',
    'ea.com': 'ea.svg',
    'battle.net': 'battle.svg',
    'rockstargames.com': 'rockstargames.svg',
    'playstation.com': 'playstation.svg',
    'xbox.com': 'xbox.svg',
    'youtube.com': 'youtube.svg',
    'twitch.tv': 'twitch.svg',
    'wikipedia.org': 'wikipedia.svg',
    'discord': 'discord.svg', 
    'facebook.com': 'facebook.svg',
    'instagram.com': 'instagram.svg',
    'x.com': 'x.svg',
    'twitter.com': 'x.svg', 
    'reddit.com': 'reddit.svg',
    'apple.com': 'appstore.svg',
    'play.google': 'googleplay.svg'
  };

  try {
    const url = new URL(urlString);
    const domain = url.hostname + url.pathname;

    // Busca una coincidencia en el mapa
    for (const key in domainIconMap) {
      if (domain.includes(key)) {
        return domainIconMap[key];
      }
    }

    return 'sin-logo.svg'; // Si no encuentra ninguna, devuelve el por defecto
  } catch (e) {
    return 'sin-logo.svg'; // En caso de URL inválida
  }
};

const getIconUrl = (iconName) => {
  return new URL(`/src/assets/icons-website/${iconName}`, import.meta.url).href;
};

const otherWebsites = computed(() => {
  const websites = gameDetail.value?.game_info?.websites;
  if (!websites) return [];

  return websites.filter(website => getWebsiteIconName(website.url) === 'sin-logo.svg');
});

const getYouTubeEmbedUrl = (videoId, autoplay = false) => {
  if (!videoId) return '';
  let url = `https://www.youtube.com/embed/${videoId}`;
  if (autoplay) {
    url += '?autoplay=1&rel=0';
  }
  return url;
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

// Recibe el objeto game_info completo
const formatReleaseStatus = (gameInfo) => {
  // 1. Comprobar si la fecha de lanzamiento es futura
  if (gameInfo.first_release_date) {
    // La fecha de la API viene en segundos, Date.now() está en milisegundos
    const releaseTimestampMs = Number(gameInfo.first_release_date) * 1000;
    if (releaseTimestampMs > Date.now()) {
      return 'Próximamente'; // Forzamos el estado si la fecha es futura
    }
  }

  // 2. Si la fecha no es futura, usamos esta lógica de estado.
  const statusCode = gameInfo.first_release_status;
  if (statusCode === null || statusCode === undefined) return 'No especificado';

  const statusMap = {
    "-1": "Lanzado", 
    "0": "Lanzado",
    "2": "Alpha",
    "3": "Beta",
    "4": "Acceso Anticipado",
    "5": "Offline",
    "6": "Cancelado",
    "7": "Rumoreado"
  };

  return statusMap[String(statusCode)] || `Estado Desconocido (${statusCode})`;
};

// Para obtener la clase CSS del estado de lanzamiento
const getReleaseStatusClass = (gameInfo) => {
  if (gameInfo.first_release_date) {
    const releaseTimestampMs = Number(gameInfo.first_release_date) * 1000;
    if (releaseTimestampMs > Date.now()) {
      return 'status-upcoming'; 
    }
  }

  const statusCode = gameInfo.first_release_status;
  if (statusCode === null || statusCode === undefined) return '';

  const statusMap = {
    '-1': 'status-released',        
    '0': 'status-released',
    '2': 'status-alpha',
    '3': 'status-beta',
    '4': 'status-early-access',
    '5': 'status-offline',
    '6': 'status-cancelled',
    '7': 'status-rumored'      
  };

  return statusMap[String(statusCode)] || 'status-other';
};

const formatPersonalPlatform = (platform) => {
  if (!platform) return 'No especificada';
  // Mapeo basado en UserPersonalPlatform enum (del backend) 
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
  };
  return platformMap[platform] || platform;
};

// Función para formatear el estado del juego del usuario
const formatUserGameStatus = (status) => {
  if (!status) return 'No especificado';
  // Mapeo basado en UserGameStatus enum (del backend) 
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
  };
  return statusMap[status] || status; // Devuelve el mapeado o el valor original
};

const formatDateSimple = (dateString) => {
  if (!dateString) return '';
  return dateString;
};

// --- NUEVO ESTADO para el formulario de la biblioteca ---
const showLibraryForm = ref(false);
const isAddingNewLibraryEntry = ref(false);
const isLoadingLibraryAction = ref(false);
const libraryActionMessage = ref('');
const libraryActionError = ref(false);


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


// Opciones para los selectores del formulario (basadas en tus enums/DTOs)
const gameStatusOptions = {
  playing: [
    { value: 'PLAYING', text: 'Jugando' },
    { value: 'PLAYING_PAUSED', text: 'Jugando (En Pausa)' },
    { value: 'PLAYING_ENDLESS', text: 'Jugando (Sin Fin)' },
  ],
  completed: [
    { value: 'COMPLETED', text: 'Completado' },
    { value: 'COMPLETED_MAIN_STORY', text: 'Historia Principal Completada' },
    { value: 'COMPLETED_MAIN_AND_SIDES', text: 'Principal + Secundarias' },
    { value: 'COMPLETED_100_PERCENT', text: 'Completado al 100%' },
  ],
  archived: [
    { value: 'ARCHIVED', text: 'Archivado' },
    { value: 'ARCHIVED_ABANDONED', text: 'Archivado (Abandonado)' },
    { value: 'ARCHIVED_NOT_PLAYING', text: 'Archivado (Sin Jugar)' },
  ]
};
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


const openLibraryModal = (isNew = false) => {
  isAddingNewLibraryEntry.value = isNew;
  libraryActionMessage.value = '';
  libraryActionError.value = false;
  modalStep.value = 0; // Siempre empezamos en el paso 0
  mainStatusCategory.value = null;

  // Limpiar formulario antes de rellenar
  Object.keys(libraryForm).forEach(key => {
    libraryForm[key] = (typeof libraryForm[key] === 'boolean' ? false : (typeof libraryForm[key] === 'string' ? '' : null));
  });

  if (!isNew && gameDetail.value?.user_game_data) {
    // Modo Edición: Rellenamos el formulario con los datos existentes
    const data = gameDetail.value.user_game_data;
    Object.keys(libraryForm).forEach(key => {
      if (data[key] !== undefined && data[key] !== null) {
        libraryForm[key] = data[key];
      }
    });

    // Determinamos la categoría principal para preseleccionar la opción
    const currentStatus = data.status || '';
    if (currentStatus.includes('PLAYING')) mainStatusCategory.value = 'playing';
    else if (currentStatus.includes('COMPLETED')) mainStatusCategory.value = 'completed';
    else if (currentStatus.includes('ARCHIVED')) mainStatusCategory.value = 'archived';
    else if (currentStatus === 'WISHLIST') mainStatusCategory.value = 'wishlist';

  }
  showLibraryForm.value = true;
};

const closeLibraryModal = () => {
  showLibraryForm.value = false;
};

// Navegación del asistente
const setStatusCategoryAndContinue = (category) => {
  console.log(`[PASO 0] Botón pulsado. Categoría seleccionada: '${category}'`);
  mainStatusCategory.value = category;
  if (category === 'wishlist') {
    libraryForm.status = 'WISHLIST';
    handleSaveToLibrary(); // Guardar directamente
  } else {
    // Asignamos un estado por defecto de la categoría
    libraryForm.status = gameStatusOptions[category][0].value;
    modalStep.value = 1;
  }
};

const computedStatusOptions = computed(() => {
  // Si no se ha seleccionado una categoría principal (ej: 'jugando'), devuelve un array vacío.
  if (!mainStatusCategory.value) return [];

  // Si se ha seleccionado, devuelve el array de opciones correspondiente a esa categoría.
  // Por ejemplo, si mainStatusCategory es 'completed', devolverá la lista de estados de completado.
  return gameStatusOptions[mainStatusCategory.value] || [];
});

const goToStep = (step) => {
  modalStep.value = step;
};




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
  // 1. Se asegura de que existe un ID de juego para guardar.
  if (!igdbId.value) return;

  // 2. Activa los indicadores de "cargando" para la interfaz.
  isLoadingLibraryAction.value = true;
  libraryActionMessage.value = '';
  libraryActionError.value = false;

  // 3. Crea una copia de los datos del formulario para enviarla.
  const dto = { ...libraryForm };

  // 4. Lógica clave: Limpia los campos de horas que NO fueron seleccionados en el desplegable.
  //    Esto asegura que solo se guarde el tipo de horas que el usuario introdujo.
  const hourTypes = ['story_duration_hours', 'story_secondary_duration_hours', 'completionist_duration_hours'];
  hourTypes.forEach(type => {
    if (type !== hourInputType.value) {
      dto[type] = null;
    }
  });

  // 5. Intenta guardar los datos en la base de datos a través de la API.
  try {
    const response = await addOrUpdateGameInUserLibrary(Number(igdbId.value), dto);
    // Si tiene éxito, actualiza los datos del juego en la página y cierra el modal.
    gameDetail.value.user_game_data = response.data;
    closeLibraryModal();
  } catch (error) {
    // 6. Si hay un error, lo captura y muestra un mensaje al usuario.
    libraryActionError.value = true;
    libraryActionMessage.value = `Error: ${error.response?.data?.message || 'No se pudo guardar.'}`;
  } finally {
    // 7. Se asegura de desactivar el indicador de "cargando", tanto si ha habido éxito como si ha habido un error.
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

<style src="./GameDetailsView.css" scoped></style>