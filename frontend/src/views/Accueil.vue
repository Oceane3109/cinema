<template>
  <!-- Section Recherche MulticritÃ¨re -->
  <div class="search-section mb-5">
    <div class="search-container">
      <div class="search-header">
        <h2 class="search-title">
          <i class="bi bi-search me-3"></i>
          Rechercher des Films
        </h2>
        <p class="search-subtitle">Trouvez le film parfait selon vos critères</p>
      </div>

      <div class="search-form">
        <div class="search-grid">
          <!-- Recherche par titre -->
          <div class="search-field">
            <label class="field-label">
              <i class="bi bi-film me-2"></i>
              Titre du film
            </label>
            <input
              type="text"
              v-model="searchCriteria.titre"
              class="form-control search-input"
              placeholder="Ex: Avatar, Titanic..."
            >
          </div>

          <!-- Recherche par genre -->
          <div class="search-field">
            <label class="field-label">
              <i class="bi bi-tag me-2"></i>
              Genre
            </label>
            <select v-model="searchCriteria.genre" class="form-select search-select">
              <option value="">Tous les genres</option>
              <option v-for="genre in availableGenres" :key="genre" :value="genre">{{ genre }}</option>
            </select>
          </div>

          <!-- Recherche par rÃ©alisateur -->
          <div class="search-field">
            <label class="field-label">
              <i class="bi bi-person-video me-2"></i>
              Réalisateur
            </label>
            <input
              type="text"
              v-model="searchCriteria.realisateur"
              class="form-control search-input"
              placeholder="Ex: James Cameron, Spielberg..."
            >
          </div>

          <!-- Recherche par acteur -->
          <div class="search-field">
            <label class="field-label">
              <i class="bi bi-people me-2"></i>
              Acteur
            </label>
            <input
              type="text"
              v-model="searchCriteria.acteurs"
              class="form-control search-input"
              placeholder="Ex: Leonardo DiCaprio, Tom Hanks..."
            >
          </div>

          <!-- Recherche par annÃ©e de sortie -->
          <div class="search-field">
            <label class="field-label">
              <i class="bi bi-calendar me-2"></i>
              Année de sortie
            </label>
            <input
              type="number"
              v-model.number="searchCriteria.annee"
              class="form-control search-input"
              placeholder="Ex: 2021, 1994..."
              min="1900"
              max="2030"
            >
            <div class="year-suggestions" v-if="availableYears.length > 0 && !searchCriteria.annee">
              <small class="text-muted">Années disponibles: {{ availableYears.slice(0, 5).join(', ') }}{{ availableYears.length > 5 ? '...' : '' }}</small>
            </div>
          </div>

          <!-- Boutons d'action -->
          <div class="search-field">
            <label class="field-label">&nbsp;</label>
            <div class="search-buttons">
              <button
                @click="searchFilms"
                class="btn btn-primary search-btn btn-sm"
                :disabled="!hasAnySearchCriteria"
              >
                <i class="bi bi-search me-1"></i>
                Rechercher
              </button>
              <button
                @click="resetSearch"
                class="btn btn-outline-secondary reset-btn btn-sm"
              >
                <i class="bi bi-arrow-counterclockwise me-1"></i>
                Réinitialiser
              </button>
            </div>
          </div>
        </div>

        <!-- RÃ©sultats de recherche -->
        <div class="search-results" v-if="films.length > 0">
          <div class="results-info">
            <span class="results-count">
              <i class="bi bi-film me-2"></i>
              {{ filteredFilms.length }} film{{ filteredFilms.length > 1 ? 's' : '' }} trouvé{{ filteredFilms.length > 1 ? 's' : '' }}
              <span v-if="hasActiveFilters">sur {{ allFilms.length }}</span>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Ã‰tat de chargement -->
  <div v-if="loading" class="loading-state">
    <div class="loading-content">
      <div class="loading-spinner">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Chargement des films...</span>
        </div>
      </div>
      <h4>Chargement des films</h4>
      <p>Veuillez patienter pendant que nous récupérons notre catalogue...</p>
    </div>
  </div>

  <!-- Ã‰tat vide -->
  <div v-else-if="films.length === 0" class="empty-state">
    <div class="empty-content">
      <div class="empty-icon">
        <i class="bi bi-film"></i>
      </div>
      <h3>Aucun film disponible</h3>
      <p>Notre catalogue est actuellement vide. Revenez bientôt pour découvrir nos prochaines projections !</p>
      <button class="btn btn-outline-primary" @click="loadFilms">
        <i class="bi bi-arrow-clockwise me-2"></i>
        Actualiser
      </button>
    </div>
  </div>

  <!-- Grille des films -->
  <div v-else class="films-section">
    <div class="films-grid">
      <div v-for="film in films" :key="film.id" class="film-card" @click="rechercherSeances(film.id)">
        <!-- Image de fond -->
        <div class="film-background">
          <div class="film-image-overlay" v-if="!film.imageUrl">
            <i class="bi bi-film"></i>
          </div>
          <img
            v-else
            :src="getFullImageUrl(film.imageUrl)"
            :alt="film.titre"
            class="film-background-image"
          >
        </div>

        <!-- Overlay avec informations -->
        <div class="film-overlay-content">
          <!-- Informations principales -->
          <div class="film-info">
            <div class="film-header-info">
              <h3 class="film-title">{{ film.titre }}</h3>
              <div class="film-rating" v-if="film.note">
                <div class="rating-stars">
                  <i class="bi bi-star-fill"></i>
                  <span class="rating-value">{{ film.note }}</span>
                  <span class="rating-text">/10</span>
                </div>
              </div>
            </div>

            <div class="film-meta">
              <span class="meta-item">
                <i class="bi bi-clock me-1"></i>
                {{ film.dureeMinutes || 'N/A' }} min
              </span>
              <span class="meta-item" v-if="film.genre">
                <i class="bi bi-tag me-1"></i>
                {{ film.genre }}
              </span>
              <span class="meta-item" v-if="film.dateSortie">
                <i class="bi bi-calendar-event me-1"></i>
                {{ new Date(film.dateSortie).getFullYear() }}
              </span>
            </div>
          </div>
        </div>

        <!-- Détails au hover (optionnel) -->
        <div class="film-details-hover">
          <div class="film-description">
            <p>{{ film.description || 'Aucune description disponible pour ce film.' }}</p>
          </div>

          <div class="film-details-compact">
            <div v-if="film.realisateur" class="detail-compact">
              <strong>Réalisateur:</strong> {{ film.realisateur }}
            </div>
            <div v-if="film.acteurs" class="detail-compact">
              <strong>Acteurs:</strong> {{ film.acteurs }}
            </div>
            <div class="detail-compact">
              <strong>Langue:</strong> {{ film.langue || 'Non spÃ©cifiÃ©' }}
              <span v-if="film.pays"> • {{ film.pays }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { filmService, seanceService } from '../services/api'

export default {
  name: 'Accueil',
  data() {
    return {
      allFilms: [],
      films: [],
      loading: true,
      searchLoading: false,
      searchCriteria: {
        titre: '',
        genre: '',
        realisateur: '',
        acteurs: '',
        annee: null
      },
      availableGenres: [],
      availableYears: []
    }
  },
  computed: {
    filteredFilms() {
      return this.films
    },
    hasActiveFilters() {
      return Object.values(this.searchCriteria).some(value =>
        value !== null && value !== '' && (typeof value === 'string' ? value.trim() !== '' : value > 0)
      )
    },
    hasAnySearchCriteria() {
      return Object.values(this.searchCriteria).some(value =>
        value !== null && value !== '' && (typeof value === 'string' ? value.trim() !== '' : value > 0)
      )
    }
  },
  async mounted() {
    await this.loadFilms()
  },
  methods: {
    async loadFilms() {
      try {
        const response = await filmService.getAllFilms()
        this.allFilms = response.data
        this.extractAvailableFilters()
        this.searchFilms()
      } catch (error) {
        console.error('Erreur lors du chargement des films:', error)
        this.allFilms = []
        this.films = []
      } finally {
        this.loading = false
      }
    },

    extractAvailableFilters() {
      // Extraire les genres uniques
      const genres = new Set()
      // Extraire les annÃ©es de sortie uniques
      const years = new Set()

      this.allFilms.forEach(film => {
        if (film.genre) {
          genres.add(film.genre)
        }
        if (film.dateSortie) {
          const year = new Date(film.dateSortie).getFullYear()
          if (year) years.add(year)
        }
      })

      this.availableGenres = Array.from(genres).sort()
      this.availableYears = Array.from(years).sort((a, b) => b - a) // Plus rÃ©cent en premier
    },

    searchFilms() {
      let filtered = [...this.allFilms]

      // Filtre par titre
      if (this.searchCriteria.titre.trim()) {
        const searchTerm = this.searchCriteria.titre.toLowerCase().trim()
        filtered = filtered.filter(film =>
          film.titre && film.titre.toLowerCase().includes(searchTerm)
        )
      }

      // Filtre par genre
      if (this.searchCriteria.genre) {
        filtered = filtered.filter(film => film.genre === this.searchCriteria.genre)
      }

      // Filtre par rÃ©alisateur
      if (this.searchCriteria.realisateur.trim()) {
        const searchTerm = this.searchCriteria.realisateur.toLowerCase().trim()
        filtered = filtered.filter(film =>
          film.realisateur && film.realisateur.toLowerCase().includes(searchTerm)
        )
      }

      // Filtre par acteurs
      if (this.searchCriteria.acteurs.trim()) {
        const searchTerm = this.searchCriteria.acteurs.toLowerCase().trim()
        filtered = filtered.filter(film =>
          film.acteurs && film.acteurs.toLowerCase().includes(searchTerm)
        )
      }

      // Filtre par annÃ©e
      if (this.searchCriteria.annee && this.searchCriteria.annee > 0) {
        filtered = filtered.filter(film => {
          if (!film.dateSortie) return false
          const filmYear = new Date(film.dateSortie).getFullYear()
          return filmYear === this.searchCriteria.annee
        })
      }

      this.films = filtered
    },

    resetSearch() {
      this.searchCriteria = {
        titre: '',
        genre: '',
        realisateur: '',
        acteurs: '',
        annee: null
      }
      this.films = [...this.allFilms]
    },

    rechercherSeances(filmId) {
      // Rediriger vers la page dédiée des séances du film
      this.$router.push(`/film/${filmId}/seances`)
    },

    formatDate(dateString) {
      if (!dateString) return 'Non spÃ©cifiÃ©e'
      const date = new Date(dateString)
      return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    },

    showAlert(message, type) {
      // CrÃ©er une alerte temporaire
      const alertDiv = document.createElement('div')
      alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed`
      alertDiv.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;'
      alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
      `

      document.body.appendChild(alertDiv)

      // Auto-suppression aprÃ¨s 5 secondes
      setTimeout(() => {
        if (alertDiv.parentNode) {
          alertDiv.remove()
        }
      }, 5000)
    },

    getFullImageUrl(url) {
      if (!url) return ''
      if (url.startsWith('http')) return url
      return `http://localhost:8080${url}`
    }
  }
}
</script>

<style scoped>
@import '../assets/accueil.css';

/* Section Mes Réservations */
.user-section {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-top: 1px solid #dee2e6;
}

.icon-wrapper {
  display: inline-block;
  padding: 1rem;
  background: rgba(13, 110, 253, 0.1);
  border-radius: 50%;
  margin-bottom: 1rem;
}

.card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15) !important;
}

.btn-lg {
  padding: 0.75rem 2rem;
  font-size: 1.1rem;
  font-weight: 500;
}
</style>
