<template>
  <div class="admin-reservations">
    <div class="d-flex justify-content-between align-items-center mb-4">
              <div>
      <h3>Gestion des Réservations</h3>
                <small :class="backendAccessible ? 'text-success' : 'text-warning'">
                  <i :class="backendAccessible ? 'bi bi-check-circle' : 'bi bi-exclamation-triangle'"></i>
                  {{ backendAccessible ? 'Backend connecté' : 'Backend non accessible - mode dégradé' }}
                </small>
              </div>
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
                      placeholder="Nom client, email, référence..."
                    >
                  </div>

                  <!-- Filtre par statut -->
                  <div class="col-md-3">
                    <label class="form-label">
                      <i class="bi bi-flag me-1"></i>
                      Statut
                    </label>
                    <select v-model="statusFilter" @change="applyFilters" class="form-select">
          <option value="">Tous les statuts</option>
          <option value="CONFIRMEE">Confirmées</option>
          <option value="ANNULEE">Annulées</option>
          <option value="UTILISEE">Utilisées</option>
        </select>
                  </div>

                  <!-- Tri -->
                  <div class="col-md-3">
                    <label class="form-label">
                      <i class="bi bi-sort me-1"></i>
                      Trier par
                    </label>
                    <select v-model="sortBy" @change="applyFilters" class="form-select">
                      <option value="dateReservation">Date de réservation</option>
                      <option value="dateSeance">Date de séance</option>
                      <option value="montantTotal">Montant</option>
                      <option value="nomClient">Nom client</option>
                      <option value="statut">Statut</option>
                    </select>
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

                  <!-- Filtre par montant -->
                  <div class="col-md-4">
                    <label class="form-label">
                      <i class="bi bi-cash me-1"></i>
                      Montant (€)
                    </label>
                    <div class="input-group">
                      <input
                        v-model.number="montantMin"
                        @input="applyFilters"
                        type="number"
                        class="form-control"
                        placeholder="Min"
                        min="0"
                        step="0.01"
                      >
                      <span class="input-group-text">-</span>
                      <input
                        v-model.number="montantMax"
                        @input="applyFilters"
                        type="number"
                        class="form-control"
                        placeholder="Max"
                        min="0"
                        step="0.01"
                      >
                    </div>
                  </div>

                  <!-- Filtre par période -->
                  <div class="col-md-6">
                    <label class="form-label">
                      <i class="bi bi-calendar-range me-1"></i>
                      Période de réservation
                    </label>
                    <div class="input-group">
                      <input
                        v-model="dateDebut"
                        @change="applyFilters"
                        type="date"
                        class="form-control"
                      >
                      <span class="input-group-text">-</span>
                      <input
                        v-model="dateFin"
                        @change="applyFilters"
                        type="date"
                        class="form-control"
                      >
                    </div>
                  </div>

                  <!-- Ordre de tri -->
                  <div class="col-md-6">
                    <label class="form-label">
                      <i class="bi bi-arrow-up-down me-1"></i>
                      Ordre
                    </label>
                    <div class="btn-group w-100">
                      <input
                        v-model="sortOrder"
                        @change="applyFilters"
                        type="radio"
                        class="btn-check"
                        name="sortOrder"
                        id="asc"
                        value="asc"
                      >
                      <label class="btn btn-outline-primary" for="asc">
                        <i class="bi bi-arrow-up me-1"></i>
                        Croissant
                      </label>

                      <input
                        v-model="sortOrder"
                        @change="applyFilters"
                        type="radio"
                        class="btn-check"
                        name="sortOrder"
                        id="desc"
                        value="desc"
                      >
                      <label class="btn btn-outline-primary" for="desc">
                        <i class="bi bi-arrow-down me-1"></i>
                        Décroissant
                      </label>
                    </div>
                  </div>
                </div>

                <!-- Résumé des filtres actifs -->
                <div v-if="hasActiveFilters" class="mt-3 p-2 bg-light rounded">
                  <small class="text-muted">
                    <i class="bi bi-info-circle me-1"></i>
                    <strong>{{ filteredReservations.length }}</strong> résultat(s) sur <strong>{{ reservations.length }}</strong> réservations
                  </small>
                </div>
      </div>
    </div>

    <!-- Statistiques -->
    <div class="row mb-4">
      <div class="col-md-3">
        <div class="card text-center">
          <div class="card-body">
            <h5 class="card-title text-success">{{ stats.confirmed }}</h5>
            <p class="card-text">Confirmées</p>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card text-center">
          <div class="card-body">
            <h5 class="card-title text-warning">{{ stats.used }}</h5>
            <p class="card-text">Utilisées</p>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card text-center">
          <div class="card-body">
            <h5 class="card-title text-danger">{{ stats.cancelled }}</h5>
            <p class="card-text">Annulées</p>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card text-center">
          <div class="card-body">
            <h5 class="card-title text-primary">{{ stats.totalRevenue }}€</h5>
            <p class="card-text">Chiffre d'affaires</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Tableau des réservations -->
    <div class="table-responsive">
      <table class="table table-striped table-hover">
        <thead class="table-dark">
          <tr>
            <th>Référence</th>
            <th>Client</th>
            <th>Film</th>
            <th>Séance</th>
            <th>Places</th>
            <th>Montant</th>
            <th>Statut</th>
            <th>Date</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="reservation in filteredReservations" :key="reservation.id">
            <td>
              <code>{{ reservation.referenceReservation }}</code>
            </td>
            <td>
              <strong>{{ reservation.nomClient }}</strong><br>
              <small class="text-muted">{{ reservation.emailClient }}</small>
            </td>
            <td>{{ reservation.seance?.film?.titre }}</td>
            <td>
              {{ formatDateTime(reservation.seance?.dateHeure) }}<br>
              <small class="text-muted">{{ reservation.seance?.salle?.nom }}</small>
            </td>
            <td>{{ reservation.nombrePlaces }}</td>
            <td>{{ reservation.montantTotal }}€</td>
            <td>
              <span :class="getStatusBadgeClass(reservation.statut)">
                {{ reservation.statut }}
              </span>
            </td>
            <td>{{ formatDate(reservation.dateReservation) }}</td>
            <td>
              <div class="btn-group btn-group-sm">
                <button
                  v-if="reservation.statut === 'CONFIRMEE'"
                  @click="markAsUsed(reservation.id)"
                  class="btn btn-outline-success"
                  title="Marquer comme utilisée"
                >
                  <i class="bi bi-check-circle"></i>
                </button>
                <button
                  v-if="reservation.statut === 'CONFIRMEE'"
                  @click="cancelReservation(reservation.id)"
                  class="btn btn-outline-warning"
                  title="Annuler"
                >
                  <i class="bi bi-x-circle"></i>
                </button>
                <button
                  @click="viewDetails(reservation)"
                  class="btn btn-outline-info"
                  title="Voir détails"
                  data-bs-toggle="modal"
                  data-bs-target="#reservationModal"
                >
                  <i class="bi bi-eye"></i>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal de détails -->
    <div class="modal fade" id="reservationModal" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Détails de la réservation</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body" v-if="selectedReservation">
            <div v-if="loadingDetails" class="text-center py-4">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Chargement...</span>
              </div>
              <p class="mt-2">Chargement des détails...</p>
            </div>
            <div v-else>
            <div class="row">
              <div class="col-12 mb-3">
                <strong>Référence:</strong> <code>{{ selectedReservation.referenceReservation }}</code>
              </div>
              <div class="col-md-6">
                <h6>Client</h6>
                <p><strong>{{ selectedReservation.nomClient }}</strong></p>
                <p>{{ selectedReservation.emailClient }}</p>
                <p>{{ selectedReservation.telephoneClient }}</p>
              </div>
              <div class="col-md-6">
                <h6>Séance</h6>
                <p><strong>{{ selectedReservation.seance?.film?.titre }}</strong></p>
                <p>{{ formatDateTime(selectedReservation.seance?.dateHeure) }}</p>
                <p>{{ selectedReservation.seance?.salle?.nom }}</p>
              </div>
              </div>

              <!-- Places réservées -->
              <div class="mt-4">
                <h6>Places réservées</h6>
                <div class="table-responsive">
                  <table class="table table-sm">
                    <thead>
                      <tr>
                        <th>Rangée</th>
                        <th>Place</th>
                        <th>Prix</th>
                        <th>Billet</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="rp in selectedReservation.reservationPlaces" :key="rp.id">
                        <td>{{ rp.place.numeroRangee }}</td>
                        <td>{{ rp.place.numeroPlace }}</td>
                        <td>{{ rp.prixUnitaire }}€</td>
                        <td>
                          <span v-if="getBilletForPlace(rp.place.id)" class="badge bg-success">✓ Généré</span>
                          <span v-else class="badge bg-warning">En attente</span>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <!-- Billets -->
              <div class="mt-4">
                <h6>Billets générés</h6>
                <div v-if="reservationBillets.length > 0">
                  <div class="d-flex flex-wrap gap-2">
                    <button
                      v-for="billet in reservationBillets"
                      :key="billet.id"
                      @click="downloadBillet(billet.id)"
                      class="btn btn-sm btn-outline-primary"
                      :title="'Billet ' + billet.referenceBillet"
                    >
                      📄 Rangée {{ billet.place.numeroRangee }}, Place {{ billet.place.numeroPlace }}
                    </button>
                  </div>
                </div>
                <div v-else class="text-muted small">
                  <i class="bi bi-info-circle me-1"></i>
                  Billets non disponibles (backend non accessible)
                </div>
              </div>

              <!-- Facture -->
              <div class="mt-4">
                <h6>Facture</h6>
                <div v-if="reservationFacture" class="card">
                  <div class="card-body">
                    <div class="row">
                      <div class="col-md-6">
                        <p><strong>Numéro:</strong> {{ reservationFacture.numeroFacture }}</p>
                        <p><strong>Émission:</strong> {{ formatDate(reservationFacture.dateEmission) }}</p>
                      </div>
                      <div class="col-md-6">
                        <p><strong>Montant HT:</strong> {{ reservationFacture.montantHt }}€</p>
                        <p><strong>TVA ({{ reservationFacture.tvaTaux }}%):</strong> {{ reservationFacture.montantTva }}€</p>
                        <p><strong>Montant TTC:</strong> {{ reservationFacture.montantTtc }}€</p>
                      </div>
                    </div>
                    <button
                      @click="downloadFacture(reservationFacture.id)"
                      class="btn btn-primary btn-sm mt-2"
                    >
                      🧾 Télécharger la facture
                    </button>
                  </div>
                </div>
                <div v-else class="text-muted small">
                  <i class="bi bi-info-circle me-1"></i>
                  Facture non disponible (backend non accessible)
                </div>
              </div>

              <!-- Résumé -->
              <div class="col-12 mt-4">
                <div class="border-top pt-3">
                  <div class="row text-center">
                    <div class="col-4">
                      <strong>{{ selectedReservation.nombrePlaces }}</strong><br>
                      <small>places</small>
                    </div>
                    <div class="col-4">
                      <strong>{{ selectedReservation.montantTotal }}€</strong><br>
                      <small>total</small>
                    </div>
                    <div class="col-4">
                      <span :class="getStatusBadgeClass(selectedReservation.statut)">
                        {{ selectedReservation.statut }}
                      </span>
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
import { reservationService, cinemaService, billetService, factureService } from '../../services/api'

console.log('Services importés:', { billetService, factureService })

export default {
  name: 'AdminReservations',
  data() {
    return {
      reservations: [],
      filteredReservations: [],
      showFilters: false,

      // Filtres
      searchText: '',
      statusFilter: '',
      filmFilter: '',
      salleFilter: '',
      montantMin: null,
      montantMax: null,
      dateDebut: '',
      dateFin: '',

      // Tri
      sortBy: 'dateReservation',
      sortOrder: 'desc',

      // Données pour les filtres
      availableFilms: [],
      availableSalles: [],

      // Détails réservation
      selectedReservation: null,
      reservationBillets: [],
      reservationFacture: null,
      loadingDetails: false,
      backendAccessible: true,

      // Statistiques
      stats: {
        confirmed: 0,
        used: 0,
        cancelled: 0,
        totalRevenue: 0
      }
    }
  },
  computed: {
    hasActiveFilters() {
      return this.searchText.trim() ||
             this.statusFilter ||
             this.filmFilter ||
             this.salleFilter ||
             (this.montantMin !== null && this.montantMin !== undefined) ||
             (this.montantMax !== null && this.montantMax !== undefined) ||
             this.dateDebut ||
             this.dateFin ||
             this.sortBy !== 'dateReservation' ||
             this.sortOrder !== 'desc'
    },

    activeFiltersCount() {
      let count = 0
      if (this.searchText.trim()) count++
      if (this.statusFilter) count++
      if (this.filmFilter) count++
      if (this.salleFilter) count++
      if (this.montantMin !== null && this.montantMin !== undefined) count++
      if (this.montantMax !== null && this.montantMax !== undefined) count++
      if (this.dateDebut) count++
      if (this.dateFin) count++
      if (this.sortBy !== 'dateReservation') count++
      if (this.sortOrder !== 'desc') count++
      return count
    }
  },
  async mounted() {
    await this.checkBackendConnectivity()
    await this.loadReservations()
    await this.loadFilms()
    await this.loadSalles()
    this.calculateStats()
    this.applyFilters()

    // Écouter la fermeture de la modal pour nettoyer les données
    const modal = document.getElementById('reservationModal')
    if (modal) {
      modal.addEventListener('hidden.bs.modal', () => {
        this.selectedReservation = null
        this.reservationBillets = []
        this.reservationFacture = null
        this.loadingDetails = false
      })
    }
  },
  methods: {
    async checkBackendConnectivity() {
      try {
        // Test simple de connectivité avec un endpoint basique
        await reservationService.getAllReservations()
        this.backendAccessible = true
        console.log('✅ Backend accessible')
      } catch (error) {
        this.backendAccessible = false
        console.warn('⚠️ Backend non accessible - mode dégradé activé')
      }
    },

    async loadReservations() {
      try {
        const response = await reservationService.getAllReservations()
        this.reservations = response.data
      } catch (error) {
        console.error('Erreur lors du chargement des réservations:', error)
        alert('Erreur lors du chargement des réservations')
      }
    },

    async loadFilms() {
      try {
        const response = await cinemaService.getFilms()
        this.availableFilms = response.data || []
      } catch (error) {
        console.error('Erreur lors du chargement des films:', error)
        this.availableFilms = []
      }
    },

    async loadSalles() {
      try {
        const response = await cinemaService.getSalles()
        this.availableSalles = response.data || []
      } catch (error) {
        console.error('Erreur lors du chargement des salles:', error)
        this.availableSalles = []
      }
    },

    toggleFilters() {
      this.showFilters = !this.showFilters
    },

    resetFilters() {
      this.searchText = ''
      this.statusFilter = ''
      this.filmFilter = ''
      this.salleFilter = ''
      this.montantMin = null
      this.montantMax = null
      this.dateDebut = ''
      this.dateFin = ''
      this.sortBy = 'dateReservation'
      this.sortOrder = 'desc'
      this.applyFilters()
    },

    applyFilters() {
      let filtered = [...this.reservations]

      // Filtre textuel
      if (this.searchText.trim()) {
        const search = this.searchText.toLowerCase()
        filtered = filtered.filter(r =>
          r.nomClient.toLowerCase().includes(search) ||
          r.emailClient.toLowerCase().includes(search) ||
          r.referenceReservation.toLowerCase().includes(search) ||
          (r.seance?.film?.titre && r.seance.film.titre.toLowerCase().includes(search)) ||
          (r.seance?.salle?.nom && r.seance.salle.nom.toLowerCase().includes(search))
        )
      }

      // Filtre par statut
      if (this.statusFilter) {
        filtered = filtered.filter(r => r.statut === this.statusFilter)
      }

      // Filtre par film
      if (this.filmFilter) {
        filtered = filtered.filter(r => r.seance?.film?.id === parseInt(this.filmFilter))
      }

      // Filtre par salle
      if (this.salleFilter) {
        filtered = filtered.filter(r => r.seance?.salle?.id === parseInt(this.salleFilter))
      }

      // Filtre par montant
      if (this.montantMin !== null && this.montantMin !== undefined) {
        filtered = filtered.filter(r => r.montantTotal >= this.montantMin)
      }
      if (this.montantMax !== null && this.montantMax !== undefined) {
        filtered = filtered.filter(r => r.montantTotal <= this.montantMax)
      }

      // Filtre par période
      if (this.dateDebut) {
        const dateDebut = new Date(this.dateDebut)
        filtered = filtered.filter(r => new Date(r.dateReservation) >= dateDebut)
      }
      if (this.dateFin) {
        const dateFin = new Date(this.dateFin)
        dateFin.setHours(23, 59, 59, 999) // Fin de journée
        filtered = filtered.filter(r => new Date(r.dateReservation) <= dateFin)
      }

      // Tri
      filtered.sort((a, b) => {
        let aValue, bValue

        switch (this.sortBy) {
          case 'dateReservation':
            aValue = new Date(a.dateReservation)
            bValue = new Date(b.dateReservation)
            break
          case 'dateSeance':
            aValue = new Date(a.seance?.dateHeure || '1900-01-01')
            bValue = new Date(b.seance?.dateHeure || '1900-01-01')
            break
          case 'montantTotal':
            aValue = a.montantTotal
            bValue = b.montantTotal
            break
          case 'nomClient':
            aValue = a.nomClient.toLowerCase()
            bValue = b.nomClient.toLowerCase()
            break
          case 'statut':
            aValue = a.statut
            bValue = b.statut
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

      this.filteredReservations = filtered
    },

    calculateStats() {
      this.stats = {
        confirmed: this.reservations.filter(r => r.statut === 'CONFIRMEE').length,
        used: this.reservations.filter(r => r.statut === 'UTILISEE').length,
        cancelled: this.reservations.filter(r => r.statut === 'ANNULEE').length,
        totalRevenue: this.reservations
          .filter(r => r.statut === 'CONFIRMEE' || r.statut === 'UTILISEE')
          .reduce((sum, r) => sum + r.montantTotal, 0)
      }
    },

    formatDateTime(dateTimeString) {
      if (!dateTimeString) return '-'
      const date = new Date(dateTimeString)
      return date.toLocaleDateString('fr-FR') + ' ' + date.toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    formatDate(dateString) {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleDateString('fr-FR')
    },

    getStatusBadgeClass(statut) {
      const classes = {
        'CONFIRMEE': 'badge bg-success',
        'UTILISEE': 'badge bg-info',
        'ANNULEE': 'badge bg-danger'
      }
      return classes[statut] || 'badge bg-secondary'
    },

    async markAsUsed(id) {
      if (confirm('Marquer cette réservation comme utilisée ?')) {
        try {
          await reservationService.useReservation(id)
          alert('Réservation marquée comme utilisée !')
          await this.loadReservations()
          this.calculateStats()
        } catch (error) {
          console.error('Erreur lors de la mise à jour:', error)
          const message = error.response?.data?.message || error.response?.data || error.message || 'Erreur lors de la mise à jour'
          alert('Erreur: ' + message)
        }
      }
    },

    async cancelReservation(id) {
      if (confirm('Annuler cette réservation ?')) {
        try {
          await reservationService.cancelReservation(id)
          alert('Réservation annulée avec succès !')
          await this.loadReservations()
          this.calculateStats()
        } catch (error) {
          console.error('Erreur lors de l\'annulation:', error)
          const message = error.response?.data?.message || error.response?.data || error.message || 'Erreur lors de l\'annulation'
          alert('Erreur: ' + message)
        }
      }
    },

    async viewDetails(reservation) {
      console.log('Ouverture des détails pour la réservation:', reservation.id)
      this.selectedReservation = reservation
      this.loadingDetails = true

      try {
        console.log('Chargement des billets...')
        // Charger les billets
        const billetsResponse = await billetService.getBilletsByReservation(reservation.id)
        this.reservationBillets = billetsResponse.data || []
        console.log('Billets chargés:', this.reservationBillets)

        console.log('Chargement de la facture...')
        // Charger la facture
        const factureResponse = await factureService.getFactureByReservation(reservation.id)
        this.reservationFacture = factureResponse.data
        console.log('Facture chargée:', this.reservationFacture)
      } catch (error) {
        console.error('Erreur réseau lors du chargement des billets/facture:', error)

        // Afficher un message plus compréhensible
        if (error.code === 'NETWORK_ERROR' || error.message.includes('Network Error')) {
          console.warn('⚠️ Backend non accessible - affichage des détails de base uniquement')
          this.reservationBillets = []
          this.reservationFacture = null
          // Ne pas afficher d'alerte pour les erreurs réseau, juste logger
        } else {
          this.reservationBillets = []
          this.reservationFacture = null
          alert('Erreur lors du chargement des billets/facture: ' + error.message)
        }
      } finally {
        this.loadingDetails = false
      }
    },

    getBilletForPlace(placeId) {
      return this.reservationBillets.find(billet => billet.place.id === placeId)
    },

    async downloadBillet(billetId) {
      try {
        const response = await billetService.downloadBilletPdf(billetId)
        const blob = new Blob([response.data], { type: 'application/pdf' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `billet-${billetId}.pdf`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      } catch (error) {
        console.error('Erreur lors du téléchargement du billet:', error)
        alert('Erreur lors du téléchargement du billet')
      }
    },

    async downloadFacture(factureId) {
      try {
        const response = await factureService.downloadFacturePdf(factureId)
        const blob = new Blob([response.data], { type: 'application/pdf' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `facture-${factureId}.pdf`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      } catch (error) {
        console.error('Erreur lors du téléchargement de la facture:', error)
        alert('Erreur lors du téléchargement de la facture')
      }
    }
  }
}
</script>

<style scoped>
@import '../../assets/admin-reservations.css';

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

/* Styles pour la modal de détails */
.modal-lg {
  max-width: 900px;
}

.modal-body .table th {
  font-size: 0.875rem;
  font-weight: 600;
  color: #495057;
  border-top: none;
}

.modal-body .table td {
  font-size: 0.875rem;
  vertical-align: middle;
}

.card {
  border: 1px solid #dee2e6;
  border-radius: 0.375rem;
}

.card-body {
  padding: 1rem;
}

.badge {
  font-size: 0.75rem;
}

.btn-sm {
  font-size: 0.8rem;
  padding: 0.25rem 0.5rem;
}

.spinner-border {
  width: 2rem;
  height: 2rem;
}
</style>
