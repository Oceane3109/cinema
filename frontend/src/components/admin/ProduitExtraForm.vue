<template>
  <div class="produit-extra-form">
    <div class="card">
      <div class="card-header bg-primary text-white">
        <h5 class="mb-0">
          <i class="bi bi-plus-circle me-2"></i>
          {{ editingProduit ? 'Modifier un produit' : 'Ajouter un produit' }}
        </h5>
      </div>
      <div class="card-body">
        <form @submit.prevent="submitForm">
          <div class="row">
            <div class="col-md-6 mb-3">
              <label for="nom" class="form-label fw-bold">Nom du produit</label>
              <input 
                type="text" 
                class="form-control" 
                id="nom" 
                v-model="produit.nom" 
                required 
                placeholder="Ex: Popcorn"
              >
            </div>
            <div class="col-md-6 mb-3">
              <label for="prixUnitaire" class="form-label fw-bold">Prix unitaire (Ar)</label>
              <input 
                type="number" 
                class="form-control" 
                id="prixUnitaire" 
                v-model.number="produit.prixUnitaire" 
                required 
                min="0" 
                step="100"
                placeholder="Ex: 10000"
              >
            </div>
          </div>
          
          <div class="row">
            <div class="col-12 mb-3">
              <label for="description" class="form-label fw-bold">Description</label>
              <textarea 
                class="form-control" 
                id="description" 
                v-model="produit.description" 
                rows="3" 
                placeholder="Description du produit..."
              ></textarea>
            </div>
          </div>
          
          <div class="row">
            <div class="col-12">
              <button type="submit" class="btn btn-primary me-2" :disabled="loading">
                <i class="bi bi-check-circle me-2"></i>
                {{ loading ? 'Enregistrement...' : (editingProduit ? 'Mettre à jour' : 'Ajouter') }}
              </button>
              <button type="button" class="btn btn-secondary" @click="resetForm" v-if="!editingProduit">
                <i class="bi bi-x-circle me-2"></i>
                Annuler
              </button>
              <button type="button" class="btn btn-outline-secondary" @click="$emit('cancel-edit')" v-else>
                <i class="bi bi-arrow-left me-2"></i>
                Retour
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
    
    <!-- Message de succès/erreur -->
    <div v-if="message" class="alert mt-3" :class="messageType === 'success' ? 'alert-success' : 'alert-danger'">
      {{ message }}
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { produitExtraService } from '../../services/ProduitExtraService'

export default {
  name: 'ProduitExtraForm',
  props: {
    editingProduit: {
      type: Object,
      default: null
    }
  },
  emits: ['produit-added', 'produit-updated', 'cancel-edit'],
  setup(props, { emit }) {
    const loading = ref(false)
    const message = ref('')
    const messageType = ref('success')
    
    const produit = reactive({
      nom: '',
      prixUnitaire: null,
      description: ''
    })
    
    // Initialiser le formulaire si on est en mode édition
    if (props.editingProduit) {
      produit.nom = props.editingProduit.nom
      produit.prixUnitaire = props.editingProduit.prixUnitaire
      produit.description = props.editingProduit.description || ''
    }
    
    const submitForm = async () => {
      loading.value = true
      message.value = ''
      
      try {
        if (props.editingProduit) {
          // Mode édition
          const updatedProduit = await produitExtraService.updateProduit(props.editingProduit.id, produit)
          emit('produit-updated', updatedProduit)
          message.value = 'Produit mis à jour avec succès!'
          messageType.value = 'success'
        } else {
          // Mode création
          const newProduit = await produitExtraService.createProduit(produit)
          emit('produit-added', newProduit)
          message.value = 'Produit ajouté avec succès!'
          messageType.value = 'success'
          resetForm()
        }
      } catch (error) {
        message.value = error.message || 'Une erreur est survenue'
        messageType.value = 'error'
      } finally {
        loading.value = false
      }
    }
    
    const resetForm = () => {
      produit.nom = ''
      produit.prixUnitaire = null
      produit.description = ''
      message.value = ''
    }
    
    return {
      produit,
      loading,
      message,
      messageType,
      submitForm,
      resetForm
    }
  }
}
</script>

<style scoped>
.produit-extra-form {
  max-width: 800px;
  margin: 0 auto;
}

.card {
  border: none;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  border-radius: 10px;
}

.card-header {
  border-radius: 10px 10px 0 0 !important;
}

.form-control:focus {
  border-color: #0d6efd;
  box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.25);
}

.btn {
  border-radius: 6px;
  font-weight: 500;
}

.alert {
  border-radius: 8px;
}
</style>
