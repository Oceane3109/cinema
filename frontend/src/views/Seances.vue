<template>
  <div class="seances-page">
    <!-- Header avec filtres -->
    <div class="page-header">
      <div class="header-content">
        <div>
          <h1 class="page-title">
            <i class="bi bi-calendar-event me-3"></i>
            Toutes les séances
          </h1>
          <p class="page-subtitle">Découvrez toutes les séances disponibles dans nos cinémas</p>
        </div>

        <!-- Filtres rapides -->
        <div class="header-actions">
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

          <button @click="loadSeances" class="btn btn-outline-primary ms-3" :disabled="loading">
            <i class="bi bi-arrow-clockwise me-2" :class="{ 'fa-spin': loading }"></i>
            Actualiser
          </button>
        </div>
      </div>
    </div>

    <!-- État de chargement -->
    <div v-if="loading" class="loading-state">
      <div class="loading-content">
        <div class="loading-spinner">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Chargement des séances...</span>
          </div>
        </div>
        <h4>Chargement des séances</h4>
        <p>Veuillez patienter pendant que nous récupérons les horaires...</p>
      </div>
    </div>

    <!-- Aucune séance -->
    <div v-else-if="seances.length === 0" class="no-seances">
      <div class="no-seances-content">
        <div class="no-seances-icon">
          <i class="bi bi-calendar-x"></i>
        </div>
        <h3>Aucune séance disponible</h3>
        <p>Il n'y a actuellement aucune séance programmée.</p>
        <button @click="loadSeances" class="btn btn-primary">
          <i class="bi bi-arrow-clockwise me-2"></i>
          Réessayer
        </button>
      </div>
    </div>

    <!-- Onglets par jour -->
    <div v-else class="seances-section">
      <!-- Navigation par onglets -->
      <div class="tabs-container">
        <div class="tabs-nav">
          <button
            v-for="jour in joursDisponibles"
            :key="jour.date"
            @click="jourActif = jour.date"
            :class="['tab-button', { active: jourActif === jour.date }]"
          >
            <div class="tab-day">{{ formatDateTab(jour.date) }}</div>
            <div class="tab-count">{{ jour.seances.length }} séance{{ jour.seances.length > 1 ? 's' : '' }}</div>
          </button>
        </div>
      </div>

      <!-- Contenu des onglets -->
      <div class="tabs-content">
        <div
          v-for="jour in joursDisponibles"
          :key="jour.date"
          v-show="jourActif === jour.date"
          class="tab-pane"
        >
          <div class="seances-grid">
            <div
              v-for="seance in jour.seances"
              :key="seance.id"
              class="seance-card"
              :class="{ 'almost-expired': isSeanceAlmostExpired(seance) }"
              @click="reserverSeance(seance.id)"
            >
          <!-- Header avec titre du film -->
          <div class="card-header">
            <div class="header-content">
              <div>
                <h5 class="film-title">{{ seance.film?.titre }}</h5>
                <div class="film-genre" v-if="seance.film?.genre">
                  {{ seance.film.genre }}
                </div>
              </div>
              <div v-if="isSeanceAlmostExpired(seance)" class="expiry-alert">
                <i class="bi bi-exclamation-triangle-fill"></i>
                <span>Bientôt !</span>
              </div>
            </div>
          </div>

          <!-- Corps de la carte -->
          <div class="card-body">
            <div class="seance-info">
              <!-- Date et heure -->
              <div class="info-row">
                <div class="info-item">
                  <i class="bi bi-calendar-event me-2"></i>
                  <span>{{ formatDate(seance.dateHeure) }}</span>
                </div>
                <div class="info-item">
                  <i class="bi bi-clock me-2"></i>
                  <span>{{ formatTime(seance.dateHeure) }}</span>
                </div>
              </div>

              <!-- Salle et prix -->
              <div class="info-row">
                <div class="info-item">
                  <i class="bi bi-building me-2"></i>
                  <span>{{ seance.salle?.nom }}</span>
                </div>
                <div class="info-item price">
                  <i class="bi bi-cash me-2"></i>
                  <span>{{ seance.prix }}€</span>
                </div>
              </div>

              <!-- Places disponibles -->
              <div class="availability-info">
                <div class="availability-bar">
                  <div
                    class="availability-fill"
                    :style="{ width: getPlacesPercentage(seance) + '%' }"
                    :class="getAvailabilityClass(seance)"
                  ></div>
                </div>
                <div class="availability-text">
                  {{ seance.placesDisponibles }}/{{ seance.salle?.capacite }} places
                </div>
              </div>
            </div>
          </div>

          <!-- Footer avec bouton -->
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
    </div>
  </div>
</template>

<script>
import { seanceService } from '../services/api'

export default {
  name: 'Seances',
  data() {
    return {
      seances: [],
      loading: true,
      sortBy: 'heure', // 'heure', 'prix', 'places'
      jourActif: null
    }
  },
  computed: {
    joursDisponibles() {
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
    },

    sortedSeances() {
      // Pour compatibilité, retourner toutes les séances triées
      const allSeances = this.seances.slice()
      allSeances.sort((a, b) => {
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
      return allSeances
    }
  },
  async mounted() {
    await this.loadSeances()
    // Initialiser avec le premier jour disponible
    if (this.joursDisponibles.length > 0) {
      this.jourActif = this.joursDisponibles[0].date
    }
  },
  methods: {
    async loadSeances() {
      this.loading = true
      try {
        const response = await seanceService.rechercherSeances()
        this.seances = response.data
      } catch (error) {
        console.error('Erreur lors du chargement des séances:', error)
      } finally {
        this.loading = false
      }
    },

    setSortBy(criteria) {
      this.sortBy = criteria
    },

    reserverSeance(seanceId) {
      this.$router.push(`/reservation/${seanceId}`)
    },

    formatDate(dateTimeString) {
      const date = new Date(dateTimeString)
      return date.toLocaleDateString('fr-FR', {
        weekday: 'short',
        month: 'short',
        day: 'numeric'
      })
    },

    formatDateLong(dateTimeString) {
      const date = new Date(dateTimeString)
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

    formatDateTab(dateTimeString) {
      const date = new Date(dateTimeString)
      const today = new Date()
      const tomorrow = new Date(today)
      tomorrow.setDate(today.getDate() + 1)

      if (date.toDateString() === today.toDateString()) {
        return 'Aujourd\'hui'
      } else if (date.toDateString() === tomorrow.toDateString()) {
        return 'Demain'
      } else {
        return date.toLocaleDateString('fr-FR', {
          weekday: 'long',
          day: 'numeric',
          month: 'short'
        })
      }
    }
  }
}
</script>

<style scoped>
/* Page header */
.page-header {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 0 0 20px 20px;
  padding: var(--space-6) 0 var(--space-8);
  margin-bottom: var(--space-8);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 var(--space-4);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-6);
}

.page-title {
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
  line-height: 1.2;
}

.page-subtitle {
  font-size: var(--font-size-lg);
  color: var(--text-secondary);
  margin: 0;
}

/* Header actions */
.header-actions {
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
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.filter-btn.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* États de chargement et vide */
.loading-state,
.no-seances {
  text-align: center;
  padding: var(--space-12) var(--space-4);
}

.loading-content,
.no-seances-content {
  max-width: 500px;
  margin: 0 auto;
}

.loading-spinner {
  margin-bottom: var(--space-4);
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

/* Section des séances avec onglets */
.seances-section {
  padding: 0 var(--space-4);
}

/* Onglets */
.tabs-container {
  margin-bottom: var(--space-6);
  background: white;
  border-radius: 16px;
  padding: var(--space-4);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tabs-nav {
  display: flex;
  gap: 0;
  border-radius: 12px;
  overflow: hidden;
  background: #f8f9fa;
  padding: 4px;
}

.tab-button {
  flex: 1;
  padding: var(--space-3) var(--space-4);
  border: none;
  background: transparent;
  color: var(--text-secondary);
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
  min-height: 60px;
  justify-content: center;
}

.tab-button:hover {
  background: rgba(255, 193, 7, 0.1);
  color: #d97706;
}

.tab-button.active {
  background: #ffc107;
  color: #2d3748;
  box-shadow: 0 2px 8px rgba(255, 193, 7, 0.3);
  font-weight: 600;
}

.tab-button.active .tab-day {
  color: #2d3748;
}

.tab-day {
  font-size: var(--font-size-base);
  font-weight: 600;
  line-height: 1.2;
}

.tab-count {
  font-size: var(--font-size-sm);
  opacity: 0.7;
  font-weight: 500;
}

.tabs-content {
  margin-top: var(--space-6);
  min-height: 400px;
}

.tab-pane {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Grille des séances */
.seances-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: var(--space-5);
  max-width: 1400px;
  margin: 0 auto;
}

/* Cartes de séances - Design épuré */
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
  border-color: var(--primary-color);
}

/* Header de la carte */
.card-header {
  background: white;
  color: #2d3748;
  padding: var(--space-4);
  border: none;
  border-bottom: 1px solid #e2e8f0;
}

.card-header.almost-expired {
  background: linear-gradient(135deg, #fff3cd, #ffeaa7);
  border-bottom: 2px solid #ffc107;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.film-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  margin: 0 0 var(--space-2) 0;
  line-height: 1.3;
}

.film-genre {
  font-size: var(--font-size-sm);
  opacity: 0.9;
  font-weight: 400;
}

/* Corps de la carte */
.card-body {
  padding: var(--space-4);
}

.seance-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.info-item.price {
  font-weight: 600;
  color: var(--success-color);
  font-size: var(--font-size-base);
}

/* Barre de disponibilité */
.availability-info {
  margin-top: var(--space-3);
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
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.card-footer .btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 1200px) {
  .seances-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: var(--space-4);
  }
}

@media (max-width: 992px) {
  .header-content {
    flex-direction: column;
    text-align: center;
    gap: var(--space-4);
  }

  .page-title {
    font-size: var(--font-size-3xl);
  }

  .header-actions {
    justify-content: center;
    flex-wrap: wrap;
  }

  .filter-buttons {
    justify-content: center;
    flex-wrap: wrap;
  }

  .seances-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: var(--space-4);
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: var(--space-4) 0 var(--space-6);
    margin-bottom: var(--space-6);
  }

  .page-title {
    font-size: var(--font-size-2xl);
  }

  .filter-group {
    flex-direction: column;
    gap: var(--space-2);
  }

  .filter-buttons {
    justify-content: center;
  }

  .seances-grid {
    grid-template-columns: 1fr;
    gap: var(--space-3);
    padding: 0 var(--space-2);
  }

  .seance-card {
    margin: 0 var(--space-2);
  }

  .info-row {
    flex-direction: column;
    gap: var(--space-2);
    align-items: flex-start;
  }

  .card-header,
  .card-body,
  .card-footer {
    padding: var(--space-3);
  }
}

@media (max-width: 576px) {
  .page-title {
    font-size: var(--font-size-xl);
  }

  .seance-card {
    border-radius: 8px;
  }

  .film-title {
    font-size: var(--font-size-base);
  }
}
</style>
