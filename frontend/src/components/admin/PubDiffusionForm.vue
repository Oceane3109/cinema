<template>
  <div class="container mt-4">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <!-- Formulaire d'ajout -->
        <div class="card shadow mb-4">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">
              <i class="bi bi-plus-circle me-2"></i>
              Ajouter une Diffusion Publicitaire
            </h5>
          </div>
          <div class="card-body">
            <form @submit.prevent="submitDiffusion">
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label for="societe" class="form-label fw-bold">Société</label>
                  <select class="form-select" id="societe" v-model="diffusion.id_societe" required>
                    <option value="">Choisir une société</option>
                    <option v-for="societe in societes" :key="societe.id" :value="societe.id">
                      {{ societe.nom }}
                    </option>
                  </select>
                </div>
                
                <div class="col-md-6 mb-3">
                  <label for="seance" class="form-label fw-bold">Séance</label>
                  <select class="form-select" id="seance" v-model="diffusion.id_sceance" required>
                    <option value="">Choisir une séance</option>
                    <option v-for="film in films" :key="film.id" :value="film.id">
                      {{ film.titre }}
                    </option>
                  </select>
                </div>
              </div>
              
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label for="date_heure_diffusion" class="form-label fw-bold">Date de diffusion</label>
                  <input 
                    type="datetime-local" 
                    class="form-control" 
                    id="date_heure_diffusion"
                    v-model="diffusion.date_heure_diffusion"
                    required
                  >
                </div>
                
                <div class="col-md-6 mb-3">
                  <label for="prix_unitaire" class="form-label fw-bold">Prix unitaire (Ar)</label>
                  <input 
                    type="number" 
                    class="form-control" 
                    id="prix_unitaire"
                    v-model="diffusion.prix_unitaire"
                    placeholder="200000"
                    min="0"
                    step="10000"
                    required
                  >
                </div>
              </div>
              
              <div class="text-center">
                <button type="submit" class="btn btn-primary btn-lg px-5" :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                  <i class="bi bi-plus-circle me-2"></i>
                  {{ loading ? 'Ajout en cours...' : 'Ajouter la diffusion' }}
                </button>
              </div>
            </form>
            
            <!-- Message de succès -->
            <div v-if="message" class="alert alert-success mt-3 text-center">
              <i class="bi bi-check-circle me-2"></i>
              {{ message }}
            </div>
          </div>
        </div>
        
        <!-- Liste des diffusions avec pagination -->
        <div class="card shadow">
          <div class="card-header bg-info text-white">
            <div class="d-flex justify-content-between align-items-center">
              <h5 class="mb-0">
                <i class="bi bi-list-ul me-2"></i>
                Diffusions Publicitaires ({{ diffusions.length }})
              </h5>
              <span class="badge bg-light text-dark">
                Page {{ pageActuelle }}/{{ totalPages }}
              </span>
            </div>
          </div>
          <div class="card-body">
            <!-- Filtres -->
            <div class="row mb-3">
              <div class="col-md-4">
                <select class="form-select" v-model="filtreSociete" @change="resetPagination">
                  <option value="">Toutes les sociétés</option>
                  <option value="Vaniala">Vaniala</option>
                  <option value="Lewis">Lewis</option>
                </select>
              </div>
              <div class="col-md-4">
                <input type="text" class="form-control" placeholder="Rechercher un film..." v-model="filtreFilm" @input="resetPagination">
              </div>
              <div class="col-md-4">
                <button class="btn btn-outline-secondary btn-sm w-100" @click="filtreSociete = ''; filtreFilm = ''; resetPagination()">
                  <i class="bi bi-arrow-clockwise me-1"></i>
                  Réinitialiser
                </button>
              </div>
            </div>
            
            <!-- Tableau des diffusions -->
            <div class="table-responsive">
              <table class="table table-hover table-striped">
                <thead class="table-dark">
                  <tr>
                    <th>ID</th>
                    <th>Société</th>
                    <th>Film</th>
                    <th>Date Diffusion</th>
                    <th>Prix (Ar)</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="diffusion in diffusionsPage" :key="diffusion.id">
                    <td>
                      <span class="badge bg-secondary">{{ diffusion.id }}</span>
                    </td>
                    <td>
                      <span class="badge" :class="diffusion.societe === 'Vaniala' ? 'bg-primary' : 'bg-warning text-dark'">
                        {{ diffusion.societe }}
                      </span>
                    </td>
                    <td>
                      <strong>{{ diffusion.film || 'N/A' }}</strong>
                    </td>
                    <td>
                      <small>
                        {{ new Date(diffusion.date_heure_diffusion).toLocaleString('fr-FR', {
                          day: '2-digit',
                          month: '2-digit', 
                          year: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit'
                        }) }}
                      </small>
                    </td>
                    <td>
                      <span class="fw-bold text-success">
                        {{ formatMontant(diffusion.prix_unitaire) }}
                      </span>
                    </td>
                    <td>
                      <button class="btn btn-sm btn-outline-primary" title="Voir les détails">
                        <i class="bi bi-eye"></i>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            
            <!-- Message si aucune diffusion -->
            <div v-if="diffusionsPage.length === 0" class="text-center py-5">
              <i class="bi bi-inbox display-1 text-muted"></i>
              <p class="text-muted mt-3">Aucune diffusion trouvée</p>
            </div>
            
            <!-- Pagination -->
            <nav v-if="totalPages > 1" class="mt-4">
              <ul class="pagination justify-content-center">
                <li class="page-item" :class="{ disabled: pageActuelle === 1 }">
                  <button class="page-link" @click="changerPage(pageActuelle - 1)">
                    <i class="bi bi-chevron-left"></i>
                    Précédent
                  </button>
                </li>
                
                <li v-for="page in totalPages" :key="page" class="page-item" :class="{ active: page === pageActuelle }">
                  <button class="page-link" @click="changerPage(page)">
                    {{ page }}
                  </button>
                </li>
                
                <li class="page-item" :class="{ disabled: pageActuelle === totalPages }">
                  <button class="page-link" @click="changerPage(pageActuelle + 1)">
                    Suivant
                    <i class="bi bi-chevron-right"></i>
                  </button>
                </li>
              </ul>
            </nav>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'

export default {
  name: 'PubDiffusionForm',
  emits: ['diffusion-ajoute'],
  setup(props, { emit }) {
    const films = ref([])
    const diffusions = ref([])
    const societes = ref([])
    const diffusion = ref({
      id_societe: '',
      id_sceance: '',
      date_heure_diffusion: '',
      prix_unitaire: ''
    })
    const loading = ref(false)
    const message = ref('')
    
    // Filtres
    const filtreSociete = ref('')
    const filtreFilm = ref('')
    
    const diffusionsFiltrees = computed(() => {
      return diffusions.value.filter(diffusion => {
        const societeMatch = !filtreSociete.value || diffusion.societe === filtreSociete.value
        const filmMatch = !filtreFilm.value || (diffusion.film && diffusion.film.toLowerCase().includes(filtreFilm.value.toLowerCase()))
        return societeMatch && filmMatch
      })
    })
    
    // Pagination
    const pageActuelle = ref(1)
    const itemsParPage = 5
    
    const totalPages = computed(() => {
      return Math.ceil(diffusionsFiltrees.value.length / itemsParPage)
    })
    
    const diffusionsPage = computed(() => {
      const debut = (pageActuelle.value - 1) * itemsParPage
      const fin = debut + itemsParPage
      return diffusionsFiltrees.value.slice(debut, fin)
    })
    
    const changerPage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        pageActuelle.value = page
      }
    }
    
    const resetPagination = () => {
      pageActuelle.value = 1
    }
    
    const loadSocietes = async () => {
      try {
        const response = await fetch('/api/pub/societes')
        const data = await response.json()
        societes.value = data
      } catch (error) {
        console.error('Erreur:', error)
      }
    }
    
    const loadFilms = async () => {
      try {
        const response = await fetch('/api/seances')
        const data = await response.json()
        films.value = data.map(seance => ({
          id: seance.id,
          titre: `${seance.film.titre} - ${new Date(seance.dateHeure).toLocaleString('fr-FR', {
            day: '2-digit',
            month: '2-digit', 
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
          })}`
        }))
      } catch (error) {
        console.error('Erreur:', error)
      }
    }
    
    const loadDiffusions = async () => {
      try {
        const response = await fetch('/api/pub/diffusions')
        const data = await response.json()
        diffusions.value = data
      } catch (error) {
        console.error('Erreur:', error)
      }
    }
    
    const submitDiffusion = async () => {
      loading.value = true
      message.value = ''
      
      try {
        const response = await fetch('/api/pub/diffusion', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(diffusion.value)
        })
        
        if (response.ok) {
          message.value = 'Diffusion ajoutée avec succès!'
          emit('diffusion-ajoute')
          // Réinitialiser le formulaire
          diffusion.value = {
            id_societe: '',
            id_sceance: '',
            date_heure_diffusion: '',
            prix_unitaire: ''
          }
          // Recharger la liste des diffusions
          loadDiffusions()
        } else {
          message.value = 'Erreur lors de l\'ajout de la diffusion'
        }
      } catch (error) {
        console.error('Erreur:', error)
        message.value = 'Erreur serveur: ' + error.message
      } finally {
        loading.value = false
      }
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    const formatMontant = (montant) => {
      return new Intl.NumberFormat('fr-FR').format(montant)
    }
    
    const getStatusClass = (statut) => {
      switch (statut) {
        case 'TERMINE':
          return 'badge bg-success'
        case 'PROGRAMMEE':
          return 'badge bg-warning'
        case 'ANNULEE':
          return 'badge bg-danger'
        default:
          return 'badge bg-secondary'
      }
    }
    
    onMounted(() => {
      loadSocietes()
      loadFilms()
      loadDiffusions()
    })
    
    return {
      films,
      diffusions,
      societes,
      diffusion,
      loading,
      message,
      submitDiffusion,
      formatDate,
      formatMontant,
      getStatusClass,
      filtreSociete,
      filtreFilm,
      diffusionsFiltrees,
      diffusionsPage,
      pageActuelle,
      totalPages,
      changerPage,
      resetPagination
    }
  }
}
</script>

<style scoped>
.container {
  max-width: 800px;
}

.card {
  border: none;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.card-header {
  border-radius: 15px 15px 0 0 !important;
  font-weight: 600;
}

.form-label {
  color: #495057;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.form-control, .form-select {
  border-radius: 10px;
  border: 2px solid #e9ecef;
  padding: 0.75rem 1rem;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.form-control:focus, .form-select:focus {
  border-color: #0d6efd;
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
}

.nav-tabs .nav-link:hover {
  background-color: #f8f9fa;
}

.nav-tabs .nav-link.active {
  color: #0d6efd;
  background-color: #fff;
  border-color: #dee2e6;
  border-bottom-color: transparent;
  font-weight: 700;
}

.card {
  border: none;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
  transition: box-shadow 0.15s ease-in-out;
}

.card:hover {
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

.table th {
  border-top: none;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 0.5px;
}

.badge {
  font-size: 0.75rem;
  font-weight: 600;
}

.btn {
  font-weight: 500;
  transition: all 0.2s ease-in-out;
}

.btn:hover {
  transform: translateY(-1px);
}

.form-control:focus {
  border-color: #86b7fe;
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
}

.text-primary {
  color: #0d6efd !important;
}

.bg-info {
  background-color: #0dcaf0 !important;
}

.table-responsive {
  border-radius: 0.375rem;
}

.display-1 {
  font-size: 4rem;
  font-weight: 300;
}

/* Styles pour la pagination */
.pagination {
  margin-bottom: 0;
}

.page-link {
  color: #0d6efd;
  border: 1px solid #dee2e6;
  padding: 0.5rem 0.75rem;
  transition: all 0.2s ease-in-out;
}

.page-link:hover {
  color: #0a58ca;
  background-color: #e9ecef;
  border-color: #dee2e6;
}

.page-item.active .page-link {
  background-color: #0d6efd;
  border-color: #0d6efd;
}

.page-item.disabled .page-link {
  color: #6c757d;
  pointer-events: none;
  background-color: #fff;
  border-color: #dee2e6;
}

</style>
