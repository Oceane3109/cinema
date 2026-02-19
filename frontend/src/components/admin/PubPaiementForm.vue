<template>
  <div class="container mt-4">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <div class="card shadow">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">
              <i class="bi bi-cash-coin me-2"></i>
              Ajouter un Paiement
            </h5>
          </div>
          <div class="card-body">
            <!-- Statut de la société -->
            <div v-if="paiement.id_societe" class="alert mb-3" :class="estSocietePayeeEnTotalite() ? 'alert-success' : 'alert-info'">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <strong>Statut de paiement:</strong>
                  <span v-if="estSocietePayeeEnTotalite()" class="badge bg-success ms-2">
                    <i class="bi bi-check-circle me-1"></i>
                    Payé en totalité
                  </span>
                  <span v-else class="badge bg-primary ms-2">
                    <i class="bi bi-hourglass-split me-1"></i>
                    En cours
                  </span>
                </div>
                <div class="text-end">
                  <small class="d-block">Reste à payer: {{ formatMontant(getResteAPayer()) }} Ar</small>
                </div>
              </div>
            </div>
            
            <form @submit.prevent="submitPaiement" :class="{ 'opacity-50': estSocietePayeeEnTotalite() }">
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label for="societe" class="form-label fw-bold">Société</label>
                  <select class="form-select" id="societe" v-model="paiement.id_societe" required>
                    <option value="">Choisir une société</option>
                    <option v-for="societe in societes" :key="societe.id" :value="societe.id">
                      {{ societe.nom }}
                    </option>
                  </select>
                </div>
                
                <div class="col-md-6 mb-3">
                  <label for="typePaiement" class="form-label fw-bold">Type de Paiement</label>
                  <select class="form-select" id="typePaiement" v-model="typePaiement" @change="onTypePaiementChange">
                    <option value="montant">Montant fixe</option>
                    <option value="pourcentage">Pourcentage du reste à payer</option>
                  </select>
                </div>
              </div>
              
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label for="montant" class="form-label fw-bold">
                    {{ typePaiement === 'montant' ? 'Montant (Ar)' : 'Pourcentage (%)' }}
                  </label>
                  <div class="input-group">
                    <span class="input-group-text">{{ typePaiement === 'montant' ? 'Ar' : '%' }}</span>
                    <input 
                      type="number" 
                      class="form-control" 
                      id="montant" 
                      v-model="paiement.montant" 
                      :step="typePaiement === 'montant' ? '0.01' : '1'"
                      :min="typePaiement === 'montant' ? '0' : '1'"
                      :max="typePaiement === 'pourcentage' ? '100' : null"
                      required
                    >
                  </div>
                  <small v-if="typePaiement === 'pourcentage' && paiement.montant" class="text-muted">
                    Équivaut à {{ formatMontant(calculerMontantPourcentage()) }} Ar
                  </small>
                </div>
                
                <div class="col-md-6 mb-3">
                  <label for="date_paiement" class="form-label fw-bold">Date de Paiement</label>
                  <input 
                    type="datetime-local" 
                    class="form-control" 
                    id="date_paiement" 
                    v-model="paiement.date_paiement"
                    required
                  >
                  <small class="text-muted">
                    Le paiement sera classé selon le mois de cette date
                  </small>
                </div>
              </div>
              
              <div class="row">
                <div class="col-12 mb-3">
                  <label for="description" class="form-label fw-bold">Description</label>
                  <textarea class="form-control" id="description" v-model="paiement.description" 
                            rows="3" placeholder="Description du paiement..."></textarea>
                </div>
              </div>
              
              <div class="row">
                <div class="col-12">
                  <button type="submit" class="btn btn-primary btn-lg w-100" :disabled="loading" @click="submitPaiement()">
                    <span v-if="!loading">
                      <i class="bi bi-check-circle me-2"></i>
                      Enregistrer le Paiement
                    </span>
                    <span v-else>
                      <span class="spinner-border spinner-border-sm me-2"></span>
                      Enregistrement en cours...
                    </span>
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Message de succès -->
    <div v-if="message" class="alert alert-success mt-3" role="alert">
      <i class="bi bi-check-circle-fill me-2"></i>
      {{ message }}
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  name: 'PubPaiementForm',
  emits: ['paiement-ajoute'],
  setup(props, { emit }) {
    const societes = ref([])
    const typePaiement = ref('montant')
    const statsSocietes = ref([])
    const paiement = ref({
      id_societe: '',
      montant: '',
      description: '',
      date_paiement: new Date().toISOString().slice(0, 16) // Date et heure actuelles par défaut
    })
    const loading = ref(false)
    const message = ref('')
    
    const loadSocietes = async () => {
      try {
        const response = await fetch('/api/pub/total-par-societe')
        const data = await response.json()
        societes.value = data.map(s => ({
          id: s.id,
          nom: s.societe || s.nom,
          montantTotal: s.montantTotal,
          dejaPayer: s.dejaPayer,
          resteAPayer: s.resteAPayer
        }))
      } catch (error) {
        console.error('Erreur:', error)
      }
    }
    
    const onTypePaiementChange = () => {
      paiement.value.montant = ''
    }
    
    const calculerMontantPourcentage = () => {
      if (!paiement.value.id_societe || !paiement.value.montant) return 0
      
      const societe = societes.value.find(s => s.id == paiement.value.id_societe)
      if (!societe) return 0
      
      const pourcentage = parseFloat(paiement.value.montant)
      return (societe.resteAPayer * pourcentage) / 100
    }
    
    const getResteAPayer = () => {
      if (!paiement.value.id_societe) return 0
      
      const societe = societes.value.find(s => s.id == paiement.value.id_societe)
      if (!societe) return 0
      
      return societe.resteAPayer
    }
    
    const formatMontant = (montant) => {
      return new Intl.NumberFormat('fr-FR').format(montant)
    }
    
    const estSocietePayeeEnTotalite = () => {
      if (!paiement.value.id_societe) return false
      
      const societe = societes.value.find(s => s.id == paiement.value.id_societe)
      if (!societe) return false
      
      return societe.resteAPayer <= 0
    }
    
    const submitPaiement = async () => {
      console.log('🚀 Fonction submitPaiement appelée!')
      loading.value = true
      message.value = ''
      
      // Vérifier si la société est déjà payée en totalité
      if (estSocietePayeeEnTotalite()) {
        message.value = 'Cette société est déjà payée en totalité!'
        loading.value = false
        return
      }
      
      // Calculer le montant final
      let montantFinal = paiement.value.montant
      if (typePaiement.value === 'pourcentage') {
        montantFinal = calculerMontantPourcentage().toString()
      }
      
      console.log('🔍 Données du formulaire avant envoi:', {
        ...paiement.value,
        montant: montantFinal
      })
      
      try {
        const response = await fetch('/api/pub/paiement', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            ...paiement.value,
            montant: montantFinal
          })
        })
        
        console.log('🔍 Réponse API:', response.status, response.statusText)
        
        if (response.ok) {
          message.value = 'Paiement enregistré avec succès!'
          console.log('✅ Paiement enregistré, émission de l\'événement paiement-ajoute')
          // Émettre l'événement au composant parent
          emit('paiement-ajoute')
          // Réinitialiser le formulaire
          paiement.value = {
            id_societe: '',
            montant: '',
            description: ''
          }
          typePaiement.value = 'montant'
        } else {
          message.value = 'Erreur lors de l\'enregistrement du paiement'
          console.error('❌ Erreur API:', response.status, response.statusText)
        }
      } catch (error) {
        console.error('❌ Erreur fetch:', error)
        message.value = 'Erreur serveur: ' + error.message
      } finally {
        loading.value = false
      }
    }
    
    onMounted(() => {
      loadSocietes()
    })
    
    return {
      societes,
      typePaiement,
      paiement,
      loading,
      message,
      submitPaiement,
      onTypePaiementChange,
      calculerMontantPourcentage,
      getResteAPayer,
      formatMontant,
      estSocietePayeeEnTotalite
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

.input-group-text {
  background-color: #f8f9fa;
  border: 2px solid #e9ecef;
  border-right: none;
  border-radius: 10px 0 0 10px;
  font-weight: 600;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 10px;
  padding: 0.75rem 2rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.alert-success {
  border-radius: 10px;
  border: none;
  font-weight: 500;
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
}
</style>