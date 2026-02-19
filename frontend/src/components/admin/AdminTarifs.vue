<template>
  <div class="admin-tarifs">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="fw-bold">
        <i class="bi bi-ticket-perforated me-2 text-primary"></i>
        Gestion des Tarifs
      </h3>
      <div class="d-flex gap-2">
        <button @click="toggleFilters" class="btn btn-outline-secondary">
          <i class="bi bi-funnel me-1"></i>
          Filtres
          <span v-if="hasActiveFilters" class="badge bg-primary ms-1">{{ activeFiltersCount }}</span>
        </button>
        <button @click="resetFilters" class="btn btn-outline-warning" v-if="hasActiveFilters">
          <i class="bi bi-x-circle me-1"></i>
          Réinitialiser
        </button>
        <button @click="showCreateModal" class="btn btn-primary">
          <i class="bi bi-plus-circle me-2"></i>Ajouter un tarif
        </button>
      </div>
    </div>

    <!-- Filtres -->
    <div v-show="showFilters" class="card mb-4 border-0 shadow-sm">
      <div class="card-header bg-light border-bottom">
        <h6 class="mb-0 fw-semibold">
          <i class="bi bi-funnel me-2 text-primary"></i>
          Filtres et recherche
        </h6>
      </div>
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-4">
            <label class="form-label fw-medium">
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
          <div class="col-md-3">
            <label class="form-label fw-medium">
              <i class="bi bi-tag me-1"></i>
              Genre
            </label>
            <select v-model="filterGenre" @change="applyFilters" class="form-select">
              <option value="">Tous</option>
              <option value="HOMME">Homme</option>
              <option value="FEMME">Femme</option>
              <option value="TOUS">Tous les genres</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label fw-medium">
              <i class="bi bi-bookmark me-1"></i>
              Type
            </label>
            <select v-model="filterType" @change="applyFilters" class="form-select">
              <option value="">Tous</option>
              <option value="STANDARD">Standard</option>
              <option value="ENFANT">Enfant</option>
              <option value="Ado">Ado</option>
              <option value="SENIOR">Senior</option>
              <option value="VIP">VIP</option>
              <option value="GROUPE">Groupe</option>
              <option value="MATINEE">Matinée</option>
              <option value="SOIREE">Soirée</option>
              <option value="PROFESSIONNEL">Professionnel</option>
              <option value="PREMIUM">Premium</option>

            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label fw-medium">
              <i class="bi bi-toggle-on me-1"></i>
              Statut
            </label>
            <select v-model="filterActif" @change="applyFilters" class="form-select">
              <option value="">Tous</option>
              <option value="true">Actifs</option>
              <option value="false">Inactifs</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Tableau des tarifs -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-light border-bottom">
        <h6 class="mb-0 fw-semibold">
          <i class="bi bi-list-ul me-2 text-primary"></i>
          Liste des tarifs ({{ filteredTarifs.length }}/{{ tarifs.length }})
        </h6>
      </div>
      <div class="card-body">
        <div v-if="loading" class="text-center py-4">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Chargement...</span>
          </div>
        </div>
        
        <div v-else-if="filteredTarifs.length === 0" class="text-center py-4 text-muted">
          <i class="bi bi-inbox fs-1 d-block mb-3"></i>
          <p class="mb-0">Aucun tarif trouvé</p>
        </div>

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle">
            <thead class="table-light">
              <tr>
                <th @click="sortBy('nom')" style="cursor: pointer;" class="fw-medium">
                  Nom
                  <i v-if="sortField === 'nom'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                </th>
                <th @click="sortBy('prix')" style="cursor: pointer;" class="fw-medium">
                  Prix
                  <i v-if="sortField === 'prix'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                </th>
                <th class="fw-medium">Type</th>
                <th class="fw-medium">Genre</th>
                <th class="fw-medium">Âge</th>
                <th class="fw-medium">Statut</th>
                <th class="fw-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="tarif in paginatedTarifs" :key="tarif.id" class="border-bottom">
                <td>
                  <div class="fw-semibold">{{ tarif.nom }}</div>
                  <small class="text-muted">{{ tarif.description }}</small>
                </td>
                <td>
                  <span class="badge bg-success fs-6">{{ formatPrix(tarif.prix) }}€</span>
                </td>
                <td>
                  <span class="badge" :class="getTypeBadgeClass(tarif.type)">
                    {{ tarif.type }}
                  </span>
                </td>
                <td>
                  <span class="badge" :class="getGenreBadgeClass(tarif.genre)">
                    <i :class="getGenreIcon(tarif.genre)" class="me-1"></i>
                    {{ getGenreLabel(tarif.genre) }}
                  </span>
                </td>
                <td>
                  <small v-if="tarif.ageMin && tarif.ageMax" class="text-muted">
                    {{ tarif.ageMin }}-{{ tarif.ageMax }} ans
                  </small>
                  <small v-else class="text-muted">-</small>
                </td>
                <td>
                  <div class="form-check form-switch">
                    <input
                      class="form-check-input"
                      type="checkbox"
                      :checked="tarif.actif"
                      @change="toggleActif(tarif)"
                    >
                  </div>
                </td>
                <td>
                  <div class="btn-group" role="group">
                    <button @click="editTarif(tarif)" class="btn btn-sm btn-outline-primary" title="Modifier">
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button @click="deleteTarif(tarif)" class="btn btn-sm btn-outline-danger" title="Supprimer">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <nav v-if="totalPages > 1" class="mt-4">
          <ul class="pagination justify-content-center mb-0">
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button class="page-link" @click="currentPage = 1" :disabled="currentPage === 1">
                <i class="bi bi-chevron-double-left"></i>
              </button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button class="page-link" @click="currentPage--" :disabled="currentPage === 1">
                <i class="bi bi-chevron-left"></i>
              </button>
            </li>
            <li v-for="page in visiblePages" :key="page" class="page-item" :class="{ active: page === currentPage }">
              <button class="page-link" @click="currentPage = page">{{ page }}</button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <button class="page-link" @click="currentPage++" :disabled="currentPage === totalPages">
                <i class="bi bi-chevron-right"></i>
              </button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <button class="page-link" @click="currentPage = totalPages" :disabled="currentPage === totalPages">
                <i class="bi bi-chevron-double-right"></i>
              </button>
            </li>
          </ul>
        </nav>
      </div>
    </div>

    <!-- Modal Création/Modification -->
    <div class="modal fade" id="tarifModal" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-light border-bottom">
            <h5 class="modal-title fw-semibold">
              <i class="bi bi-ticket-perforated me-2 text-primary"></i>
              {{ editingTarif ? 'Modifier le tarif' : 'Ajouter un tarif' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveTarif">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label fw-medium">Nom *</label>
                  <input
                    v-model="tarifForm.nom"
                    type="text"
                    class="form-control"
                    required
                    placeholder="Ex: Adulte Homme"
                  >
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-medium">Prix *</label>
                  <div class="input-group">
                    <input
                      v-model.number="tarifForm.prix"
                      type="number"
                      step="0.01"
                      min="0"
                      class="form-control"
                      required
                      placeholder="12.00"
                    >
                    <span class="input-group-text">€</span>
                  </div>
                </div>
                <div class="col-12">
                  <label class="form-label fw-medium">Description</label>
                  <textarea
                    v-model="tarifForm.description"
                    class="form-control"
                    rows="2"
                    placeholder="Description du tarif..."
                  ></textarea>
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-medium">Type *</label>
                  <select v-model="tarifForm.type" class="form-select" required>
                    <option value="">Sélectionner...</option>
                    <option value="STANDARD">Standard</option>
                    <option value="ENFANT">Enfant</option>
                    <option value="Ado">Ado</option>
                    <option value="SENIOR">Senior</option>
                    <option value="VIP">VIP</option>
                    <option value="GROUPE">Groupe</option>
                    <option value="MATINEE">Matinée</option>
                    <option value="SOIREE">Soirée</option>
                    <option value="PROFESSIONNEL">Professionnel</option>
                    <option value="PREMIUM">Premium</option>

                  </select>
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-medium">Genre *</label>
                  <select v-model="tarifForm.genre" class="form-select" required>
                    <option value="">Sélectionner...</option>
                    <option value="HOMME">Homme</option>
                    <option value="FEMME">Femme</option>
                    <option value="TOUS">Tous les genres</option>
                  </select>
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-medium">Statut</label>
                  <div class="form-check form-switch mt-2">
                    <input
                      v-model="tarifForm.actif"
                      class="form-check-input"
                      type="checkbox"
                    >
                    <label class="form-check-label">Actif</label>
                  </div>
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-medium">Âge minimum</label>
                  <input
                    v-model.number="tarifForm.ageMin"
                    type="number"
                    min="0"
                    class="form-control"
                    placeholder="Ex: 18"
                  >
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-medium">Âge maximum</label>
                  <input
                    v-model.number="tarifForm.ageMax"
                    type="number"
                    min="0"
                    class="form-control"
                    placeholder="Ex: 64"
                  >
                </div>
                <div class="col-12">
                  <label class="form-label fw-medium">Description complémentaire</label>
                  <textarea
                    v-model="tarifForm.descriptionComplementaire"
                    class="form-control"
                    rows="2"
                    placeholder="Informations supplémentaires..."
                  ></textarea>
                </div>
              </div>
            </form>
          </div>
          <div class="modal-footer bg-light border-top">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
              <i class="bi bi-x-circle me-2"></i>
              Annuler
            </button>
            <button type="button" class="btn btn-primary" @click="saveTarif">
              <i class="bi bi-check-circle me-2"></i>
              {{ editingTarif ? 'Modifier' : 'Ajouter' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { Modal } from 'bootstrap'

export default {
  name: 'AdminTarifs',
  setup() {
    const tarifs = ref([])
    const loading = ref(false)
    const showFilters = ref(false)
    
    // Filtres
    const searchText = ref('')
    const filterGenre = ref('')
    const filterType = ref('')
    const filterActif = ref('')
    
    // Tri
    const sortField = ref('nom')
    const sortOrder = ref('asc')
    
    // Pagination
    const currentPage = ref(1)
    const itemsPerPage = ref(10)
    
    // Modal
    let tarifModal = null
    const editingTarif = ref(null)
    const tarifForm = ref({
      nom: '',
      prix: 0,
      description: '',
      type: '',
      genre: '',
      actif: true,
      ageMin: null,
      ageMax: null,
      descriptionComplementaire: ''
    })

    // Computed
    const hasActiveFilters = computed(() => {
      return searchText.value || filterGenre.value || filterType.value || filterActif.value
    })

    const activeFiltersCount = computed(() => {
      let count = 0
      if (searchText.value) count++
      if (filterGenre.value) count++
      if (filterType.value) count++
      if (filterActif.value) count++
      return count
    })

    const filteredTarifs = computed(() => {
      let filtered = [...tarifs.value]
      
      // Recherche textuelle
      if (searchText.value) {
        const search = searchText.value.toLowerCase()
        filtered = filtered.filter(tarif => 
          tarif.nom.toLowerCase().includes(search) ||
          tarif.description?.toLowerCase().includes(search)
        )
      }
      
      // Filtre par genre
      if (filterGenre.value) {
        filtered = filtered.filter(tarif => tarif.genre === filterGenre.value)
      }
      
      // Filtre par type
      if (filterType.value) {
        filtered = filtered.filter(tarif => tarif.type === filterType.value)
      }
      
      // Filtre par statut
      if (filterActif.value) {
        const actif = filterActif.value === 'true'
        filtered = filtered.filter(tarif => tarif.actif === actif)
      }
      
      // Tri
      filtered.sort((a, b) => {
        let aVal = a[sortField.value]
        let bVal = b[sortField.value]
        
        if (typeof aVal === 'string') {
          aVal = aVal.toLowerCase()
          bVal = bVal.toLowerCase()
        }
        
        if (sortOrder.value === 'asc') {
          return aVal > bVal ? 1 : -1
        } else {
          return aVal < bVal ? 1 : -1
        }
      })
      
      return filtered
    })

    const totalPages = computed(() => {
      return Math.ceil(filteredTarifs.value.length / itemsPerPage.value)
    })

    const paginatedTarifs = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage.value
      const end = start + itemsPerPage.value
      return filteredTarifs.value.slice(start, end)
    })

    const visiblePages = computed(() => {
      const pages = []
      const maxVisible = 5
      let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
      let end = Math.min(totalPages.value, start + maxVisible - 1)
      
      if (end - start + 1 < maxVisible) {
        start = Math.max(1, end - maxVisible + 1)
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      
      return pages
    })

    // Méthodes
    const loadTarifs = async () => {
      loading.value = true
      try {
        const response = await fetch('/api/tarifs')
        const data = await response.json()
        tarifs.value = data
      } catch (error) {
        console.error('Erreur lors du chargement des tarifs:', error)
      } finally {
        loading.value = false
      }
    }

    const applyFilters = () => {
      currentPage.value = 1
    }

    const resetFilters = () => {
      searchText.value = ''
      filterGenre.value = ''
      filterType.value = ''
      filterActif.value = ''
      currentPage.value = 1
    }

    const sortBy = (field) => {
      if (sortField.value === field) {
        sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
      } else {
        sortField.value = field
        sortOrder.value = 'asc'
      }
    }

    const toggleFilters = () => {
      showFilters.value = !showFilters.value
    }

    const showCreateModal = () => {
      editingTarif.value = null
      tarifForm.value = {
        nom: '',
        prix: 0,
        description: '',
        type: '',
        genre: '',
        actif: true,
        ageMin: null,
        ageMax: null,
        descriptionComplementaire: ''
      }
      tarifModal.show()
    }

    const editTarif = (tarif) => {
      editingTarif.value = tarif
      tarifForm.value = { ...tarif }
      tarifModal.show()
    }

    const saveTarif = async () => {
      try {
        const url = editingTarif.value ? `/api/tarifs/${editingTarif.value.id}` : '/api/tarifs'
        const method = editingTarif.value ? 'PUT' : 'POST'
        
        const response = await fetch(url, {
          method,
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(tarifForm.value)
        })
        
        if (response.ok) {
          await loadTarifs()
          tarifModal.hide()
          // Toast de succès
        }
      } catch (error) {
        console.error('Erreur lors de la sauvegarde du tarif:', error)
      }
    }

    const deleteTarif = async (tarif) => {
      if (confirm(`Êtes-vous sûr de vouloir supprimer le tarif "${tarif.nom}" ?`)) {
        try {
          const response = await fetch(`/api/tarifs/${tarif.id}`, {
            method: 'DELETE'
          })
          
          if (response.ok) {
            await loadTarifs()
            // Toast de succès
          }
        } catch (error) {
          console.error('Erreur lors de la suppression du tarif:', error)
        }
      }
    }

    const toggleActif = async (tarif) => {
      try {
        const response = await fetch(`/api/tarifs/${tarif.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ ...tarif, actif: !tarif.actif })
        })
        
        if (response.ok) {
          tarif.actif = !tarif.actif
        }
      } catch (error) {
        console.error('Erreur lors de la modification du statut:', error)
      }
    }

    // Utilitaires
    const formatPrix = (prix) => {
      return new Intl.NumberFormat('fr-FR', { minimumFractionDigits: 2 }).format(prix)
    }

    const getTypeBadgeClass = (type) => {
      const classes = {
        'STANDARD': 'bg-primary',
        'ENFANT': 'bg-success',
        'Ado': 'bg-info',
        'SENIOR': 'bg-warning',
        'VIP': 'bg-danger',
        'GROUPE': 'bg-secondary',
        'MATINEE': 'bg-light text-dark',
        'SOIREE': 'bg-dark',
        'PROFESSIONNEL': 'bg-purple'
      }
      return classes[type] || 'bg-secondary'
    }

    const getGenreBadgeClass = (genre) => {
      const classes = {
        'HOMME': 'bg-blue',
        'FEMME': 'bg-pink',
        'TOUS': 'bg-gray'
      }
      return classes[genre] || 'bg-secondary'
    }

    const getGenreIcon = (genre) => {
      const icons = {
        'HOMME': 'bi bi-gender-male',
        'FEMME': 'bi bi-gender-female',
        'TOUS': 'bi bi-people'
      }
      return icons[genre] || 'bi bi-person'
    }

    const getGenreLabel = (genre) => {
      const labels = {
        'HOMME': 'Homme',
        'FEMME': 'Femme',
        'TOUS': 'Tous'
      }
      return labels[genre] || genre
    }

    // Lifecycle
    onMounted(() => {
      loadTarifs()
      tarifModal = new Modal(document.getElementById('tarifModal'))
    })

    return {
      tarifs,
      loading,
      showFilters,
      searchText,
      filterGenre,
      filterType,
      filterActif,
      sortField,
      sortOrder,
      currentPage,
      itemsPerPage,
      tarifForm,
      editingTarif,
      hasActiveFilters,
      activeFiltersCount,
      filteredTarifs,
      totalPages,
      paginatedTarifs,
      visiblePages,
      loadTarifs,
      applyFilters,
      resetFilters,
      sortBy,
      toggleFilters,
      showCreateModal,
      editTarif,
      saveTarif,
      deleteTarif,
      toggleActif,
      formatPrix,
      getTypeBadgeClass,
      getGenreBadgeClass,
      getGenreIcon,
      getGenreLabel
    }
  }
}
</script>

<style scoped>
.admin-tarifs {
  padding: 20px;
  background-color: var(--bg-secondary);
}

/* Badges personnalisés avec les couleurs du projet */
.bg-blue {
  background-color: var(--primary-color) !important;
  color: white;
}

.bg-pink {
  background-color: var(--danger-color) !important;
  color: white;
}

.bg-gray {
  background-color: var(--secondary-color) !important;
  color: white;
}

.bg-purple {
  background-color: #6f42c1 !important;
  color: white;
}

/* Styles de tableau cohérents */
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

/* Badges */
.badge {
  font-size: 0.75em;
  padding: 0.375rem 0.75rem;
  border-radius: var(--radius-full);
  font-weight: 500;
}

/* Boutons */
.btn-group .btn {
  padding: 0.375rem 0.5rem;
  border-radius: var(--radius-md);
  border-color: var(--border-color);
}

.btn-group .btn:hover {
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

/* Formulaire */
.form-control {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 0.75rem;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: all 0.15s ease-in-out;
}

.form-control:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 0.2rem rgba(37, 99, 235, 0.25);
  outline: 0;
}

.form-select {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 0.75rem;
  background-color: var(--bg-primary);
  color: var(--text-primary);
}

.form-select:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 0.2rem rgba(37, 99, 235, 0.25);
  outline: 0;
}

.form-label {
  color: var(--text-primary);
  font-weight: 500;
  margin-bottom: 0.5rem;
}

/* Modal */
.modal-content {
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  background-color: var(--bg-primary);
}

.modal-header {
  background-color: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-color);
  padding: 1.25rem;
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
}

.modal-footer {
  background-color: var(--bg-tertiary);
  border-top: 1px solid var(--border-color);
  padding: 1.25rem;
  border-radius: 0 0 var(--radius-lg) var(--radius-lg);
}

/* Pagination */
.pagination .page-link {
  color: var(--primary-color);
  border: 1px solid var(--border-color);
  padding: 0.5rem 0.75rem;
  background-color: var(--bg-primary);
}

.pagination .page-link:hover {
  background-color: var(--bg-secondary);
  border-color: var(--primary-color);
}

.pagination .page-item.active .page-link {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

.pagination .page-item.disabled .page-link {
  color: var(--text-muted);
  background-color: var(--bg-secondary);
  border-color: var(--border-light);
}

/* Boutons principaux */
.btn-primary {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
  border-radius: var(--radius-md);
  padding: 0.75rem 1.5rem;
  font-weight: 500;
  transition: all 0.15s ease-in-out;
}

.btn-primary:hover {
  background-color: var(--primary-dark);
  border-color: var(--primary-dark);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-outline-primary {
  color: var(--primary-color);
  border-color: var(--primary-color);
  background-color: transparent;
}

.btn-outline-primary:hover {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
}

.btn-outline-secondary {
  color: var(--text-secondary);
  border-color: var(--border-color);
  background-color: transparent;
}

.btn-outline-secondary:hover {
  background-color: var(--secondary-color);
  border-color: var(--secondary-color);
  color: white;
}

.btn-outline-warning {
  color: var(--warning-color);
  border-color: var(--warning-color);
  background-color: transparent;
}

.btn-outline-warning:hover {
  background-color: var(--warning-color);
  border-color: var(--warning-color);
  color: white;
}

.btn-outline-danger {
  color: var(--danger-color);
  border-color: var(--danger-color);
  background-color: transparent;
}

.btn-outline-danger:hover {
  background-color: var(--danger-color);
  border-color: var(--danger-color);
  color: white;
}

/* Spinner */
.spinner-border {
  border-color: var(--primary-color) transparent var(--primary-color) transparent;
}

/* Form switch */
.form-check-input {
  background-color: var(--bg-secondary);
  border-color: var(--border-color);
}

.form-check-input:checked {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

.form-check-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 0.2rem rgba(37, 99, 235, 0.25);
}

/* Animations subtiles */
.btn {
  transition: all 0.15s ease-in-out;
}

.form-control,
.form-select {
  transition: all 0.15s ease-in-out;
}

.card {
  transition: box-shadow 0.15s ease-in-out;
}

.card:hover {
  box-shadow: var(--shadow-md);
}

/* Responsive */
@media (max-width: 768px) {
  .admin-tarifs {
    padding: 1rem;
  }
  
  .table-responsive {
    font-size: 0.875rem;
  }
  
  .btn-group .btn {
    padding: 0.25rem 0.375rem;
  }
}
</style>
