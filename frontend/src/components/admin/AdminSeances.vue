<template>
  <div class="admin-seances">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3>Gestion des Séances</h3>
      <div class="d-flex gap-2">
        <button @click="toggleFilters" class="btn btn-outline-secondary">
          <i class="bi bi-funnel me-1"></i>
          Filtres avancés
          <span v-if="hasActiveFilters" class="badge bg-primary ms-1">{{ activeFiltersCount }}</span>
        </button>
        <button @click="resetFilters" class="btn btn-outline-warning" v-if="hasActiveFilters">
          <i class="bi bi-x-circle me-1"></i>
          Réinitialiser
        </button>
        <button @click="showCreateModal" class="btn btn-primary">
          <i class="bi bi-plus-circle me-2"></i>Ajouter une séance
        </button>
      </div>
    </div>

    <!-- Filtres avancés -->
    <div v-show="showFilters" class="card mb-4">
      <div class="card-header">
        <h6 class="mb-0">
          <i class="bi bi-funnel me-2"></i>
          Filtres et tris avancés
        </h6>
      </div>
      <div class="card-body">
        <div class="row g-3">
          <!-- Recherche textuelle -->
          <div class="col-md-6">
            <label class="form-label">
              <i class="bi bi-search me-1"></i>
              Recherche
            </label>
            <input
              v-model="searchText"
              @input="applyFilters"
              type="text"
              class="form-control"
              placeholder="Film, salle..."
            >
          </div>

          <!-- Filtre par statut -->
          <div class="col-md-3">
            <label class="form-label">
              <i class="bi bi-flag me-1"></i>
              Statut
            </label>
            <select v-model="statusFilter" @change="applyFilters" class="form-select">
              <option value="">Tous statuts</option>
              <option value="ACTIVE">Active</option>
              <option value="ANNULLEE">Annulée</option>
              <option value="TERMINEE">Terminée</option>
            </select>
          </div>

          <!-- Filtre par prix -->
          <div class="col-md-3">
            <label class="form-label">
              <i class="bi bi-cash me-1"></i>
              Prix (€)
            </label>
            <div class="input-group">
              <input
                v-model.number="prixMin"
                @input="applyFilters"
                type="number"
                class="form-control"
                placeholder="Min"
                min="0"
                step="0.01"
              >
              <span class="input-group-text">-</span>
              <input
                v-model.number="prixMax"
                @input="applyFilters"
                type="number"
                class="form-control"
                placeholder="Max"
                min="0"
                step="0.01"
              >
            </div>
          </div>

          <!-- Filtre par film -->
          <div class="col-md-4">
            <label class="form-label">
              <i class="bi bi-film me-1"></i>
              Film
            </label>
            <select v-model="filmFilter" @change="applyFilters" class="form-select">
              <option value="">Tous les films</option>
              <option v-for="film in availableFilms" :key="film.id" :value="film.id">
                {{ film.titre }}
              </option>
            </select>
          </div>

          <!-- Filtre par salle -->
          <div class="col-md-4">
            <label class="form-label">
              <i class="bi bi-geo-alt me-1"></i>
              Salle
            </label>
            <select v-model="salleFilter" @change="applyFilters" class="form-select">
              <option value="">Toutes les salles</option>
              <option v-for="salle in availableSalles" :key="salle.id" :value="salle.id">
                {{ salle.nom }}
              </option>
            </select>
          </div>

          <!-- Filtre par période -->
          <div class="col-md-4">
            <label class="form-label">
              <i class="bi bi-calendar-range me-1"></i>
              Date
            </label>
            <input
              v-model="dateFilter"
              @change="applyFilters"
              type="date"
              class="form-control"
            >
          </div>

          <!-- Tri -->
          <div class="col-md-6">
            <label class="form-label">
              <i class="bi bi-sort me-1"></i>
              Trier par
            </label>
            <div class="row">
              <div class="col-8">
                <select v-model="sortBy" @change="applyFilters" class="form-select">
                  <option value="dateHeure">Date/heure</option>
                  <option value="prix">Prix</option>
                  <option value="placesDisponibles">Places disponibles</option>
                  <option value="film.titre">Film</option>
                  <option value="salle.nom">Salle</option>
                  <option value="id">ID</option>
                </select>
              </div>
              <div class="col-4">
                <div class="btn-group w-100">
                  <input
                    v-model="sortOrder"
                    @change="applyFilters"
                    type="radio"
                    class="btn-check"
                    name="sortOrderSeances"
                    id="ascSeances"
                    value="asc"
                  >
                  <label class="btn btn-outline-primary btn-sm" for="ascSeances">
                    <i class="bi bi-arrow-up me-1"></i>
                  </label>

                  <input
                    v-model="sortOrder"
                    @change="applyFilters"
                    type="radio"
                    class="btn-check"
                    name="sortOrderSeances"
                    id="descSeances"
                    value="desc"
                  >
                  <label class="btn btn-outline-primary btn-sm" for="descSeances">
                    <i class="bi bi-arrow-down me-1"></i>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Résumé des filtres actifs -->
        <div v-if="hasActiveFilters" class="mt-3 p-2 bg-light rounded">
          <small class="text-muted">
            <i class="bi bi-info-circle me-1"></i>
            <strong>{{ filteredSeances.length }}</strong> séance(s) sur <strong>{{ seances.length }}</strong>
          </small>
        </div>
      </div>
    </div>

    <!-- Tableau des séances -->
    <div class="table-responsive">
      <table class="table table-striped table-hover">
        <thead class="table-dark">
          <tr>
            <th>ID</th>
            <th>Film</th>
            <th>Salle</th>
            <th>Date/Heure</th>
            <th>Prix</th>
            <th>Places</th>
            <th>Statut</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="seance in filteredSeances" :key="seance.id">
            <td>{{ seance.id }}</td>
            <td>{{ seance.film?.titre }}</td>
            <td>{{ seance.salle?.nom }}</td>
            <td>{{ formatDateTime(seance.dateHeure) }}</td>
            <td>{{ seance.prix }}€</td>
            <td>{{ seance.placesDisponibles }}/{{ seance.salle?.capacite }}</td>
            <td>
              <span :class="getStatusBadgeClass(seance.statut)">
                {{ seance.statut }}
              </span>
            </td>
            <td>
              <button @click="editSeance(seance)" class="btn btn-sm btn-outline-primary me-2">
                <i class="bi bi-pencil"></i>
              </button>
              <button @click="deleteSeance(seance.id)" class="btn btn-sm btn-outline-danger">
                <i class="bi bi-trash"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal pour créer/éditer une séance -->
    <div v-if="showModal" class="modal-backdrop fade show" @click="closeModal"></div>
    <div class="modal fade" :class="{ 'show': showModal, 'd-block': showModal }" :style="{ display: showModal ? 'block' : 'none' }" tabindex="-1">
      <div class="modal-dialog modal-lg" @click.stop>
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEditing ? 'Modifier la séance' : 'Ajouter une séance' }}
            </h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveSeance">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label">Film *</label>
                  <select
                    v-model.number="seanceForm.filmId"
                    class="form-select"
                    required
                  >
                    <option value="">Sélectionner un film</option>
                    <option
                      v-for="film in films"
                      :key="film.id"
                      :value="film.id"
                    >
                      {{ film.titre }}
                    </option>
                  </select>
                </div>
                <div class="col-md-6">
                  <label class="form-label">Salle *</label>
                  <select
                    v-model.number="seanceForm.salleId"
                    class="form-select"
                    required
                  >
                    <option value="">Sélectionner une salle</option>
                    <option
                      v-for="salle in salles"
                      :key="salle.id"
                      :value="salle.id"
                    >
                      {{ salle.nom }} ({{ salle.capacite }} places)
                    </option>
                  </select>
                </div>
                <div class="col-md-6">
                  <label class="form-label">Date *</label>
                  <input
                    type="date"
                    v-model="seanceForm.date"
                    class="form-control"
                    required
                  >
                </div>
                <div class="col-md-6">
                  <label class="form-label">Heure *</label>
                  <input
                    type="time"
                    v-model="seanceForm.heure"
                    class="form-control"
                    required
                  >
                </div>
                <div class="col-md-6">
                  <label class="form-label">Prix (€) *</label>
                  <input
                    type="number"
                    v-model.number="seanceForm.prix"
                    class="form-control"
                    min="0"
                    step="0.01"
                    required
                  >
                </div>
                <div class="col-md-6">
                  <label class="form-label">Statut</label>
                  <select v-model="seanceForm.statut" class="form-select">
                    <option value="ACTIVE">Active</option>
                    <option value="ANNULLEE">Annulée</option>
                    <option value="TERMINEE">Terminée</option>
                  </select>
                </div>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">
              Annuler
            </button>
            <button @click="saveSeance" type="button" class="btn btn-primary">
              {{ isEditing ? 'Modifier' : 'Ajouter' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { seanceService, filmService, salleService } from '../../services/api'

export default {
  name: 'AdminSeances',
  data() {
    return {
      seances: [],
      filteredSeances: [],
      films: [],
      salles: [],
      showFilters: false,

      // Filtres
      searchText: '',
      statusFilter: '',
      filmFilter: '',
      salleFilter: '',
      prixMin: null,
      prixMax: null,
      dateFilter: '',

      // Tri
      sortBy: 'dateHeure',
      sortOrder: 'desc',

      // Données pour les filtres
      availableFilms: [],
      availableSalles: [],

      // Modal
      isEditing: false,
      showModal: false,
      seanceForm: {
        filmId: '',
        salleId: '',
        date: '',
        heure: '',
        prix: null,
        statut: 'ACTIVE'
      }
    }
  },
  async mounted() {
    await this.loadData()
    this.extractFilterData()
    this.applyFilters()
  },
  methods: {
    async loadData() {
      try {
        const [seancesRes, filmsRes, sallesRes] = await Promise.all([
          seanceService.rechercherSeances(),
          filmService.getAllFilms(),
          salleService.getAllSalles()
        ])
        this.seances = seancesRes.data
        this.films = filmsRes.data
        this.salles = sallesRes.data
      } catch (error) {
        console.error('Erreur lors du chargement des données:', error)
        alert('Erreur lors du chargement des données')
      }
    },

    extractFilterData() {
      this.availableFilms = this.films
      this.availableSalles = this.salles
    },

    toggleFilters() {
      this.showFilters = !this.showFilters
    },

    resetFilters() {
      this.searchText = ''
      this.statusFilter = ''
      this.filmFilter = ''
      this.salleFilter = ''
      this.prixMin = null
      this.prixMax = null
      this.dateFilter = ''
      this.sortBy = 'dateHeure'
      this.sortOrder = 'desc'
      this.applyFilters()
    },

    applyFilters() {
      let filtered = [...this.seances]

      // Filtre textuel
      if (this.searchText.trim()) {
        const search = this.searchText.toLowerCase()
        filtered = filtered.filter(s =>
          (s.film?.titre && s.film.titre.toLowerCase().includes(search)) ||
          (s.salle?.nom && s.salle.nom.toLowerCase().includes(search))
        )
      }

      // Filtre par statut
      if (this.statusFilter) {
        filtered = filtered.filter(s => s.statut === this.statusFilter)
      }

      // Filtre par film
      if (this.filmFilter) {
        filtered = filtered.filter(s => s.film?.id === parseInt(this.filmFilter))
      }

      // Filtre par salle
      if (this.salleFilter) {
        filtered = filtered.filter(s => s.salle?.id === parseInt(this.salleFilter))
      }

      // Filtre par prix
      if (this.prixMin !== null && this.prixMin !== undefined) {
        filtered = filtered.filter(s => s.prix >= this.prixMin)
      }
      if (this.prixMax !== null && this.prixMax !== undefined) {
        filtered = filtered.filter(s => s.prix <= this.prixMax)
      }

      // Filtre par date
      if (this.dateFilter) {
        const filterDate = new Date(this.dateFilter)
        filtered = filtered.filter(s => {
          const seanceDate = new Date(s.dateHeure)
          return seanceDate.toDateString() === filterDate.toDateString()
        })
      }

      // Tri
      filtered.sort((a, b) => {
        let aValue, bValue

        switch (this.sortBy) {
          case 'dateHeure':
            aValue = new Date(a.dateHeure)
            bValue = new Date(b.dateHeure)
            break
          case 'prix':
            aValue = a.prix
            bValue = b.prix
            break
          case 'placesDisponibles':
            aValue = a.placesDisponibles
            bValue = b.placesDisponibles
            break
          case 'film.titre':
            aValue = (a.film?.titre || '').toLowerCase()
            bValue = (b.film?.titre || '').toLowerCase()
            break
          case 'salle.nom':
            aValue = (a.salle?.nom || '').toLowerCase()
            bValue = (b.salle?.nom || '').toLowerCase()
            break
          case 'id':
            aValue = a.id
            bValue = b.id
            break
          default:
            return 0
        }

        if (this.sortOrder === 'asc') {
          return aValue > bValue ? 1 : aValue < bValue ? -1 : 0
        } else {
          return aValue < bValue ? 1 : aValue > bValue ? -1 : 0
        }
      })

      this.filteredSeances = filtered
    },

    showCreateModal() {
      this.isEditing = false
      this.resetForm()
      this.showModal = true
    },

    editSeance(seance) {
      this.isEditing = true
      this.seanceForm = {
        filmId: seance.film?.id || '',
        salleId: seance.salle?.id || '',
        date: this.formatDateForInput(seance.dateHeure),
        heure: this.formatTimeForInput(seance.dateHeure),
        prix: seance.prix,
        statut: seance.statut
      }
      this.showModal = true
    },

    closeModal() {
      this.showModal = false
    },

    resetForm() {
      this.seanceForm = {
        filmId: '',
        salleId: '',
        date: '',
        heure: '',
        prix: null,
        statut: 'ACTIVE'
      }
    },

    formatDateTime(dateTimeString) {
      const date = new Date(dateTimeString)
      return date.toLocaleDateString('fr-FR') + ' ' + date.toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    formatDateForInput(dateTimeString) {
      const date = new Date(dateTimeString)
      return date.toISOString().split('T')[0]
    },

    formatTimeForInput(dateTimeString) {
      const date = new Date(dateTimeString)
      return date.toTimeString().slice(0, 5)
    },

    getStatusBadgeClass(statut) {
      const classes = {
        'ACTIVE': 'badge bg-success',
        'ANNULLEE': 'badge bg-danger',
        'TERMINEE': 'badge bg-secondary'
      }
      return classes[statut] || 'badge bg-secondary'
    },

    async saveSeance() {
      try {
        // Convertir les données du formulaire en format API
        const seanceData = {
          film: { id: parseInt(this.seanceForm.filmId) },
          salle: { id: parseInt(this.seanceForm.salleId) },
          dateHeure: `${this.seanceForm.date}T${this.seanceForm.heure}:00`,
          prix: parseFloat(this.seanceForm.prix),
          statut: this.seanceForm.statut
        }

        if (this.isEditing) {
          await seanceService.updateSeance(this.seanceForm.id, seanceData)
          alert('Séance modifiée avec succès !')
        } else {
          await seanceService.createSeance(seanceData)
          alert('Séance créée avec succès !')
        }

        // Fermer le modal
        this.closeModal()

        // Recharger les données
        await this.loadData()
      } catch (error) {
        console.error('Erreur lors de la sauvegarde:', error)
        const message = error.response?.data?.message || error.response?.data || error.message || 'Erreur lors de la sauvegarde'
        alert('Erreur: ' + message)
      }
    },

    async deleteSeance(id) {
      if (confirm('Êtes-vous sûr de vouloir supprimer cette séance ?')) {
        try {
          await seanceService.deleteSeance(id)
          alert('Séance supprimée avec succès !')
          await this.loadData()
        } catch (error) {
          console.error('Erreur lors de la suppression:', error)
          const message = error.response?.data?.message || error.response?.data || error.message || 'Erreur lors de la suppression'
          alert('Erreur: ' + message)
        }
      }
    }
  },
  computed: {
    hasActiveFilters() {
      return this.searchText.trim() ||
             this.statusFilter ||
             this.filmFilter ||
             this.salleFilter ||
             (this.prixMin !== null && this.prixMin !== undefined) ||
             (this.prixMax !== null && this.prixMax !== undefined) ||
             this.dateFilter ||
             this.sortBy !== 'dateHeure' ||
             this.sortOrder !== 'desc'
    },

    activeFiltersCount() {
      let count = 0
      if (this.searchText.trim()) count++
      if (this.statusFilter) count++
      if (this.filmFilter) count++
      if (this.salleFilter) count++
      if (this.prixMin !== null && this.prixMin !== undefined) count++
      if (this.prixMax !== null && this.prixMax !== undefined) count++
      if (this.dateFilter) count++
      if (this.sortBy !== 'dateHeure') count++
      if (this.sortOrder !== 'desc') count++
      return count
    }
  }
}
</script>

<style scoped>
@import '../../assets/admin-seances.css';

/* Styles pour les filtres avancés */
.card-header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
}

.form-label {
  font-weight: 600;
  color: #495057;
  margin-bottom: 0.5rem;
}

.form-select, .form-control {
  border-radius: 0.375rem;
}

.input-group-text {
  background-color: #e9ecef;
  border-color: #ced4da;
}

.btn-group .btn {
  border-radius: 0.375rem !important;
}

.btn-outline-secondary:hover {
  background-color: #6c757d;
  border-color: #6c757d;
}

/* Indicateur de filtres actifs */
.badge {
  font-size: 0.7rem;
  padding: 0.25rem 0.5rem;
}
</style>
