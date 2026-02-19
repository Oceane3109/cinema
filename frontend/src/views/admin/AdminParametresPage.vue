<template>
  <div class="admin-layout">
    <!-- Sidebar -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h4 class="text-white mb-0">
          <i class="bi bi-film me-2"></i>
          Cinéma Admin
        </h4>
      </div>
      <nav class="sidebar-nav">
        <ul class="nav flex-column">
          <li class="nav-item">
            <router-link to="/admin/dashboard" class="nav-link">
              <i class="bi bi-speedometer2 me-2"></i>
              Tableau de bord
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/films" class="nav-link">
              <i class="bi bi-film me-2"></i>
              Films
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/seances" class="nav-link">
              <i class="bi bi-calendar-event me-2"></i>
              Séances
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/salles" class="nav-link">
              <i class="bi bi-building me-2"></i>
              Salles
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/reservations" class="nav-link">
              <i class="bi bi-ticket-perforated me-2"></i>
              Réservations
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/chiffre-affaires" class="nav-link">
              <i class="bi bi-currency-euro me-2"></i>
              Chiffre d'affaires
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/statistiques" class="nav-link">
              <i class="bi bi-graph-up me-2"></i>
              Statistiques
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/parametres" class="nav-link active">
              <i class="bi bi-gear me-2"></i>
              Paramètres
            </router-link>
          </li>
        </ul>
      </nav>
    </div>

    <!-- Main Content -->
    <div class="main-content">
      <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h1 class="h3 mb-0">Paramètres de Réservation</h1>
          <button class="btn btn-primary" @click="showAddModal = true">
            <i class="bi bi-plus-circle me-2"></i>
            Ajouter un paramètre
          </button>
        </div>

        <!-- Paramètres de remise -->
        <div class="card mb-4">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">
              <i class="bi bi-percent me-2"></i>
              Configuration des Remises
            </h5>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-6">
                <div class="mb-3">
                  <label class="form-label fw-bold">Nombre minimum de places pour remise</label>
                  <div class="input-group">
                    <input 
                      type="number" 
                      class="form-control" 
                      v-model="remiseConfig.placesMinimum"
                      @change="updateRemiseConfig"
                      min="1"
                    >
                    <span class="input-group-text">places</span>
                  </div>
                  <small class="text-muted">Nombre de places minimum pour bénéficier d'une remise</small>
                </div>
              </div>
              <div class="col-md-6">
                <div class="mb-3">
                  <label class="form-label fw-bold">Pourcentage de remise</label>
                  <div class="input-group">
                    <input 
                      type="number" 
                      class="form-control" 
                      v-model="remiseConfig.pourcentageRemise"
                      @change="updateRemiseConfig"
                      min="0"
                      max="100"
                      step="0.1"
                    >
                    <span class="input-group-text">%</span>
                  </div>
                  <small class="text-muted">Pourcentage de remise appliqué</small>
                </div>
              </div>
            </div>
            
            <!-- Aperçu de la remise -->
            <div class="alert alert-info mt-3">
              <h6 class="alert-heading">
                <i class="bi bi-info-circle me-2"></i>
                Aperçu de la configuration
              </h6>
              <p class="mb-2">
                Une remise de <strong>{{ remiseConfig.pourcentageRemise }}%</strong> sera appliquée 
                pour toute réservation de <strong>{{ remiseConfig.placesMinimum }} places</strong> ou plus.
              </p>
              <div class="row mt-3">
                <div class="col-md-4">
                  <div class="d-flex align-items-center">
                    <i class="bi bi-calculator text-primary me-2"></i>
                    <span>Exemple: 6 places × 12€ = 72€</span>
                  </div>
                </div>
                <div class="col-md-4">
                  <div class="d-flex align-items-center">
                    <i class="bi bi-percent text-success me-2"></i>
                    <span>Remise: {{ (72 * remiseConfig.pourcentageRemise / 100).toFixed(2) }}€</span>
                  </div>
                </div>
                <div class="col-md-4">
                  <div class="d-flex align-items-center">
                    <i class="bi bi-cash-stack text-warning me-2"></i>
                    <span>Total: {{ (72 - (72 * remiseConfig.pourcentageRemise / 100)).toFixed(2) }}€</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Liste des paramètres -->
        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">
              <i class="bi bi-list-ul me-2"></i>
              Tous les paramètres
            </h5>
          </div>
          <div class="card-body">
            <div class="table-responsive">
              <table class="table table-striped">
                <thead>
                  <tr>
                    <th>Nom du paramètre</th>
                    <th>Valeur</th>
                    <th>Type</th>
                    <th>Description</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="parametre in parametres" :key="parametre.id">
                    <td>
                      <code>{{ parametre.nomParametre }}</code>
                    </td>
                    <td>
                      <span v-if="parametre.typeValeur === 'BOOLEAN'" class="badge" 
                            :class="parametre.valeur === 'true' ? 'bg-success' : 'bg-danger'">
                        {{ parametre.valeur === 'true' ? 'Oui' : 'Non' }}
                      </span>
                      <span v-else class="fw-bold">{{ parametre.valeur }}</span>
                    </td>
                    <td>
                      <span class="badge bg-secondary">{{ parametre.typeValeur }}</span>
                    </td>
                    <td>{{ parametre.description || '-' }}</td>
                    <td>
                      <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-primary" @click="editParametre(parametre)">
                          <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-outline-danger" @click="deleteParametre(parametre.id)">
                          <i class="bi bi-trash"></i>
                        </button>
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

    <!-- Modal Ajout/Modification -->
    <div class="modal fade" :class="{ show: showAddModal }" :style="{ display: showAddModal ? 'block' : 'none' }">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ editingParametre ? 'Modifier' : 'Ajouter' }} un paramètre
            </h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveParametre">
              <div class="mb-3">
                <label class="form-label">Nom du paramètre</label>
                <input type="text" class="form-control" v-model="formData.nomParametre" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Valeur</label>
                <input type="text" class="form-control" v-model="formData.valeur" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Type</label>
                <select class="form-select" v-model="formData.typeValeur" required>
                  <option value="STRING">Texte</option>
                  <option value="INTEGER">Nombre entier</option>
                  <option value="DECIMAL">Nombre décimal</option>
                  <option value="BOOLEAN">Booléen</option>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label">Description</label>
                <textarea class="form-control" v-model="formData.description" rows="3"></textarea>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Annuler</button>
            <button type="button" class="btn btn-primary" @click="saveParametre">
              {{ editingParametre ? 'Modifier' : 'Ajouter' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  name: 'AdminParametresPage',
  setup() {
    const parametres = ref([])
    const showAddModal = ref(false)
    const editingParametre = ref(null)
    const formData = ref({
      nomParametre: '',
      valeur: '',
      typeValeur: 'STRING',
      description: ''
    })
    const remiseConfig = ref({
      placesMinimum: 5,
      pourcentageRemise: 10
    })

    const loadParametres = async () => {
      try {
        const response = await fetch('/api/parametres-reservation')
        if (response.ok) {
          parametres.value = await response.json()
        }
      } catch (error) {
        console.error('Erreur lors du chargement des paramètres:', error)
      }
    }

    const loadRemiseConfig = async () => {
      try {
        const response = await fetch('/api/parametres-reservation/remise/info')
        if (response.ok) {
          const data = await response.json()
          remiseConfig.value = {
            placesMinimum: data.placesMinimum,
            pourcentageRemise: data.pourcentageRemise
          }
        }
      } catch (error) {
        console.error('Erreur lors du chargement de la configuration de remise:', error)
      }
    }

    const updateRemiseConfig = async () => {
      try {
        // Mettre à jour les paramètres de remise
        await updateParametre('REMISE_PLACES_MIN', remiseConfig.value.placesMinimum.toString())
        await updateParametre('REMISE_POURCENTAGE', remiseConfig.value.pourcentageRemise.toString())
      } catch (error) {
        console.error('Erreur lors de la mise à jour de la configuration de remise:', error)
      }
    }

    const updateParametre = async (nom, valeur) => {
      try {
        const response = await fetch(`/api/parametres-reservation/${nom}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            nomParametre: nom,
            valeur: valeur,
            typeValeur: nom.includes('PERCENTAGE') ? 'DECIMAL' : 'INTEGER',
            description: nom.includes('PLACES') ? 'Nombre minimum de places pour remise' : 'Pourcentage de remise'
          })
        })
        
        if (response.ok) {
          await loadParametres()
        }
      } catch (error) {
        console.error('Erreur lors de la mise à jour du paramètre:', error)
      }
    }

    const editParametre = (parametre) => {
      editingParametre.value = parametre
      formData.value = { ...parametre }
      showAddModal.value = true
    }

    const saveParametre = async () => {
      try {
        const url = editingParametre.value 
          ? `/api/parametres-reservation/${editingParametre.value.id}`
          : '/api/parametres-reservation'
        
        const method = editingParametre.value ? 'PUT' : 'POST'
        
        const response = await fetch(url, {
          method,
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(formData.value)
        })
        
        if (response.ok) {
          await loadParametres()
          closeModal()
        }
      } catch (error) {
        console.error('Erreur lors de la sauvegarde du paramètre:', error)
      }
    }

    const deleteParametre = async (id) => {
      if (confirm('Êtes-vous sûr de vouloir supprimer ce paramètre ?')) {
        try {
          const response = await fetch(`/api/parametres-reservation/${id}`, {
            method: 'DELETE'
          })
          
          if (response.ok) {
            await loadParametres()
          }
        } catch (error) {
          console.error('Erreur lors de la suppression du paramètre:', error)
        }
      }
    }

    const closeModal = () => {
      showAddModal.value = false
      editingParametre.value = null
      formData.value = {
        nomParametre: '',
        valeur: '',
        typeValeur: 'STRING',
        description: ''
      }
    }

    onMounted(() => {
      loadParametres()
      loadRemiseConfig()
    })

    return {
      parametres,
      showAddModal,
      editingParametre,
      formData,
      remiseConfig,
      editParametre,
      saveParametre,
      deleteParametre,
      closeModal,
      updateRemiseConfig
    }
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 250px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  position: fixed;
  height: 100vh;
  overflow-y: auto;
}

.sidebar-header {
  padding: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-nav {
  padding: 1rem 0;
}

.sidebar-nav .nav-link {
  color: rgba(255, 255, 255, 0.8);
  padding: 0.75rem 1.5rem;
  border-radius: 0;
  transition: all 0.3s ease;
}

.sidebar-nav .nav-link:hover {
  color: white;
  background-color: rgba(255, 255, 255, 0.1);
}

.sidebar-nav .nav-link.active {
  color: white;
  background-color: rgba(255, 255, 255, 0.2);
  border-left: 4px solid white;
}

.main-content {
  flex: 1;
  margin-left: 250px;
  padding: 2rem;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.modal.show {
  display: block;
  background-color: rgba(0, 0, 0, 0.5);
}

.modal.fade .modal-dialog {
  transform: translate(0, 0);
  transition: transform 0.3s ease-out;
}

.modal.show .modal-dialog {
  transform: translate(0, 0);
}

.btn-group-sm .btn {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
}

.table th {
  border-top: none;
  font-weight: 600;
  color: #495057;
  background-color: #f8f9fa;
}

.alert {
  border: none;
  border-radius: 0.5rem;
}

.alert-info {
  background-color: #e7f5ff;
  color: #084298;
}

.card {
  border: none;
  border-radius: 0.75rem;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

.card-header {
  border-bottom: 1px solid rgba(0, 0, 0, 0.125);
  border-radius: 0.75rem 0.75rem 0 0 !important;
}

.bg-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
}

.input-group-text {
  background-color: #e9ecef;
  border-color: #ced4da;
}

code {
  background-color: #f1f3f4;
  padding: 0.2rem 0.4rem;
  border-radius: 0.25rem;
  font-size: 0.875em;
}
</style>
