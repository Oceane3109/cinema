<template>
  <div class="admin-salles">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3>Gestion des Salles</h3>
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
          <i class="bi bi-plus-circle me-2"></i>Ajouter une salle
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
              placeholder="Nom, description..."
            >
          </div>

          <!-- Filtre par type -->
          <div class="col-md-3">
            <label class="form-label">
              <i class="bi bi-tag me-1"></i>
              Type
            </label>
            <select v-model="typeFilter" @change="applyFilters" class="form-select">
              <option value="">Tous les types</option>
              <option value="STANDARD">Standard</option>
              <option value="PREMIUM">Premium</option>
              <option value="VIP">VIP</option>
              <option value="FAMILIAL">Familial</option>
              <option value="3D">3D</option>
            </select>
          </div>

          <!-- Filtre par capacité -->
          <div class="col-md-3">
            <label class="form-label">
              <i class="bi bi-people me-1"></i>
              Capacité
            </label>
            <select v-model="capaciteFilter" @change="applyFilters" class="form-select">
              <option value="">Toutes capacités</option>
              <option value="small">≤ 100 places</option>
              <option value="medium">101-150 places</option>
              <option value="large">≥ 151 places</option>
            </select>
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
                  <option value="nom">Nom</option>
                  <option value="capacite">Capacité</option>
                  <option value="type">Type</option>
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
                    name="sortOrderSalles"
                    id="ascSalles"
                    value="asc"
                  >
                  <label class="btn btn-outline-primary btn-sm" for="ascSalles">
                    <i class="bi bi-arrow-up me-1"></i>
                  </label>

                  <input
                    v-model="sortOrder"
                    @change="applyFilters"
                    type="radio"
                    class="btn-check"
                    name="sortOrderSalles"
                    id="descSalles"
                    value="desc"
                  >
                  <label class="btn btn-outline-primary btn-sm" for="descSalles">
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
            <strong>{{ filteredSalles.length }}</strong> salle(s) sur <strong>{{ salles.length }}</strong>
          </small>
        </div>
      </div>
    </div>

    <!-- Tableau des salles -->
    <div class="table-responsive">
      <table class="table table-striped table-hover">
        <thead class="table-dark">
          <tr>
            <th>ID</th>
            <th>Nom</th>
            <th>Capacité</th>
            <th>Type</th>
            <th>Répartition des places</th>
            <th>Statut</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="salle in filteredSalles" :key="salle.id">
            <td>{{ salle.id }}</td>
            <td>{{ salle.nom }}</td>
            <td>{{ salle.capacite }}</td>
            <td>
              <span :class="getTypeBadgeClass(salle.type)">
                {{ salle.type }}
              </span>
            </td>
            <td>
              <div class="d-flex flex-wrap gap-1">
                <span v-for="(count, categorie) in getRepartitionPlaces(salle)" :key="categorie" 
                      class="badge" :class="getCategorieBadgeClass(categorie)">
                  {{ categorie }}: {{ count }}
                </span>
              </div>
            </td>
            <td>
              <span class="badge" :class="getStatutClass(salle)">
                {{ getStatutText(salle) }}
              </span>
            </td>
            <td>
              <button @click="editSalle(salle)" class="btn btn-sm btn-outline-primary me-2">
                <i class="bi bi-pencil"></i>
              </button>
              <button @click="configurerPlaces(salle)" class="btn btn-sm btn-outline-success me-2">
                <i class="bi bi-gear"></i>
              </button>
              <button @click="deleteSalle(salle.id)" class="btn btn-sm btn-outline-danger">
                <i class="bi bi-trash"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal pour créer/éditer une salle -->
    <div v-if="showModal" class="modal-backdrop fade show" @click="closeModal"></div>
    <div class="modal fade" :class="{ 'show': showModal, 'd-block': showModal }" :style="{ display: showModal ? 'block' : 'none' }" tabindex="-1">
      <div class="modal-dialog" @click.stop>
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEditing ? 'Modifier la salle' : 'Ajouter une salle' }}
            </h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveSalle">
              <div class="mb-3">
                <label class="form-label">Nom *</label>
                <input
                  type="text"
                  v-model="salleForm.nom"
                  class="form-control"
                  required
                >
              </div>
              <div class="mb-3">
                <label class="form-label">Capacité *</label>
                <input
                  type="number"
                  v-model.number="salleForm.capacite"
                  class="form-control"
                  min="1"
                  required
                >
              </div>
              <div class="mb-3">
                <label class="form-label">Type</label>
                <select v-model="salleForm.type" class="form-select">
                  <option value="STANDARD">Standard</option>
                  <option value="PREMIUM">Premium</option>
                  <option value="VIP">VIP</option>
                  <option value="FAMILIAL">Familial</option>
                  <option value="3D">3D</option>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label">Description</label>
                <textarea
                  v-model="salleForm.description"
                  class="form-control"
                  rows="3"
                ></textarea>
              </div>

              <!-- Configuration de la répartition des places -->
              <div class="mb-4">
                <h6 class="fw-semibold mb-3">
                  <i class="bi bi-grid-3x3-gap me-2"></i>
                  Répartition des places
                </h6>
                <div class="row g-3">
                  <div class="col-md-4">
                    <label class="form-label">
                      Places VIP
                    </label>
                    <input v-model="vipCount" type="number" class="form-control" min="0" 
                           @input="updateRepartition">
                    <small class="text-muted">{{ vipCount || 0 }} places</small>
                  </div>
                  <div class="col-md-4">
                    <label class="form-label">
                      Places Premium
                    </label>
                    <input v-model="premiumCount" type="number" class="form-control" min="0" 
                           @input="updateRepartition">
                    <small class="text-muted">{{ premiumCount || 0 }} places</small>
                  </div>
                  <div class="col-md-4">
                    <label class="form-label">
                      Places Standard
                    </label>
                    <input v-model="standardCount" type="number" class="form-control" min="0" 
                           @input="updateRepartition">
                    <small class="text-muted">{{ standardCount || 0 }} places</small>
                  </div>
                </div>
                
                <div class="alert mt-3" :class="totalPlacesAlertClassCreation">
                  <i class="bi bi-info-circle me-2"></i>
                  Total: {{ totalPlacesCalculeCreation }} / {{ salleForm.capacite }} places
                  <span v-if="totalPlacesCalculeCreation !== salleForm.capacite">
                    (Différence: {{ Math.abs(totalPlacesCalculeCreation - salleForm.capacite) }})
                  </span>
                </div>

                <div class="mt-3">
                  <small class="text-muted">
                    <i class="bi bi-info-circle me-1"></i>
                    Configurez la répartition des places par catégorie. Le total doit correspondre à la capacité de la salle.
                  </small>
                </div>
              </div>

              <!-- Sélecteur de places visuel -->
              <div class="mb-4" v-if="salleForm.capacite && (vipCount > 0 || premiumCount > 0 || standardCount > 0)">
                <h6 class="fw-semibold mb-3">
                  <i class="bi bi-grid-3x3-gap me-2"></i>
                  Sélection manuelle des places
                </h6>
                <div class="alert alert-info">
                  <small>
                    <i class="bi bi-info-circle me-1"></i>
                    Choisissez une catégorie ci-dessous, puis cliquez sur les places pour les assigner. Les places non assignées seront automatiquement STANDARD.
                  </small>
                </div>
                
                <!-- Sélecteur de catégorie -->
                <div class="category-selector mb-3">
                  <label class="form-label fw-medium">Catégorie à assigner :</label>
                  <div class="btn-group" role="group">
                    <button type="button" class="btn" 
                            :class="currentCategory === 'VIP' ? 'btn-danger' : 'btn-outline-danger'"
                            @click="currentCategory = 'VIP'">
                      🔴 VIP
                    </button>
                    <button type="button" class="btn" 
                            :class="currentCategory === 'PREMIUM' ? 'btn-primary' : 'btn-outline-primary'"
                            @click="currentCategory = 'PREMIUM'">
                      🟣 Premium
                    </button>
                    <button type="button" class="btn" 
                            :class="currentCategory === 'STANDARD' ? 'btn-success' : 'btn-outline-success'"
                            @click="currentCategory = 'STANDARD'">
                      🟢 Standard
                    </button>
                  </div>
                  <small class="text-muted">
                    Catégorie actuelle : <strong>{{ currentCategory }}</strong>
                  </small>
                </div>
                
                <div class="seat-selector-container">
                  <!-- Écran miniature -->
                  <div class="mini-cinema-screen">
                    <div class="mini-screen-rectangle">
                      <span class="mini-screen-text">ÉCRAN</span>
                    </div>
                  </div>
                  
                  <!-- Grille de places -->
                  <div class="mini-seats-container">
                    <div v-for="row in salleRows" :key="row" class="mini-row">
                      <div class="mini-row-label">{{ row }}</div>
                      <div
                        v-for="seat in rowSeats"
                        :key="seat"
                        class="mini-seat"
                        :class="getMiniSeatClass(row, seat)"
                        @click="toggleMiniSeat(row, seat)"
                      >
                        {{ seat }}
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- Mini-légende -->
                <div class="mini-legend mt-2">
                  <div class="mini-legend-item">
                    <div class="mini-seat mini-seat-vip"></div>
                    <span>VIP</span>
                  </div>
                  <div class="mini-legend-item">
                    <div class="mini-seat mini-seat-premium"></div>
                    <span>Premium</span>
                  </div>
                  <div class="mini-legend-item">
                    <div class="mini-seat mini-seat-standard"></div>
                    <span>Standard</span>
                  </div>
                </div>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">
              Annuler
            </button>
            <button @click="saveSalle" type="button" class="btn btn-primary">
              {{ isEditing ? 'Modifier' : 'Ajouter' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de configuration des places -->
    <div v-if="showConfigModal" class="modal-backdrop fade show" @click="closeConfigModal"></div>
    <div class="modal fade" :class="{ 'show': showConfigModal, 'd-block': showConfigModal }" :style="{ display: showConfigModal ? 'block' : 'none' }" tabindex="-1">
      <div class="modal-dialog modal-lg" @click.stop>
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-gear me-2"></i>
              Configurer les places - {{ selectedSalle?.nom }}
            </h5>
            <button type="button" class="btn-close" @click="closeConfigModal"></button>
          </div>
          <div class="modal-body">
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label">Capacité totale</label>
                <input v-model.number="configForm.capacite" type="number" class="form-control" min="1" max="500">
                <small class="text-muted">Nombre total de places dans la salle</small>
              </div>
              <div class="col-md-6">
                <label class="form-label">Type de salle</label>
                <select v-model="configForm.type" class="form-select">
                  <option value="STANDARD">Standard</option>
                  <option value="PREMIUM">Premium</option>
                  <option value="VIP">VIP</option>
                  <option value="3D">3D</option>
                  <option value="FAMILLE">Familiale</option>
                </select>
              </div>
            </div>

            <div class="mt-4">
              <h6 class="fw-semibold mb-3">
                <i class="bi bi-grid-3x3-gap me-2"></i>
                Répartition des places
              </h6>
              <div class="row g-3">
                <div class="col-md-4" v-for="categorie in ['VIP', 'PREMIUM', 'STANDARD']" :key="categorie">
                  <label class="form-label">
                    Places {{ categorie }}
                  </label>
                  <input v-model.number="configForm.repartition[categorie]" type="number" class="form-control" min="0" 
                         @input="calculerTotal">
                  <small class="text-muted">{{ configForm.repartition[categorie] || 0 }} places</small>
                </div>
              </div>
              
              <div class="alert mt-3" :class="totalPlacesAlertClass">
                <i class="bi bi-info-circle me-2"></i>
                Total: {{ totalPlacesCalcule }} / {{ configForm.capacite }} places
                <span v-if="totalPlacesCalcule !== configForm.capacite">
                  (Différence: {{ Math.abs(totalPlacesCalcule - configForm.capacite) }})
                </span>
              </div>
            </div>

            <div class="mt-4">
              <h6 class="fw-semibold mb-3">
                <i class="bi bi-calculator me-2"></i>
                Revenu maximum estimé
              </h6>
              <div class="row g-3">
                <div class="col-md-4" v-for="categorie in ['VIP', 'PREMIUM', 'STANDARD']" :key="categorie">
                  <div class="card border-0 shadow-sm">
                    <div class="card-body text-center">
                      <h6>{{ categorie }}</h6>
                      <div class="text-success fw-bold">
                        {{ formatPrix((configForm.repartition[categorie] || 0) * prixByCategorie[categorie]) }}€
                      </div>
                      <small class="text-muted">
                        {{ configForm.repartition[categorie] || 0 }} × {{ prixByCategorie[categorie] }}€
                      </small>
                    </div>
                  </div>
                </div>
              </div>
              <div class="mt-3 text-center">
                <h5 class="fw-bold text-primary">
                  Revenu total: {{ formatPrix(revenuTotalEstime) }}€
                </h5>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeConfigModal">Annuler</button>
            <button @click="saveConfiguration" type="button" class="btn btn-primary" :disabled="!isConfigurationValid">
              <i class="bi bi-check-circle me-2"></i>
              Enregistrer la configuration
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { salleService } from '../../services/api'
import { placeService } from '../../services/api'
import { ref } from 'vue'

export default {
  name: 'AdminSalles',
  data() {
    return {
      salles: [],
      filteredSalles: [],
      
      // Données pour les sélecteurs
      sallesDisponibles: ref([]),
      filmsDisponibles: ref([]),
      
      // Sélecteur de places miniature
      miniSeatCategories: {}, // { "1-1": "VIP", "1-2": "STANDARD", ... }
      currentCategory: 'VIP', // Catégorie actuellement sélectionnée pour le clic
      showFilters: false,

      // Filtres
      searchText: '',
      typeFilter: '',
      capaciteFilter: '',

      // Tri
      sortBy: 'nom',
      sortOrder: 'asc',

      // Modal
      isEditing: false,
      showModal: false,
      salleForm: {
        nom: '',
        capacite: null,
        type: 'STANDARD',
        description: '',
        repartition: {
          VIP: 0,
          PREMIUM: 0,
          STANDARD: 0
        }
      },
      
      // Variables réactives pour les inputs de répartition
      vipCount: 0,
      premiumCount: 0,
      standardCount: 0,
      
      // Modal configuration des places
      showConfigModal: false,
      selectedSalle: null,
      configForm: {
        capacite: 100,
        repartition: {
          VIP: 10,
          PREMIUM: 20,
          STANDARD: 70
        }
      },
      prixByCategorie: {
        'VIP': 18.00,
        'PREMIUM': 15.00,
        'STANDARD': 12.00
      }
    }
  },
  async mounted() {
    await this.loadSalles()
    this.applyFilters()
  },
  watch: {
    'salleForm.repartition': {
      handler(newVal, oldVal) {
        console.log('Répartition changée:', newVal);
        this.$forceUpdate();
      },
      deep: true
    }
  },
  methods: {
    async loadSalles() {
      try {
        const response = await salleService.getAllSalles()
        console.log('Salles chargées depuis API:', response.data)
        this.salles = response.data
        console.log('this.salles après assignation:', this.salles)
        // Forcer la réactivité
        this.$forceUpdate()
      } catch (error) {
        console.error('Erreur lors du chargement des salles:', error)
        alert('Erreur lors du chargement des salles')
      }
    },

    toggleFilters() {
      this.showFilters = !this.showFilters
    },

    resetFilters() {
      this.searchText = ''
      this.typeFilter = ''
      this.capaciteFilter = ''
      this.sortBy = 'nom'
      this.sortOrder = 'asc'
      this.applyFilters()
    },

    applyFilters() {
      let filtered = [...this.salles]

      // Filtre textuel
      if (this.searchText.trim()) {
        const search = this.searchText.toLowerCase()
        filtered = filtered.filter(s =>
          s.nom.toLowerCase().includes(search) ||
          (s.description && s.description.toLowerCase().includes(search))
        )
      }

      // Filtre par type
      if (this.typeFilter) {
        filtered = filtered.filter(s => s.type === this.typeFilter)
      }

      // Filtre par capacité
      if (this.capaciteFilter) {
        filtered = filtered.filter(s => {
          switch (this.capaciteFilter) {
            case 'small':
              return s.capacite <= 100
            case 'medium':
              return s.capacite > 100 && s.capacite <= 150
            case 'large':
              return s.capacite > 150
            default:
              return true
          }
        })
      }

      // Tri
      filtered.sort((a, b) => {
        let aValue, bValue

        switch (this.sortBy) {
          case 'nom':
            aValue = a.nom.toLowerCase()
            bValue = b.nom.toLowerCase()
            break
          case 'capacite':
            aValue = a.capacite
            bValue = b.capacite
            break
          case 'type':
            aValue = a.type
            bValue = b.type
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

      this.filteredSalles = filtered
    },

    showCreateModal() {
      this.isEditing = false
      this.resetForm()
      this.showModal = true
    },

    editSalle(salle) {
      this.isEditing = true
      this.salleForm = { 
        ...salle,
        // Parser repartitionPlaces si c'est une chaîne JSON
        repartition: salle.repartitionPlaces ? 
          (typeof salle.repartitionPlaces === 'string' ? 
            JSON.parse(salle.repartitionPlaces) : 
            salle.repartitionPlaces) : {}
      }
      this.showModal = true
    },

    closeModal() {
      this.showModal = false
    },

    resetForm() {
      this.salleForm = {
        nom: '',
        capacite: null,
        type: 'STANDARD',
        description: '',
        repartition: {
          VIP: 0,
          PREMIUM: 0,
          STANDARD: 0
        }
      }
    },

    getTypeBadgeClass(type) {
      const classes = {
        'STANDARD': 'badge bg-secondary',
        'PREMIUM': 'badge bg-warning',
        'VIP': 'badge bg-danger',
        'FAMILIAL': 'badge bg-info',
        '3D': 'badge bg-success'
      }
      return classes[type] || 'badge bg-secondary'
    },

    async saveSalle() {
      try {
        // Validation de la répartition des places
        if (this.salleForm.capacite && this.totalPlacesCalculeCreation !== this.salleForm.capacite) {
          alert('La répartition des places doit correspondre à la capacité totale de la salle !')
          return
        }

        // Préparer les données avec la répartition
        const salleData = {
          ...this.salleForm,
          repartitionPlaces: JSON.stringify(this.salleForm.repartition)
        }
        console.log('Données envoyées au backend:', salleData)

        if (this.isEditing) {
          // Modification d'une salle existante
          const response = await salleService.updateSalle(this.salleForm.id, this.salleForm)
          console.log('Réponse updateSalle:', response.data)
          alert('Salle modifiée avec succès !')
        } else {
          await salleService.createSalle(salleData)
          alert('Salle créée avec succès !')
        }

        // Fermer le modal
        this.closeModal()

        // Recharger les salles
        await this.loadSalles()
      } catch (error) {
        console.error('Erreur lors de la sauvegarde:', error)
        const message = error.response?.data?.message || error.message || 'Erreur lors de la sauvegarde'
        alert('Erreur: ' + message)
      }
    },

    async deleteSalle(id) {
      if (confirm('Êtes-vous sûr de vouloir supprimer cette salle ?')) {
        try {
          await salleService.deleteSalle(id)
          alert('Salle supprimée avec succès !')
          await this.loadSalles()
        } catch (error) {
          console.error('Erreur lors de la suppression:', error)
          const message = error.response?.data?.message || error.message || 'Erreur lors de la suppression'
          alert('Erreur: ' + message)
        }
      }
    },

    // Méthodes pour la configuration des places
    configurerPlaces(salle) {
      console.log('Configuration des places pour la salle:', salle)
      this.selectedSalle = salle
      this.configForm = {
        capacite: salle.capacite,
        type: salle.type,
        repartition: {
          VIP: 0,
          PREMIUM: 0,
          STANDARD: salle.capacite || 0
        }
      }
      
      // Si la salle a une répartition configurée, l'utiliser
      if (salle.repartitionPlaces) {
        console.log('repartitionPlaces trouvé:', salle.repartitionPlaces)
        try {
          const repartitionData = JSON.parse(salle.repartitionPlaces)
          console.log('repartitionData parsé:', repartitionData)
          this.configForm.repartition = {
            VIP: repartitionData.VIP || 0,
            PREMIUM: repartitionData.PREMIUM || 0,
            STANDARD: repartitionData.STANDARD || salle.capacite || 0
          }
          console.log('configForm.repartition final:', this.configForm.repartition)
        } catch (e) {
          console.warn('Erreur lors du parsing de repartitionPlaces:', e)
        }
      } else {
        console.log('Pas de repartitionPlaces trouvé pour la salle')
      }
      
      this.showConfigModal = true
    },

    closeConfigModal() {
      this.showConfigModal = false
      this.selectedSalle = null
    },

    calculerTotal() {
      // Recalculer automatiquement si nécessaire
    },

    updateRepartition() {
      // Convertir en nombres et mettre à jour salleForm.repartition
      this.salleForm.repartition.VIP = parseInt(this.vipCount) || 0;
      this.salleForm.repartition.PREMIUM = parseInt(this.premiumCount) || 0;
      this.salleForm.repartition.STANDARD = parseInt(this.standardCount) || 0;
      
      console.log('Répartition mise à jour:', this.salleForm.repartition);
      this.$forceUpdate();
    },

    calculerTotalCreation() {
      // Forcer la conversion en nombres
      this.salleForm.repartition.VIP = parseInt(this.salleForm.repartition.VIP) || 0;
      this.salleForm.repartition.PREMIUM = parseInt(this.salleForm.repartition.PREMIUM) || 0;
      this.salleForm.repartition.STANDARD = parseInt(this.salleForm.repartition.STANDARD) || 0;
      
      // Forcer la mise à jour des computed properties
      this.$forceUpdate();
      
      // Mettre à jour la répartition depuis les places miniatures
      const counts = this.repartitionFromMiniSeats;
      this.salleForm.repartition = { ...counts };
      
      // Debug
      console.log('Répartition mise à jour:', this.salleForm.repartition);
    },

    // Méthodes pour le sélecteur de places miniature
    getMiniSeatClass(row, seat) {
      const key = `${row}-${seat}`;
      const categorie = this.miniSeatCategories[key] || 'STANDARD';
      return `mini-seat-${categorie.toLowerCase()}`;
    },

    toggleMiniSeat(row, seat) {
      const key = `${row}-${seat}`;
      
      // Vérifier que la clé existe
      if (!key) return;
      
      // Si la place a déjà la même catégorie, la désélectionner
      if (this.miniSeatCategories[key] === this.currentCategory) {
        // Désélectionner la place
        delete this.miniSeatCategories[key];
      } else {
        // Assigner la catégorie actuellement sélectionnée
        this.miniSeatCategories[key] = this.currentCategory;
      }
      
      // Mettre à jour la répartition
      this.updateRepartitionFromMiniSeats();
    },

    updateRepartitionFromMiniSeats() {
      // Compter les places par catégorie depuis les places miniatures
      const counts = { VIP: 0, PREMIUM: 0, STANDARD: 0 };
      
      // Parcourir les catégories de manière classique pour éviter les problèmes de réactivité
      for (const key in this.miniSeatCategories) {
        const categorie = this.miniSeatCategories[key];
        if (categorie && counts[categorie] !== undefined) {
          counts[categorie]++;
        }
      }
      
      // Mettre à jour les variables réactives
      this.vipCount = counts.VIP;
      this.premiumCount = counts.PREMIUM;
      this.standardCount = counts.STANDARD;
      
      // Mettre à jour salleForm.repartition
      this.salleForm.repartition = { ...counts };
      
      console.log('Répartition depuis mini-seats:', counts);
      this.$forceUpdate();
    },

    initializeMiniSeats() {
      // Initialiser toutes les places comme STANDARD
      this.miniSeatCategories = {};
      for (let row = 1; row <= this.salleRows; row++) {
        for (let seat = 1; seat <= this.rowSeats.length; seat++) {
          const totalPlaces = (row - 1) * this.rowSeats.length + seat;
          if (totalPlaces <= this.salleForm.capacite) {
            this.miniSeatCategories[`${row}-${seat}`] = 'STANDARD';
          }
        }
      }
    },

    // Surcharge de resetForm pour inclure les places miniatures
    resetForm() {
      this.salleForm = {
        nom: '',
        capacite: null,
        type: 'STANDARD',
        description: '',
        repartition: {
          VIP: 0,
          PREMIUM: 0,
          STANDARD: 0
        }
      };
    },

    resetCounts() {
      this.vipCount = 0;
      this.premiumCount = 0;
      this.standardCount = 0;
    },

    // Réinitialiser les places miniatures
    resetMiniSeats() {
      this.miniSeatCategories = {};
      this.currentCategory = 'VIP';
    },

    async saveConfiguration() {
      if (!this.isConfigurationValid) {
        alert('La répartition des places doit correspondre à la capacité totale')
        return
      }

      try {
        // Appel API réel pour configurer les places
        const response = await placeService.configurerPlacesSalle(this.selectedSalle.id, {
          capacite: this.configForm.capacite,
          type: this.configForm.type,
          repartition: this.configForm.repartition
        })
        
        alert('Configuration enregistrée avec succès !')
        this.closeConfigModal()
        
        // Forcer le rechargement des données avec un petit délai
        await new Promise(resolve => setTimeout(resolve, 500))
        await this.loadSalles()
        this.applyFilters()
        
        // Forcer la réactivité de Vue
        this.$forceUpdate()
      } catch (error) {
        console.error('Erreur lors de la sauvegarde:', error)
        const message = error.response?.data?.message || error.message || 'Erreur lors de la sauvegarde'
        alert('Erreur: ' + message)
      }
    },

    getCategorieBadgeClass(categorie) {
      const classes = {
        'VIP': 'bg-warning',
        'PREMIUM': 'bg-info',
        'STANDARD': 'bg-secondary'
      }
      return classes[categorie] || 'bg-secondary'
    },

    getRepartitionPlaces(salle) {
      if (salle.repartitionPlaces) {
        try {
          return JSON.parse(salle.repartitionPlaces)
        } catch (e) {
          console.warn('Erreur parsing repartitionPlaces:', e)
        }
      }
      // Retourner une répartition par défaut
      return {
        VIP: 0,
        PREMIUM: 0,
        STANDARD: salle.capacite || 0
      }
    },

    getStatutClass(salle) {
      // Déterminer le statut en fonction de la configuration
      const repartition = this.getRepartitionPlaces(salle)
      const totalConfigured = Object.values(repartition).reduce((sum, count) => sum + count, 0)
      const hasVipOrPremium = repartition.VIP > 0 || repartition.PREMIUM > 0
      
      if (totalConfigured === 0) {
        return 'bg-danger' // Non configurée
      } else if (!hasVipOrPremium && repartition.STANDARD === salle.capacite) {
        return 'bg-warning' // Configuration par défaut (tout STANDARD)
      } else if (totalConfigured !== salle.capacite) {
        return 'bg-warning' // Configuration incomplète
      } else {
        return 'bg-success' // Correctement configurée avec VIP/PREMIUM
      }
    },

    getStatutText(salle) {
      const repartition = this.getRepartitionPlaces(salle)
      const totalConfigured = Object.values(repartition).reduce((sum, count) => sum + count, 0)
      const hasVipOrPremium = repartition.VIP > 0 || repartition.PREMIUM > 0
      
      if (totalConfigured === 0) {
        return 'Non configurée'
      } else if (!hasVipOrPremium && repartition.STANDARD === salle.capacite) {
        return 'Par défaut'
      } else if (totalConfigured !== salle.capacite) {
        return 'Incomplète'
      } else {
        return 'Configurée'
      }
    },

    formatPrix(prix) {
      return new Intl.NumberFormat('fr-FR', { minimumFractionDigits: 2 }).format(prix)
    }
  },
  computed: {
    hasActiveFilters() {
      return this.searchText || this.typeFilter || this.capaciteFilter;
    },

    activeFiltersCount() {
      let count = 0;
      if (this.searchText) count++;
      if (this.typeFilter) count++;
      if (this.capaciteFilter) count++;
      return count;
    },

    // Propriétés calculées pour la configuration
    totalPlacesCalcule() {
      return Object.values(this.configForm.repartition).reduce((sum, count) => sum + (count || 0), 0);
    },

    totalPlacesAlertClass() {
      if (this.totalPlacesCalcule === this.configForm.capacite) {
        return 'alert-success';
      } else if (this.totalPlacesCalcule < this.configForm.capacite) {
        return 'alert-warning';
      } else {
        return 'alert-danger';
      }
    },

    // Propriétés calculées pour le formulaire de création
    totalPlacesCalculeCreation() {
      return Object.values(this.salleForm.repartition).reduce((sum, count) => sum + (count || 0), 0);
    },

    totalPlacesAlertClassCreation() {
      if (!this.salleForm.capacite) {
        return 'alert-secondary';
      }
      if (this.totalPlacesCalculeCreation === this.salleForm.capacite) {
        return 'alert-success';
      } else if (this.totalPlacesCalculeCreation < this.salleForm.capacite) {
        return 'alert-warning';
      } else {
        return 'alert-danger';
      }
    },

    isConfigurationValid() {
      return this.totalPlacesCalcule === this.configForm.capacite;
    },

    revenuTotalEstime() {
      return Object.entries(this.configForm.repartition).reduce((total, [categorie, count]) => {
        return total + ((count || 0) * this.prixByCategorie[categorie]);
      }, 0);
    },

    // Computed properties pour le sélecteur de places miniature
    salleRows() {
      if (!this.salleForm.capacite) return 0;
      // Calculer le nombre de rangées (environ 10 places par rangée)
      return Math.ceil(this.salleForm.capacite / 10);
    },

    rowSeats() {
      // Places par rangée (maximum 10)
      return Array.from({length: Math.min(10, this.salleForm.capacite)}, (_, i) => i + 1);
    },

    // Computed pour mettre à jour la répartition automatiquement
    repartitionFromMiniSeats() {
      const counts = { VIP: 0, PREMIUM: 0, STANDARD: 0 };
      
      Object.values(this.miniSeatCategories).forEach(categorie => {
        if (counts[categorie] !== undefined) {
          counts[categorie]++;
        }
      });
      
      return counts;
    }
  }
}
</script>

<style scoped>
@import '../../assets/admin-salles.css';

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

/* Styles pour la configuration des places */
.alert {
  padding: 0.75rem;
  border-radius: 0.375rem;
}

.alert-success {
  background-color: #d1e7dd;
  border-color: #badbcc;
  color: #0f5132;
}

.alert-warning {
  background-color: #fff3cd;
  border-color: #ffeaa7;
  color: #664d03;
}

.alert-danger {
  background-color: #f8d7da;
  border-color: #f5c2c7;
  color: #721c24;
}

.card-body {
  padding: 1rem;
}
/* Styles pour le sélecteur de places miniature */
.seat-selector-container {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 15px;
  margin-top: 10px;
}

.category-selector {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  padding: 15px;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.category-selector .btn-group {
  width: 100%;
}

.category-selector .btn {
  flex: 1;
  font-weight: 600;
  border-radius: 6px !important;
  transition: all 0.2s ease;
}

.category-selector .btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.category-selector .btn-danger {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  border-color: #a93226;
}

.category-selector .btn-outline-danger:hover {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  border-color: #a93226;
}

.category-selector .btn-primary {
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
  border-color: #7d3c98;
}

.category-selector .btn-outline-primary:hover {
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
  border-color: #7d3c98;
}

.category-selector .btn-success {
  background: linear-gradient(135deg, #27ae60, #229954);
  border-color: #1e8449;
}

.category-selector .btn-outline-success:hover {
  background: linear-gradient(135deg, #27ae60, #229954);
  border-color: #1e8449;
}

.mini-cinema-screen {
  margin-bottom: 15px;
  text-align: center;
}

.mini-screen-rectangle {
  background: linear-gradient(135deg, #2c3e50, #34495e);
  color: #fff;
  padding: 6px 15px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: bold;
  letter-spacing: 1px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  display: inline-block;
}

.mini-screen-text {
  color: #fff;
}

.mini-seats-container {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 15px;
}

.mini-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
}

.mini-row-label {
  width: 20px;
  text-align: center;
  color: #fff;
  font-size: 10px;
  font-weight: bold;
}

.mini-seat {
  width: 20px;
  height: 20px;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
  color: white;
}

/* Couleurs des places miniatures */
.mini-seat-vip {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  border: 1px solid #a93226;
}

.mini-seat-vip:hover {
  background: linear-gradient(135deg, #c0392b, #a93226);
  transform: scale(1.1);
}

.mini-seat-premium {
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
  border: 1px solid #7d3c98;
}

.mini-seat-premium:hover {
  background: linear-gradient(135deg, #8e44ad, #7d3c98);
  transform: scale(1.1);
}

.mini-seat-standard {
  background: linear-gradient(135deg, #27ae60, #229954);
  border: 1px solid #1e8449;
}

.mini-seat-standard:hover {
  background: linear-gradient(135deg, #229954, #1e8449);
  transform: scale(1.1);
}

.mini-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.mini-legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 10px;
  color: #000;
}

.mini-legend-item .mini-seat {
  width: 16px;
  height: 16px;
  font-size: 6px;
  cursor: default;
}
</style>
