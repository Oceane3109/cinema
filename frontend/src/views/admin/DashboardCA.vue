<template>
  <div class="dashboard-ca">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="fw-bold">
        <i class="bi bi-graph-up me-2 text-primary"></i>
        Tableau de Bord Chiffre d'Affaires
      </h3>
      <div class="d-flex gap-2">
        <button @click="refreshData" class="btn btn-success" :disabled="loading">
          <i class="bi bi-arrow-clockwise me-2"></i>
          Actualiser
        </button>
      </div>
    </div>

    <!-- Statistiques principales -->
    <div class="row mb-4">
      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-grow-1">
                <div class="text-uppercase text-primary fw-bold small mb-1">
                  CA Total
                </div>
                <div class="h4 mb-0 fw-bold text-gray-800">
                  {{ formatPrix(statsGenerales.totalChiffreAffaire) }}€
                </div>
              </div>
              <div class="ms-3">
                <div class="icon-box bg-primary bg-opacity-10 text-primary rounded-3 p-3">
                  <i class="bi bi-cash-stack fs-4"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-grow-1">
                <div class="text-uppercase text-success fw-bold small mb-1">
                  Entrées totales
                </div>
                <div class="h4 mb-0 fw-bold text-gray-800">
                  {{ statsGenerales.totalEntrees.toLocaleString() }}
                </div>
              </div>
              <div class="ms-3">
                <div class="icon-box bg-success bg-opacity-10 text-success rounded-3 p-3">
                  <i class="bi bi-people fs-4"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-grow-1">
                <div class="text-uppercase text-info fw-bold small mb-1">
                  Tarif moyen
                </div>
                <div class="h4 mb-0 fw-bold text-gray-800">
                  {{ formatPrix(statsGenerales.tarifMoyen) }}€
                </div>
              </div>
              <div class="ms-3">
                <div class="icon-box bg-info bg-opacity-10 text-info rounded-3 p-3">
                  <i class="bi bi-graph-up-arrow fs-4"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-body">
            <div class="d-flex align-items-center">
              <div class="flex-grow-1">
                <div class="text-uppercase text-warning fw-bold small mb-1">
                  Films concernés
                </div>
                <div class="h4 mb-0 fw-bold text-gray-800">
                  {{ statsGenerales.nombreFilms }}
                </div>
              </div>
              <div class="ms-3">
                <div class="icon-box bg-warning bg-opacity-10 text-warning rounded-3 p-3">
                  <i class="bi bi-film fs-4"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Chiffre d'affaire par film -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-light border-bottom">
            <h6 class="mb-0 fw-semibold">
              <i class="bi bi-film me-2 text-primary"></i>
              Chiffre d'Affaires par Film
            </h6>
          </div>
          <div class="card-body">
            <div v-if="loading" class="text-center py-4">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Chargement...</span>
              </div>
            </div>
            
            <div v-else-if="caParFilm.length === 0" class="text-center py-4 text-muted">
              <i class="bi bi-inbox fs-1 d-block mb-3"></i>
              <p class="mb-0">Aucune donnée de chiffre d'affaires par film trouvée</p>
            </div>

            <div v-else class="table-responsive">
              <table class="table table-hover align-middle">
                <thead class="table-light">
                  <tr>
                    <th @click="sortByFilm('filmTitre')" style="cursor: pointer;" class="fw-medium">
                      Film
                      <i v-if="sortFilmField === 'filmTitre'" :class="sortFilmOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                    </th>
                    <th @click="sortByFilm('totalChiffreAffaire')" style="cursor: pointer;" class="fw-medium">
                      Chiffre d'Affaires
                      <i v-if="sortFilmField === 'totalChiffreAffaire'" :class="sortFilmOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                    </th>
                    <th @click="sortByFilm('totalEntrees')" style="cursor: pointer;" class="fw-medium">
                      Entrées
                      <i v-if="sortFilmField === 'totalEntrees'" :class="sortFilmOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                    </th>
                    <th @click="sortByFilm('tarifMoyen')" style="cursor: pointer;" class="fw-medium">
                      Tarif Moyen
                      <i v-if="sortFilmField === 'tarifMoyen'" :class="sortFilmOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="film in sortedCaParFilm" :key="film.filmId" class="border-bottom">
                    <td>
                      <div class="fw-semibold">{{ film.filmTitre }}</div>
                    </td>
                    <td>
                      <span class="badge bg-success fs-6">{{ formatPrix(film.totalChiffreAffaire) }}€</span>
                    </td>
                    <td>
                      <span class="badge bg-info">{{ film.totalEntrees.toLocaleString() }}</span>
                    </td>
                    <td>
                      <span class="badge bg-warning">{{ formatPrix(film.tarifMoyen) }}€</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Revenu maximum par séance -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-light border-bottom">
            <h6 class="mb-0 fw-semibold">
              <i class="bi bi-cash-stack me-2 text-primary"></i>
              Revenu Maximum par Séance
            </h6>
          </div>
          <div class="card-body">
            <div v-if="loadingTypes" class="text-center py-4">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Chargement...</span>
              </div>
            </div>
            
            <div v-else-if="caMaxParSeance.length === 0" class="text-center py-4 text-muted">
              <i class="bi bi-inbox fs-1 d-block mb-3"></i>
              <p class="mb-0">Aucune donnée de revenu par séance trouvée</p>
            </div>

            <div v-else>
              <!-- Tableau des revenus par séance -->
              <div class="table-responsive">
                <table class="table table-hover align-middle">
                  <thead class="table-light">
                    <tr>
                      <th class="fw-medium">Salle</th>
                      <th class="fw-medium">Film</th>
                      <th class="fw-medium">Date/Heure</th>
                      <th class="fw-medium">Total Places</th>
                      <th class="fw-medium">Revenu Maximum</th>
                      <th class="fw-medium">Détails par catégorie</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="seance in caMaxParSeance" :key="seance.seanceId" class="border-bottom">
                      <td>
                        <div class="fw-semibold">{{ seance.salleNom }}</div>
                      </td>
                      <td>
                        <div class="fw-semibold">{{ seance.filmTitre }}</div>
                      </td>
                      <td>
                        <small class="text-muted">{{ formatDateTime(seance.dateHeure) }}</small>
                      </td>
                      <td>
                        <span class="fw-semibold">{{ seance.totalPlaces }}</span>
                      </td>
                      <td>
                        <span class="fw-semibold">{{ formatPrix(seance.revenuMaximum) }}€</span>
                      </td>
                      <td>
                        <div class="d-flex flex-wrap gap-1">
                          <span v-for="(count, categorie) in seance.placesParCategorie" :key="categorie" 
                                class="badge bg-secondary" style="font-size: 0.7em;">
                            {{ categorie }}: {{ count }}
                          </span>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Revenu par type de place (résumé) -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="card border-0 shadow-sm">
          <div class="card-header bg-light border-bottom">
            <h6 class="mb-0 fw-semibold">
              <i class="bi bi-ticket-perforated me-2 text-primary"></i>
              Résumé par Type de Place
            </h6>
          </div>
          <div class="card-body">
            <div v-if="loadingTypes" class="text-center py-4">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Chargement...</span>
              </div>
            </div>
            
            <div v-else-if="caMaxParType.length === 0" class="text-center py-4 text-muted">
              <i class="bi bi-inbox fs-1 d-block mb-3"></i>
              <p class="mb-0">Aucune donnée de chiffre d'affaires par type de place trouvée</p>
            </div>

            <div v-else>
              <div class="row">
                <div v-for="type in caMaxParType" :key="type.typePlace" class="col-md-6 col-lg-4 mb-3">
                  <div class="card border-0 shadow-sm h-100">
                    <div class="card-body">
                      <div class="d-flex align-items-center mb-3">
                        <div class="flex-grow-1">
                          <h6 class="fw-semibold mb-1">{{ type.typePlace }}</h6>
                          <small class="text-muted">{{ type.nombrePlaces }} places disponibles</small>
                        </div>
                        <div class="ms-3">
                          <div class="icon-box rounded-3 p-3" :class="getTypePlaceIconClass(type.typePlace)">
                            <i :class="getTypePlaceIcon(type.typePlace)" class="fs-4"></i>
                          </div>
                        </div>
                      </div>
                      <div class="row text-center">
                        <div class="col-6">
                          <div class="fw-bold text-success">{{ formatPrix(type.caMaximum) }}€</div>
                          <small class="text-muted">CA Maximum</small>
                        </div>
                        <div class="col-6">
                          <div class="fw-bold text-info">{{ formatPrix(type.caActuel) }}€</div>
                          <small class="text-muted">CA Actuel</small>
                        </div>
                      </div>
                      <div class="progress mt-3" style="height: 8px;">
                        <div 
                          class="progress-bar bg-success" 
                          :style="{ width: Math.min((type.caActuel / type.caMaximum) * 100, 100) + '%' }"
                        ></div>
                      </div>
                      <small class="text-muted">
                        {{ Math.round((type.caActuel / type.caMaximum) * 100) }}% du potentiel réalisé
                      </small>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'

export default {
  name: 'DashboardCA',
  setup() {
    const chiffreAffaires = ref([])
    const caParFilm = ref([])
    const caMaxParSeance = ref([]) // Renommé pour plus de clarté
    const caMaxParType = ref([]) // Gardé pour le résumé par type
    const loading = ref(false)
    const loadingTypes = ref(false)

    // Tri pour les films
    const sortFilmField = ref('totalChiffreAffaire')
    const sortFilmOrder = ref('desc')

    const statsGenerales = computed(() => {
      if (!chiffreAffaires.value || !Array.isArray(chiffreAffaires.value) || chiffreAffaires.value.length === 0) {
        return {
          totalChiffreAffaire: 0,
          totalEntrees: 0,
          tarifMoyen: 0,
          nombreFilms: 0
        }
      }
      
      const totalCA = chiffreAffaires.value.reduce((sum, ca) => sum + (ca.chiffreAffaire || 0), 0)
      const totalEntrees = chiffreAffaires.value.reduce((sum, ca) => sum + (ca.nombreEntrees || 0), 0)
      const filmsUniques = new Set(chiffreAffaires.value.map(ca => ca.filmId).filter(id => id))
      
      return {
        totalChiffreAffaire: totalCA,
        totalEntrees,
        tarifMoyen: totalEntrees > 0 ? totalCA / totalEntrees : 0,
        nombreFilms: filmsUniques.size
      }
    })

    const sortedCaParFilm = computed(() => {
      if (!caParFilm.value || !Array.isArray(caParFilm.value)) {
        return []
      }
      
      return [...caParFilm.value].sort((a, b) => {
        let aVal = a[sortFilmField.value]
        let bVal = b[sortFilmField.value]
        
        if (typeof aVal === 'string') {
          aVal = aVal.toLowerCase()
          bVal = bVal.toLowerCase()
        }
        
        if (sortFilmOrder.value === 'asc') {
          return aVal > bVal ? 1 : -1
        } else {
          return aVal < bVal ? 1 : -1
        }
      })
    })

    const loadChiffreAffaires = async () => {
      loading.value = true
      try {
        const response = await fetch('/api/chiffre-affaires/test-data')
        if (!response.ok) {
          console.error('Erreur HTTP:', response.status, response.statusText)
          chiffreAffaires.value = []
          return
        }
        
        const data = await response.json()
        chiffreAffaires.value = Array.isArray(data) ? data : []
        
        // Calculer le CA par film
        calculateCaParFilm()
      } catch (error) {
        console.error('Erreur lors du chargement du chiffre d\'affaires:', error)
        chiffreAffaires.value = []
      } finally {
        loading.value = false
      }
    }

    const calculateCaParFilm = () => {
      if (!chiffreAffaires.value || !Array.isArray(chiffreAffaires.value)) {
        caParFilm.value = []
        return
      }

      const filmMap = new Map()
      
      chiffreAffaires.value.forEach(ca => {
        if (!ca.filmId || !ca.filmTitre) return
        
        if (!filmMap.has(ca.filmId)) {
          filmMap.set(ca.filmId, {
            filmId: ca.filmId,
            filmTitre: ca.filmTitre,
            totalChiffreAffaire: 0,
            totalEntrees: 0
          })
        }
        
        const film = filmMap.get(ca.filmId)
        film.totalChiffreAffaire += ca.chiffreAffaire || 0
        film.totalEntrees += ca.nombreEntrees || 0
      })
      
      // Calculer le tarif moyen pour chaque film
      caParFilm.value = Array.from(filmMap.values()).map(film => ({
        ...film,
        tarifMoyen: film.totalEntrees > 0 ? film.totalChiffreAffaire / film.totalEntrees : 0
      }))
    }

    const loadCaMaxParType = async () => {
      loadingTypes.value = true
      try {
        // Utiliser l'endpoint API pour récupérer les revenus max par séance
        const response = await fetch('/api/chiffre-affaires/revenu-max-par-seance')
        if (!response.ok) {
          console.error('Erreur HTTP:', response.status, response.statusText)
          caMaxParSeance.value = []
          caMaxParType.value = []
          return
        }
        
        const data = await response.json()
        console.log('Données revenu max par séance:', data)
        
        // Stocker les données de séances brutes
        caMaxParSeance.value = data
        
        // Grouper les données par type de place et calculer les statistiques
        const typePlaceMap = new Map()
        
        data.forEach(seance => {
          if (seance.placesParCategorie) {
            Object.entries(seance.placesParCategorie).forEach(([categorie, nombrePlaces]) => {
              if (!typePlaceMap.has(categorie)) {
                typePlaceMap.set(categorie, {
                  typePlace: categorie,
                  nombrePlaces: 0,
                  caMaximum: 0,
                  caActuel: 0
                })
              }
              
              const typeData = typePlaceMap.get(categorie)
              typeData.nombrePlaces += nombrePlaces
              
              // Calculer le CA maximum pour cette catégorie
              // Pour l'instant, on utilise une estimation basée sur les prix standards
              let prixMoyen = 10 // Prix par défaut
              if (categorie === 'PREMIUM') prixMoyen = 20
              else if (categorie === 'VIP') prixMoyen = 30
              else if (categorie === 'STANDARD') prixMoyen = 12
              
              typeData.caMaximum += nombrePlaces * prixMoyen
            })
          }
        })
        
        // Calculer le CA actuel (pour l'instant, on utilise 70% du maximum comme exemple)
        caMaxParType.value = Array.from(typePlaceMap.values()).map(type => ({
          ...type,
          caActuel: type.caMaximum * 0.7 // Simulation: 70% du potentiel réalisé
        }))
        
        console.log('CA max par type calculé:', caMaxParType.value)
      } catch (error) {
        console.error('Erreur lors du chargement du CA max par type:', error)
        caMaxParSeance.value = []
        caMaxParType.value = []
      } finally {
        loadingTypes.value = false
      }
    }

    const sortByFilm = (field) => {
      if (sortFilmField.value === field) {
        sortFilmOrder.value = sortFilmOrder.value === 'asc' ? 'desc' : 'asc'
      } else {
        sortFilmField.value = field
        sortFilmOrder.value = 'desc'
      }
    }

    const refreshData = async () => {
      await Promise.all([
        loadChiffreAffaires(),
        loadCaMaxParType()
      ])
    }

    const formatPrix = (prix) => {
      return new Intl.NumberFormat('fr-FR', { minimumFractionDigits: 2 }).format(prix)
    }

    const formatDateTime = (dateTime) => {
      if (!dateTime) return ''
      return new Date(dateTime).toLocaleString('fr-FR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const getTypePlaceIcon = (type) => {
      const icons = {
        'Standard': 'bi bi-ticket-perforated',
        'VIP': 'bi bi-gem',
        'Enfant': 'bi bi-balloon',
        'Senior': 'bi bi-person-check'
      }
      return icons[type] || 'bi bi-ticket-perforated'
    }

    const getTypePlaceIconClass = (type) => {
      const classes = {
        'Standard': 'bg-primary bg-opacity-10 text-primary',
        'VIP': 'bg-warning bg-opacity-10 text-warning',
        'Enfant': 'bg-info bg-opacity-10 text-info',
        'Senior': 'bg-success bg-opacity-10 text-success'
      }
      return classes[type] || 'bg-secondary bg-opacity-10 text-secondary'
    }

    onMounted(() => {
      refreshData()
    })

    return {
      chiffreAffaires,
      caParFilm,
      caMaxParSeance,
      caMaxParType,
      loading,
      loadingTypes,
      sortFilmField,
      sortFilmOrder,
      statsGenerales,
      sortedCaParFilm,
      loadChiffreAffaires,
      loadCaMaxParType,
      sortByFilm,
      refreshData,
      formatPrix,
      formatDateTime,
      getTypePlaceIcon,
      getTypePlaceIconClass
    }
  }
}
</script>

<style scoped>
.dashboard-ca {
  padding: 20px;
  background-color: var(--bg-secondary);
}

/* Cartes de statistiques */
.icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: var(--radius-lg);
  font-size: 1.5rem;
  transition: all 0.15s ease-in-out;
}

.icon-box:hover {
  transform: scale(1.05);
}

/* Tableau */
.table {
  background-color: var(--bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.table th {
  border-top: none;
  font-weight: 600;
  color: var(--text-primary);
  background-color: var(--bg-tertiary);
  padding: 1rem;
}

.table td {
  padding: 0.875rem 1rem;
  vertical-align: middle;
  border-color: var(--border-color);
}

.table tbody tr:hover {
  background-color: var(--bg-secondary);
}

/* Cards */
.card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  background-color: var(--bg-primary);
}

.card-header {
  background-color: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-color);
  padding: 1rem 1.25rem;
}

.card-body {
  padding: 1.25rem;
}

/* Badge */
.badge {
  font-size: 0.75em;
  padding: 0.375rem 0.75rem;
  border-radius: var(--radius-full);
  font-weight: 500;
}

/* Progress bar */
.progress {
  background-color: var(--bg-tertiary);
  border-radius: var(--radius-full);
}

.progress-bar {
  transition: width 0.3s ease;
}

/* Text utilities */
.text-gray-800 {
  color: var(--text-primary) !important;
}

.text-primary {
  color: var(--primary-color) !important;
}

.text-success {
  color: var(--success-color) !important;
}

.text-info {
  color: var(--info-color) !important;
}

.text-warning {
  color: var(--warning-color) !important;
}

.text-muted {
  color: var(--text-muted) !important;
}

/* Background utilities */
.bg-primary {
  background-color: var(--primary-color) !important;
}

.bg-success {
  background-color: var(--success-color) !important;
}

.bg-info {
  background-color: var(--info-color) !important;
}

.bg-warning {
  background-color: var(--warning-color) !important;
}

.bg-secondary {
  background-color: var(--secondary-color) !important;
}

.bg-opacity-10 {
  opacity: 0.1 !important;
}

/* Layout utilities */
.d-flex {
  display: flex !important;
}

.align-items-center {
  align-items: center !important;
}

.justify-content-between {
  justify-content: space-between !important;
}

.flex-grow-1 {
  flex-grow: 1 !important;
}

.ms-3 {
  margin-left: 1rem !important;
}

.me-2 {
  margin-right: 0.5rem !important;
}

.mb-4 {
  margin-bottom: 1.5rem !important;
}

.mb-3 {
  margin-bottom: 1rem !important;
}

.mb-1 {
  margin-bottom: 0.25rem !important;
}

.mt-3 {
  margin-top: 1rem !important;
}

/* Spinner */
.spinner-border {
  border-color: var(--primary-color) transparent var(--primary-color) transparent;
}

/* Button utilities */
.btn {
  border-radius: var(--radius-md);
  padding: 0.75rem 1.5rem;
  font-weight: 500;
  transition: all 0.15s ease-in-out;
}

.btn-success {
  background-color: var(--success-color);
  border-color: var(--success-color);
  color: white;
}

.btn-success:hover {
  background-color: var(--success-dark);
  border-color: var(--success-dark);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>