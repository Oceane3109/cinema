<template>
  <div class="admin-films">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3>Gestion des Films</h3>
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
          <i class="bi bi-plus-circle me-2"></i>Ajouter un film
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
              placeholder="Titre, réalisateur, genre..."
            >
          </div>

          <!-- Filtre par genre -->
          <div class="col-md-3">
            <label class="form-label">
              <i class="bi bi-tag me-1"></i>
              Genre
            </label>
            <select v-model="genreFilter" @change="applyFilters" class="form-select">
              <option value="">Tous les genres</option>
              <option v-for="genre in availableGenres" :key="genre" :value="genre">
                {{ genre }}
              </option>
            </select>
          </div>

          <!-- Filtre par note -->
          <div class="col-md-3">
            <label class="form-label">
              <i class="bi bi-star me-1"></i>
              Note minimale
            </label>
            <select v-model="noteMinFilter" @change="applyFilters" class="form-select">
              <option value="">Toutes les notes</option>
              <option value="8">≥ 8/10</option>
              <option value="7">≥ 7/10</option>
              <option value="6">≥ 6/10</option>
              <option value="5">≥ 5/10</option>
            </select>
          </div>

          <!-- Filtre par durée -->
          <div class="col-md-6">
            <label class="form-label">
              <i class="bi bi-clock me-1"></i>
              Durée (minutes)
            </label>
            <div class="input-group">
              <input
                v-model.number="dureeMin"
                @input="applyFilters"
                type="number"
                class="form-control"
                placeholder="Min"
                min="0"
              >
              <span class="input-group-text">-</span>
              <input
                v-model.number="dureeMax"
                @input="applyFilters"
                type="number"
                class="form-control"
                placeholder="Max"
                min="0"
              >
            </div>
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
                  <option value="titre">Titre</option>
                  <option value="dureeMinutes">Durée</option>
                  <option value="note">Note</option>
                  <option value="dateSortie">Date de sortie</option>
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
                    name="sortOrderFilms"
                    id="ascFilms"
                    value="asc"
                  >
                  <label class="btn btn-outline-primary btn-sm" for="ascFilms">
                    <i class="bi bi-arrow-up me-1"></i>
                  </label>

                  <input
                    v-model="sortOrder"
                    @change="applyFilters"
                    type="radio"
                    class="btn-check"
                    name="sortOrderFilms"
                    id="descFilms"
                    value="desc"
                  >
                  <label class="btn btn-outline-primary btn-sm" for="descFilms">
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
            <strong>{{ filteredFilms.length }}</strong> film(s) sur <strong>{{ films.length }}</strong>
          </small>
        </div>
      </div>
    </div>

    <!-- Tableau des films -->
    <div class="table-responsive">
      <table class="table table-striped table-hover">
        <thead class="table-dark">
          <tr>
            <th>ID</th>
            <th>Titre</th>
            <th>Durée</th>
            <th>Genre</th>
            <th>Réalisateur</th>
            <th>Note</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="film in filteredFilms" :key="film.id">
            <td>{{ film.id }}</td>
            <td>{{ film.titre }}</td>
            <td>{{ film.dureeMinutes }} min</td>
            <td>{{ film.genre }}</td>
            <td>{{ film.realisateur }}</td>
            <td>{{ film.note }}/10</td>
            <td>
              <button @click="editFilm(film)" class="btn btn-sm btn-outline-primary me-2">
                <i class="bi bi-pencil"></i>
              </button>
              <button @click="deleteFilm(film.id)" class="btn btn-sm btn-outline-danger">
                <i class="bi bi-trash"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal pour créer/éditer un film -->
    <div v-if="showModal" class="modal-backdrop fade show" @click="closeModal"></div>
    <div class="modal fade" :class="{ 'show': showModal, 'd-block': showModal }" :style="{ display: showModal ? 'block' : 'none' }" tabindex="-1">
      <div class="modal-dialog modal-lg" @click.stop>
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEditing ? 'Modifier le film' : 'Ajouter un film' }}
            </h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveFilm">
              <div class="row g-3">
                <div class="col-md-8">
                  <label class="form-label">Titre *</label>
                  <input
                    type="text"
                    v-model="filmForm.titre"
                    class="form-control"
                    required
                  >
                </div>
                <div class="col-md-4">
                  <label class="form-label">Durée (minutes) *</label>
                  <input
                    type="number"
                    v-model.number="filmForm.dureeMinutes"
                    class="form-control"
                    min="1"
                    required
                  >
                </div>
                <div class="col-md-6">
                  <label class="form-label">Genre</label>
                  <input
                    type="text"
                    v-model="filmForm.genre"
                    class="form-control"
                  >
                </div>
                <div class="col-md-6">
                  <label class="form-label">Réalisateur</label>
                  <input
                    type="text"
                    v-model="filmForm.realisateur"
                    class="form-control"
                  >
                </div>
                <div class="col-md-6">
                  <label class="form-label">Date de sortie</label>
                  <input
                    type="date"
                    v-model="filmForm.dateSortie"
                    class="form-control"
                  >
                </div>
                <div class="col-md-6">
                  <label class="form-label">Note (/10)</label>
                  <input
                    type="number"
                    v-model.number="filmForm.note"
                    class="form-control"
                    min="0"
                    max="10"
                    step="0.1"
                  >
                </div>
                <div class="col-12">
                  <label class="form-label">Image du film</label>
                  <div class="image-upload-section">
                    <input
                      type="file"
                      ref="imageInput"
                      @change="handleImageUpload"
                      accept="image/*"
                      class="form-control"
                      style="display: none;"
                    >
                    <div v-if="!filmForm.imageUrl" class="image-upload-placeholder" @click="$refs.imageInput.click()">
                      <i class="bi bi-cloud-upload"></i>
                      <p>Cliquez pour sélectionner une image</p>
                      <small class="text-muted">Formats acceptés: JPG, PNG, GIF (max 5MB)</small>
                    </div>
                    <div v-else class="image-preview">
                      <img :src="getFullImageUrl(filmForm.imageUrl)" alt="Aperçu" class="preview-image">
                      <div class="image-actions">
                        <button type="button" @click="$refs.imageInput.click()" class="btn btn-sm btn-outline-primary">
                          <i class="bi bi-pencil"></i> Changer
                        </button>
                        <button type="button" @click="removeImage()" class="btn btn-sm btn-outline-danger">
                          <i class="bi bi-trash"></i> Supprimer
                        </button>
                      </div>
                    </div>
                    <div v-if="uploadingImage" class="mt-2">
                      <div class="spinner-border spinner-border-sm text-primary" role="status">
                        <span class="visually-hidden">Upload en cours...</span>
                      </div>
                      <span class="ms-2 text-muted">Upload en cours...</span>
                    </div>
                  </div>
                </div>
                <div class="col-12">
                  <label class="form-label">Description</label>
                  <textarea
                    v-model="filmForm.description"
                    class="form-control"
                    rows="3"
                  ></textarea>
                </div>
                <div class="col-md-6">
                  <label class="form-label">Langue</label>
                  <input
                    type="text"
                    v-model="filmForm.langue"
                    class="form-control"
                  >
                </div>
                <div class="col-md-6">
                  <label class="form-label">Pays</label>
                  <input
                    type="text"
                    v-model="filmForm.pays"
                    class="form-control"
                  >
                </div>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">
              Annuler
            </button>
            <button @click="saveFilm" type="button" class="btn btn-primary">
              {{ isEditing ? 'Modifier' : 'Ajouter' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { filmService, uploadService } from '../../services/api'

export default {
  name: 'AdminFilms',
  data() {
    return {
      films: [],
      filteredFilms: [],
      showFilters: false,

      // Filtres
      searchText: '',
      genreFilter: '',
      noteMinFilter: '',
      dureeMin: null,
      dureeMax: null,

      // Tri
      sortBy: 'titre',
      sortOrder: 'asc',

      // Données pour les filtres
      availableGenres: [],

      // Modal
      isEditing: false,
      showModal: false,
      filmForm: {
        titre: '',
        description: '',
        dureeMinutes: null,
        dateSortie: '',
        genre: '',
        realisateur: '',
        acteurs: '',
        note: null,
        langue: '',
        pays: '',
        imageUrl: ''
      },
      uploadingImage: false
    }
  },
  async mounted() {
    await this.loadFilms()
    this.extractGenres()
    this.applyFilters()
  },
  methods: {
    async loadFilms() {
      try {
        const response = await filmService.getAllFilms()
        this.films = response.data
      } catch (error) {
        console.error('Erreur lors du chargement des films:', error)
        alert('Erreur lors du chargement des films')
      }
    },

    extractGenres() {
      const genres = new Set()
      this.films.forEach(film => {
        if (film.genre) {
          film.genre.split(',').forEach(g => genres.add(g.trim()))
        }
      })
      this.availableGenres = Array.from(genres).sort()
    },

    toggleFilters() {
      this.showFilters = !this.showFilters
    },

    resetFilters() {
      this.searchText = ''
      this.genreFilter = ''
      this.noteMinFilter = ''
      this.dureeMin = null
      this.dureeMax = null
      this.sortBy = 'titre'
      this.sortOrder = 'asc'
      this.applyFilters()
    },

    applyFilters() {
      let filtered = [...this.films]

      // Filtre textuel
      if (this.searchText.trim()) {
        const search = this.searchText.toLowerCase()
        filtered = filtered.filter(f =>
          f.titre.toLowerCase().includes(search) ||
          (f.realisateur && f.realisateur.toLowerCase().includes(search)) ||
          (f.genre && f.genre.toLowerCase().includes(search)) ||
          (f.description && f.description.toLowerCase().includes(search))
        )
      }

      // Filtre par genre
      if (this.genreFilter) {
        filtered = filtered.filter(f => f.genre && f.genre.includes(this.genreFilter))
      }

      // Filtre par note minimale
      if (this.noteMinFilter) {
        const minNote = parseFloat(this.noteMinFilter)
        filtered = filtered.filter(f => f.note >= minNote)
      }

      // Filtre par durée
      if (this.dureeMin !== null && this.dureeMin !== undefined) {
        filtered = filtered.filter(f => f.dureeMinutes >= this.dureeMin)
      }
      if (this.dureeMax !== null && this.dureeMax !== undefined) {
        filtered = filtered.filter(f => f.dureeMinutes <= this.dureeMax)
      }

      // Tri
      filtered.sort((a, b) => {
        let aValue, bValue

        switch (this.sortBy) {
          case 'titre':
            aValue = a.titre.toLowerCase()
            bValue = b.titre.toLowerCase()
            break
          case 'dureeMinutes':
            aValue = a.dureeMinutes || 0
            bValue = b.dureeMinutes || 0
            break
          case 'note':
            aValue = a.note || 0
            bValue = b.note || 0
            break
          case 'dateSortie':
            aValue = new Date(a.dateSortie || '1900-01-01')
            bValue = new Date(b.dateSortie || '1900-01-01')
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

      this.filteredFilms = filtered
    },

    showCreateModal() {
      this.isEditing = false
      this.resetForm()
      this.showModal = true
    },

    editFilm(film) {
      this.isEditing = true
      this.filmForm = { ...film }
      this.showModal = true
    },

    closeModal() {
      this.showModal = false
    },

    resetForm() {
      this.filmForm = {
        titre: '',
        description: '',
        dureeMinutes: null,
        dateSortie: '',
        genre: '',
        realisateur: '',
        acteurs: '',
        note: null,
        langue: '',
        pays: ''
      }
    },

    async saveFilm() {
      try {
        if (this.isEditing) {
          await filmService.updateFilm(this.filmForm.id, this.filmForm)
          alert('Film modifié avec succès !')
        } else {
          await filmService.createFilm(this.filmForm)
          alert('Film créé avec succès !')
        }

        // Fermer le modal
        this.closeModal()

        // Recharger les films
        await this.loadFilms()
      } catch (error) {
        console.error('Erreur lors de la sauvegarde:', error)
        const message = error.response?.data?.message || error.message || 'Erreur lors de la sauvegarde'
        alert('Erreur: ' + message)
      }
    },

    async deleteFilm(id) {
      if (confirm('Êtes-vous sûr de vouloir supprimer ce film ?')) {
        try {
          await filmService.deleteFilm(id)
          alert('Film supprimé avec succès !')
          await this.loadFilms()
        } catch (error) {
          console.error('Erreur lors de la suppression:', error)
          const message = error.response?.data?.message || error.message || 'Erreur lors de la suppression'
          alert('Erreur: ' + message)
        }
      }
    },

    async handleImageUpload(event) {
      const file = event.target.files[0]
      if (!file) return

      // Validation du fichier
      if (!file.type.startsWith('image/')) {
        alert('Veuillez sélectionner un fichier image valide.')
        return
      }

      if (file.size > 5 * 1024 * 1024) { // 5MB
        alert('Le fichier ne doit pas dépasser 5MB.')
        return
      }

      this.uploadingImage = true

      try {
        const response = await uploadService.uploadImage(file)
        this.filmForm.imageUrl = response.data.url
      } catch (error) {
        console.error('Erreur lors de l\'upload:', error)
        alert('Erreur lors de l\'upload de l\'image: ' + (error.response?.data?.error || error.message))
      } finally {
        this.uploadingImage = false
      }
    },

    removeImage() {
      this.filmForm.imageUrl = ''
      if (this.$refs.imageInput) {
        this.$refs.imageInput.value = ''
      }
    },

    getFullImageUrl(url) {
      if (!url) return ''
      if (url.startsWith('http')) return url
      return `http://localhost:8080${url}`
    }
  },
  computed: {
    hasActiveFilters() {
      return this.searchText.trim() ||
             this.genreFilter ||
             this.noteMinFilter ||
             (this.dureeMin !== null && this.dureeMin !== undefined) ||
             (this.dureeMax !== null && this.dureeMax !== undefined) ||
             this.sortBy !== 'titre' ||
             this.sortOrder !== 'asc'
    },

    activeFiltersCount() {
      let count = 0
      if (this.searchText.trim()) count++
      if (this.genreFilter) count++
      if (this.noteMinFilter) count++
      if (this.dureeMin !== null && this.dureeMin !== undefined) count++
      if (this.dureeMax !== null && this.dureeMax !== undefined) count++
      if (this.sortBy !== 'titre') count++
      if (this.sortOrder !== 'asc') count++
      return count
    }
  }
}
</script>

<style scoped>
@import '../../assets/admin-films.css';

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
