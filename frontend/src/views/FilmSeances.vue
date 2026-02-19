<template>
  <div class="film-seances">
    <!-- Header avec infos du film -->
    <div class="film-header-section" v-if="film">
      <div class="film-header-content">
        <div class="film-poster">
          <div class="poster-placeholder" v-if="!film.imageUrl">
            <i class="bi bi-film"></i>
          </div>
          <img v-else :src="getFullImageUrl(film.imageUrl)" :alt="film.titre" class="poster-image">
        </div>

        <div class="film-info">
          <h1 class="film-title">{{ film.titre }}</h1>
          <div class="film-meta">
            <span class="meta-item">
              <i class="bi bi-clock me-1"></i>
              {{ film.dureeMinutes }} min
            </span>
            <span class="meta-item" v-if="film.genre">
              <i class="bi bi-tag me-1"></i>
              {{ film.genre }}
            </span>
            <span class="meta-item" v-if="film.dateSortie">
              <i class="bi bi-calendar-event me-1"></i>
              {{ new Date(film.dateSortie).getFullYear() }}
            </span>
            <span class="meta-item rating" v-if="film.note">
              <i class="bi bi-star-fill me-1"></i>
              {{ film.note }}/10
            </span>
          </div>

          <div class="film-description" v-if="film.description">
            <p>{{ film.description }}</p>
          </div>

          <div class="film-credits">
            <div v-if="film.realisateur" class="credit-item">
              <strong>Réalisateur :</strong> {{ film.realisateur }}
            </div>
            <div v-if="film.acteurs" class="credit-item">
              <strong>Acteurs :</strong> {{ film.acteurs }}
            </div>
            <div class="credit-item">
              <strong>Langue :</strong> {{ film.langue || 'Non spécifié' }}
              <span v-if="film.pays"> • {{ film.pays }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Section des séances -->
    <div class="seances-section">
      <div class="container-fluid px-4">
        <!-- Header des séances -->
        <div class="seances-header">
          <div class="header-content">
            <div>
              <h2 class="seances-title">
                <i class="bi bi-calendar-event me-3"></i>
                Séances disponibles
              </h2>
              <p class="seances-subtitle">Choisissez votre séance préférée</p>
            </div>

            <!-- Filtres rapides -->
            <div class="quick-filters">
              <div class="filter-group">
                <label class="filter-label">
                  <i class="bi bi-funnel me-2"></i>
                  Trier par :
                </label>
                <div class="filter-buttons">
                  <button
                    @click="setSortBy('heure')"
                    :class="['filter-btn', { active: sortBy === 'heure' }]"
                  >
                    <i class="bi bi-clock me-1"></i>
                    Heure
                  </button>
                  <button
                    @click="setSortBy('prix')"
                    :class="['filter-btn', { active: sortBy === 'prix' }]"
                  >
                    <i class="bi bi-cash me-1"></i>
                    Prix
                  </button>
                  <button
                    @click="setSortBy('places')"
                    :class="['filter-btn', { active: sortBy === 'places' }]"
                  >
                    <i class="bi bi-ticket-perforated me-1"></i>
                    Places
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- État de chargement -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Chargement des séances...</span>
            </div>
          </div>
          <p>Chargement des séances...</p>
        </div>

        <!-- Séances groupées par jour -->
        <div v-else-if="seancesParJour.length > 0" class="seances-content">
          <div v-for="jour in seancesParJour" :key="jour.date" class="jour-section">
            <h3 class="jour-title">
              <i class="bi bi-calendar-day me-2"></i>
              {{ formatDateLongue(jour.date) }}
            </h3>

            <div class="seances-grid">
              <div
                v-for="seance in jour.seances"
                :key="seance.id"
                class="seance-card"
                :class="{ 'almost-expired': isSeanceAlmostExpired(seance) }"
                @click="reserverSeance(seance.id)"
              >
                <!-- En-tête simple -->
                <div class="card-header">
                  <div class="header-content">
                    <div>
                      <div class="seance-time">
                        <i class="bi bi-clock me-2"></i>
                        <strong>{{ formatTime(seance.dateHeure) }}</strong>
                      </div>
                      <div class="seance-venue">
                        <i class="bi bi-building me-2"></i>
                        {{ seance.salle?.nom }}
                      </div>
                    </div>
                    <div v-if="isSeanceAlmostExpired(seance)" class="expiry-alert">
                      <i class="bi bi-exclamation-triangle-fill"></i>
                      <span>Bientôt !</span>
                    </div>
                  </div>
                </div>

                <!-- Corps avec prix et places -->
                <div class="card-body">
                  <div class="price-section">
                    <div class="price-display">
                      <span class="price-amount">{{ seance.prix }}</span>
                      <span class="price-currency">€</span>
                    </div>
                    <div class="price-label">par place</div>
                  </div>

                  <div class="availability-section">
                    <div class="availability-bar">
                      <div
                        class="availability-fill"
                        :style="{ width: getPlacesPercentage(seance) + '%' }"
                        :class="getAvailabilityClass(seance)"
                      ></div>
                    </div>
                    <div class="availability-text">
                      {{ seance.placesDisponibles }}/{{ seance.salle?.capacite }} places restantes
                    </div>
                  </div>
                </div>

                <!-- Bouton d'action -->
                <div class="card-footer">
                  <button
                    class="btn w-100"
                    :class="seance.placesDisponibles > 0 ? 'btn-warning' : 'btn-secondary disabled'"
                    @click.stop="reserverSeance(seance.id)"
                  >
                    <i class="bi bi-ticket me-2"></i>
                    {{ seance.placesDisponibles > 0 ? 'Réserver' : 'Complet' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Aucune séance -->
        <div v-else class="no-seances">
          <div class="no-seances-content">
            <div class="no-seances-icon">
              <i class="bi bi-calendar-x"></i>
            </div>
            <h3>Aucune séance disponible</h3>
            <p>Ce film n'est pas programmé pour le moment.</p>
            <router-link to="/" class="btn btn-primary">
              <i class="bi bi-arrow-left me-2"></i>
              Retour aux films
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { filmService, seanceService } from '../services/api'

export default {
  name: 'FilmSeances',
  props: ['filmId'],
  data() {
    return {
      film: null,
      seances: [],
      loading: true,
      sortBy: 'heure' // 'heure', 'prix', 'places'
    }
  },
  computed: {
    seancesParJour() {
      // Grouper les séances par jour
      const groupes = {}

      // Filtrer les séances passées
      const now = new Date()
      const seancesFutures = this.seances.filter(seance => {
        const seanceDate = new Date(seance.dateHeure)
        return seanceDate > now
      })

      seancesFutures.forEach(seance => {
        const date = new Date(seance.dateHeure).toDateString()
        if (!groupes[date]) {
          groupes[date] = {
            date: date,
            seances: []
          }
        }
        groupes[date].seances.push(seance)
      })

      // Trier les séances de chaque jour selon le critère choisi
      Object.values(groupes).forEach(groupe => {
        groupe.seances.sort((a, b) => {
          switch (this.sortBy) {
            case 'prix':
              return a.prix - b.prix
            case 'places':
              return b.placesDisponibles - a.placesDisponibles
            case 'heure':
            default:
              return new Date(a.dateHeure) - new Date(b.dateHeure)
          }
        })
      })

      // Retourner les groupes triés par date
      return Object.values(groupes).sort((a, b) => new Date(a.date) - new Date(b.date))
    }
  },
  async mounted() {
    await this.loadFilm()
    await this.loadSeances()
  },
  methods: {
    async loadFilm() {
      try {
        // Pour l'instant, on récupère tous les films et on trouve celui qui correspond
        // TODO: Ajouter un endpoint pour récupérer un film par ID
        const response = await filmService.getAllFilms()
        this.film = response.data.find(f => f.id == this.filmId)
      } catch (error) {
        console.error('Erreur lors du chargement du film:', error)
      }
    },

    async loadSeances() {
      try {
        // Récupérer toutes les séances et filtrer par filmId
        const response = await seanceService.getAllSeances()
        this.seances = response.data.filter(s => s.film && s.film.id == this.filmId)
      } catch (error) {
        console.error('Erreur lors du chargement des séances:', error)
        this.seances = []
      } finally {
        this.loading = false
      }
    },

    reserverSeance(seanceId) {
      this.$router.push(`/reservation/${seanceId}`)
    },

    formatDateLongue(dateString) {
      const date = new Date(dateString)
      return date.toLocaleDateString('fr-FR', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    },

    formatTime(dateTimeString) {
      const date = new Date(dateTimeString)
      return date.toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    getStatusClass(seance) {
      if (seance.placesDisponibles === 0) return 'status-full'
      if (seance.placesDisponibles <= 5) return 'status-low'
      return 'status-available'
    },

    getStatusIcon(seance) {
      if (seance.placesDisponibles === 0) return 'bi-x-circle-fill'
      if (seance.placesDisponibles <= 5) return 'bi-exclamation-triangle-fill'
      return 'bi-check-circle-fill'
    },

    getStatusText(seance) {
      if (seance.placesDisponibles === 0) return 'Complet'
      if (seance.placesDisponibles <= 5) return 'Dernières places'
      return 'Disponible'
    },

    getPlacesPercentage(seance) {
      if (!seance.salle?.capacite) return 0
      return (seance.placesDisponibles / seance.salle.capacite) * 100
    },

    getAvailabilityClass(seance) {
      const percentage = this.getPlacesPercentage(seance)
      if (percentage > 50) return 'availability-high'
      if (percentage > 25) return 'availability-medium'
      return 'availability-low'
    },

    isSeanceAlmostExpired(seance) {
      const now = new Date()
      const seanceDate = new Date(seance.dateHeure)
      const diffInHours = (seanceDate - now) / (1000 * 60 * 60)
      return diffInHours <= 2 && diffInHours > 0
    },

    getAvailabilityIcon(seance) {
      if (seance.placesDisponibles === 0) return 'bi-dash-circle-fill'
      if (seance.placesDisponibles <= 5) return 'bi-exclamation-triangle-fill'
      return 'bi-check-circle-fill'
    },

    setSortBy(criteria) {
      this.sortBy = criteria
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

/* Section header du film */
.film-header-section {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  color: var(--text-primary);
  padding: var(--space-8) 0;
  border-bottom: 1px solid var(--border-color);
}

.film-header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--space-4);
  display: flex;
  gap: var(--space-6);
  align-items: flex-start;
}

.film-poster {
  flex-shrink: 0;
}

.poster-placeholder,
.poster-image {
  width: 150px;
  height: 225px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
}

.poster-placeholder {
  background: linear-gradient(135deg, #6c757d, var(--primary-dark));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
}

.poster-image {
  object-fit: cover;
}

.film-info {
  flex: 1;
}

.film-title {
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--space-4);
  line-height: 1.2;
}

.film-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.film-meta .meta-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  background: var(--bg-primary);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.film-meta .rating {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: white;
}

.film-description p {
  font-size: var(--font-size-lg);
  line-height: 1.6;
  margin-bottom: var(--space-4);
  color: var(--text-secondary);
}

.film-credits {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.credit-item {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
}

.credit-item strong {
  color: #6c757d;
  margin-right: var(--space-2);
}

/* Section des séances */
.seances-section {
  padding: var(--space-8) 0;
  background: var(--bg-primary);
}

.seances-header {
  margin-bottom: var(--space-8);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
}

.seances-title {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.seances-subtitle {
  font-size: var(--font-size-lg);
  color: var(--text-secondary);
  margin: 0;
}

/* Filtres rapides */
.quick-filters {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.filter-label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--text-secondary);
  margin: 0;
  white-space: nowrap;
}

.filter-buttons {
  display: flex;
  gap: var(--space-2);
}

.filter-btn {
  padding: var(--space-2) var(--space-3);
  border: 2px solid var(--border-color);
  background: var(--bg-primary);
  color: var(--text-secondary);
  border-radius: 25px;
  font-size: var(--font-size-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.filter-btn:hover {
  border-color: #6c757d;
  color: #6c757d;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(108, 117, 125, 0.2);
}

.filter-btn.active {
  background: #6c757d;
  border-color: #6c757d;
  color: white;
  box-shadow: 0 4px 12px rgba(108, 117, 125, 0.3);
}

/* Groupes par jour */
.jour-section {
  margin-bottom: var(--space-8);
}

.jour-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-3);
  border-bottom: 2px solid #6c757d;
}

.seances-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: var(--space-7);
  perspective: 1000px;
}

.seance-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.seance-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border-color: #6c757d;
}

/* Header de la carte */
.card-header {
  background: white;
  color: #2d3748;
  padding: var(--space-4);
  border: none;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header.almost-expired {
  background: linear-gradient(135deg, #fff3cd, #ffeaa7);
  border-bottom: 2px solid #ffc107;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
}

.expiry-alert {
  background: #dc3545;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 8px rgba(220, 53, 69, 0.3);
}

.expiry-alert i {
  font-size: 0.8rem;
}

.seance-time,
.seance-venue {
  display: flex;
  align-items: center;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.seance-time strong {
  color: var(--text-primary);
  font-weight: 600;
}

.seance-time-wrapper,
.seance-salle-wrapper {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex: 1;
}

.seance-time-wrapper {
  justify-content: flex-start;
}

.seance-salle-wrapper {
  justify-content: flex-end;
  text-align: right;
}

.time-icon,
.salle-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.time-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(108, 117, 125, 0.3);
}

.salle-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3);
}

.time-content,
.salle-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.time-main,
.salle-name {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.time-duration,
.salle-type {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Corps de la carte */
.seance-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
  padding: var(--space-5);
  background: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.price-section {
  text-align: center;
  flex: 1;
}

.price-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  margin-bottom: 4px;
}

.price-amount {
  font-size: 2rem;
  font-weight: 800;
  color: #6c757d;
  line-height: 1;
}

.price-currency {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.price-label {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.availability-section {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex: 1;
  justify-content: flex-end;
}

.availability-indicator {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.availability-good {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  animation: pulse-green 3s infinite;
}

.availability-low {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  animation: pulse-orange 2s infinite;
}

.availability-full {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
}

@keyframes pulse-green {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

@keyframes pulse-orange {
  0%, 100% { transform: scale(1); box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3); }
  50% { transform: scale(1.08); box-shadow: 0 6px 20px rgba(245, 158, 11, 0.5); }
}

.availability-text {
  text-align: right;
}

.places-count {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.places-label {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* Corps de la carte */
.card-body {
  padding: var(--space-4);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-section {
  text-align: center;
  flex: 1;
}

.price-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  margin-bottom: 4px;
}

.price-amount {
  font-size: 1.8rem;
  font-weight: 800;
  color: #6c757d;
  line-height: 1;
}

.price-currency {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.price-label {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.availability-section {
  flex: 1;
}

.availability-bar {
  width: 100%;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  margin-bottom: var(--space-2);
  overflow: hidden;
}

.availability-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.availability-high {
  background: var(--success-color);
}

.availability-medium {
  background: var(--warning-color);
}

.availability-low {
  background: var(--danger-color);
}

.availability-text {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  text-align: center;
  font-weight: 500;
}

/* Footer de la carte */
.card-footer {
  padding: var(--space-4);
  background: #f8f9fa;
  border-top: 1px solid var(--border-color);
}

.card-footer .btn {
  font-weight: 600;
  padding: var(--space-3) var(--space-4);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.card-footer .btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
}

.card-footer .btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  border-radius: 20px;
  font-size: var(--font-size-sm);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.status-available {
  background: rgba(16, 185, 129, 0.15);
  color: #059669;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.status-low {
  background: rgba(245, 158, 11, 0.15);
  color: #d97706;
  border: 1px solid rgba(245, 158, 11, 0.2);
}

.status-full {
  background: rgba(239, 68, 68, 0.15);
  color: #dc2626;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.cta-section {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: #6c757d;
  font-weight: 600;
  transition: all 0.3s ease;
}

.seance-card:hover .cta-section {
  transform: translateX(4px);
}

.cta-text {
  font-size: var(--font-size-sm);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.cta-arrow {
  transition: transform 0.3s ease;
}

.seance-card:hover .cta-arrow {
  transform: translateX(4px);
}

/* État vide */
.no-seances {
  text-align: center;
  padding: var(--space-12) var(--space-4);
}

.no-seances-content {
  max-width: 500px;
  margin: 0 auto;
}

.no-seances-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, var(--secondary-color), var(--info-color));
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-4);
  color: white;
  font-size: 2.5rem;
  box-shadow: var(--shadow-lg);
}

.no-seances h3 {
  color: var(--text-primary);
  margin-bottom: var(--space-3);
  font-weight: var(--font-weight-semibold);
}

.no-seances p {
  color: var(--text-secondary);
  margin-bottom: var(--space-6);
}

/* Responsive */
@media (max-width: 992px) {
  .film-header-content {
    flex-direction: column;
    text-align: center;
    gap: var(--space-4);
  }

  .film-title {
    font-size: var(--font-size-3xl);
  }

  .header-content {
    flex-direction: column;
    text-align: center;
    gap: var(--space-3);
  }

  .quick-filters {
    justify-content: center;
  }

  .seances-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .film-header-section {
    padding: var(--space-6) 0;
  }

  .film-header-content {
    padding: 0 var(--space-3);
  }

  .poster-placeholder,
  .poster-image {
    width: 120px;
    height: 180px;
  }

  .film-title {
    font-size: var(--font-size-2xl);
  }

  .seances-title {
    font-size: var(--font-size-2xl);
  }

  .quick-filters {
    flex-direction: column;
    gap: var(--space-3);
  }

  .filter-buttons {
    justify-content: center;
    flex-wrap: wrap;
  }

  .seances-grid {
    grid-template-columns: 1fr;
    gap: var(--space-4);
  }

  .seance-card {
    padding: var(--space-4);
  }

  .seance-header {
    flex-direction: column;
    gap: var(--space-3);
    align-items: center;
    text-align: center;
  }

  .seance-time-wrapper,
  .seance-salle-wrapper {
    justify-content: center;
  }

  .seance-body {
    flex-direction: column;
    gap: var(--space-4);
    text-align: center;
  }

  .availability-section {
    justify-content: center;
  }
}

@media (max-width: 576px) {
  .film-title {
    font-size: var(--font-size-xl);
  }

  .film-meta {
    justify-content: center;
  }
}
</style>
