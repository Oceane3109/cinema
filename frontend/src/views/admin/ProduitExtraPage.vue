<template>
  <div class="admin-layout">
    <AdminHeader />
    
    <div class="admin-main">
      <div class="container-fluid">
        <div class="row">
          <div class="col-12">
            <div class="d-flex justify-content-between align-items-center mb-4">
              <h2 class="mb-0">
                <i class="bi bi-basket text-primary me-2"></i>
                Gestion des Produits Extras
              </h2>
            </div>
          </div>
        </div>
        
        <!-- Formulaire d'ajout/modification -->
        <ProduitExtraForm 
          @produit-added="onProduitAdded" 
          @produit-updated="onProduitUpdated"
          :editing-produit="editingProduit"
          @cancel-edit="cancelEdit"
        />
        
        <!-- Liste des produits -->
        <div class="card mt-4">
          <div class="card-header bg-light">
            <h5 class="mb-0">
              <i class="bi bi-list-ul me-2"></i>
              Liste des produits disponibles
            </h5>
          </div>
          <div class="card-body">
            <div v-if="loading" class="text-center py-4">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Chargement...</span>
              </div>
            </div>
            
            <div v-else-if="produits.length === 0" class="text-center py-4 text-muted">
              <i class="bi bi-inbox fs-1 d-block mb-3"></i>
              <p>Aucun produit enregistré</p>
            </div>
            
            <div v-else class="table-responsive">
              <table class="table table-hover">
                <thead>
                  <tr>
                    <th>Nom</th>
                    <th>Prix unitaire</th>
                    <th>Description</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="produit in produits" :key="produit.id">
                    <td><strong>{{ produit.nom }}</strong></td>
                    <td>{{ formatMontant(produit.prixUnitaire) }} Ar</td>
                    <td>{{ produit.description || '-' }}</td>
                    <td>
                      <button class="btn btn-sm btn-outline-primary me-2" @click="editProduit(produit)">
                        <i class="bi bi-pencil"></i>
                      </button>
                      <button class="btn btn-sm btn-outline-danger" @click="deleteProduit(produit)">
                        <i class="bi bi-trash"></i>
                      </button>
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
</template>

<script>
import { ref, onMounted } from 'vue'
import AdminHeader from '../../components/AdminHeader.vue'
import ProduitExtraForm from '../../components/admin/ProduitExtraForm.vue'
import { produitExtraService } from '../../services/ProduitExtraService'

export default {
  name: 'ProduitExtraPage',
  components: {
    AdminHeader,
    ProduitExtraForm
  },
  setup() {
    const produits = ref([])
    const loading = ref(false)
    const editingProduit = ref(null)
    
    const loadProduits = async () => {
      loading.value = true
      try {
        produits.value = await produitExtraService.getAllProduits()
      } catch (error) {
        console.error('Erreur lors du chargement des produits:', error)
      } finally {
        loading.value = false
      }
    }
    
    const onProduitAdded = (newProduit) => {
      produits.value.push(newProduit)
    }
    
    const onProduitUpdated = (updatedProduit) => {
      const index = produits.value.findIndex(p => p.id === updatedProduit.id)
      if (index !== -1) {
        produits.value[index] = updatedProduit
      }
      editingProduit.value = null
    }
    
    const editProduit = (produit) => {
      editingProduit.value = { ...produit }
    }
    
    const cancelEdit = () => {
      editingProduit.value = null
    }
    
    const deleteProduit = async (produit) => {
      if (confirm(`Êtes-vous sûr de vouloir supprimer "${produit.nom}" ?`)) {
        try {
          await produitExtraService.deleteProduit(produit.id)
          produits.value = produits.value.filter(p => p.id !== produit.id)
        } catch (error) {
          alert('Erreur lors de la suppression: ' + error.message)
        }
      }
    }
    
    const formatMontant = (montant) => {
      return new Intl.NumberFormat('fr-FR').format(montant)
    }
    
    onMounted(() => {
      loadProduits()
    })
    
    return {
      produits,
      loading,
      editingProduit,
      onProduitAdded,
      onProduitUpdated,
      editProduit,
      cancelEdit,
      deleteProduit,
      formatMontant
    }
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

.admin-main {
  padding: 2rem;
}

.card {
  border: none;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  border-radius: 10px;
}

.table th {
  background-color: #f8f9fa;
  font-weight: 600;
}

.btn-sm {
  border-radius: 4px;
}
</style>
