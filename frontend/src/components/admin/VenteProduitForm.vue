<template>
  <div class="vente-produit-form">
    <div class="card">
      <div class="card-header bg-success text-white">
        <h5 class="mb-0">
          <i class="bi bi-cash-coin me-2"></i>
          Enregistrer une vente de produit
        </h5>
      </div>
      <div class="card-body">
        <form @submit.prevent="submitForm">
          <div class="row">
            <div class="col-md-6 mb-3">
              <label for="seance" class="form-label fw-bold">Séance</label>
              <select class="form-select" id="seance" v-model="vente.idSeance" required @change="onSeanceChange">
                <option value="">Choisir une séance</option>
                <optgroup v-for="group in groupedSeances" :key="group.label" :label="group.label">
                  <option v-for="seance in group.seances" :key="seance.id" :value="seance.id">
                    {{ seance.filmTitre }} - {{ formatDateTime(seance.dateHeure) }} ({{ seance.salleNom }})
                    <span v-if="group.isPast" class="text-muted">[Passée]</span>
                    <span v-else-if="group.isToday" class="text-success">[Aujourd'hui]</span>
                    <span v-else class="text-info">[À venir]</span>
                  </option>
                </optgroup>
              </select>
            </div>
            <div class="col-md-6 mb-3">
              <label for="produit" class="form-label fw-bold">Produit</label>
              <select class="form-select" id="produit" v-model="vente.idProduit" required @change="onProduitChange">
                <option value="">Choisir un produit</option>
                <option v-for="produit in produits" :key="produit.id" :value="produit.id">
                  {{ produit.nom }} - {{ formatMontant(produit.prixUnitaire) }} Ar
                </option>
              </select>
            </div>
          </div>
          
          <div class="row">
            <div class="col-md-4 mb-3">
              <label for="quantite" class="form-label fw-bold">Quantité</label>
              <input 
                type="number" 
                class="form-control" 
                id="quantite" 
                v-model.number="vente.quantite" 
                required 
                min="1"
                @input="calculerTotal"
              >
            </div>
            <div class="col-md-4 mb-3">
              <label for="prixUnitaire" class="form-label fw-bold">Prix unitaire (Ar)</label>
              <input 
                type="number" 
                class="form-control" 
                id="prixUnitaire" 
                v-model.number="vente.prixUnitaireVente" 
                required 
                min="0" 
                step="100"
                @input="calculerTotal"
              >
            </div>
            <div class="col-md-4 mb-3">
              <label class="form-label fw-bold">Total</label>
              <div class="form-control bg-light">
                <strong>{{ formatMontant(total) }} Ar</strong>
              </div>
            </div>
          </div>
          
          <div class="row">
            <div class="col-12">
              <button type="submit" class="btn btn-success me-2" :disabled="loading || !canSubmit">
                <i class="bi bi-check-circle me-2"></i>
                {{ loading ? 'Enregistrement...' : 'Enregistrer la vente' }}
              </button>
              <button type="button" class="btn btn-secondary" @click="resetForm">
                <i class="bi bi-x-circle me-2"></i>
                Annuler
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
    
    <!-- Ventes du jour -->
    <div v-if="ventesDuJour.length > 0" class="card mt-4">
      <div class="card-header bg-info text-white">
        <h6 class="mb-0">
          <i class="bi bi-calendar-day me-2"></i>
          Ventes du jour ({{ new Date().toLocaleDateString('fr-FR') }})
        </h6>
      </div>
      <div class="card-body">
        <div class="table-responsive">
          <table class="table table-sm">
            <thead>
              <tr>
                <th>Séance</th>
                <th>Produit</th>
                <th>Quantité</th>
                <th>Prix unitaire</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="vente in ventesDuJour" :key="vente.id">
                <td>{{ getSeanceInfo(vente.seance) }}</td>
                <td>{{ vente.produit.nom }}</td>
                <td>{{ vente.quantite }}</td>
                <td>{{ formatMontant(vente.prixUnitaireVente) }} Ar</td>
                <td><strong>{{ formatMontant(vente.quantite * vente.prixUnitaireVente) }} Ar</strong></td>
              </tr>
            </tbody>
            <tfoot>
              <tr class="table-info">
                <td colspan="4"><strong>Total du jour</strong></td>
                <td><strong>{{ formatMontant(totalDuJour) }} Ar</strong></td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { venteProduitService } from '../../services/VenteProduitService'
import { produitExtraService } from '../../services/ProduitExtraService'

export default {
  name: 'VenteProduitForm',
  emits: ['vente-added'],
  setup(props, { emit }) {
    const loading = ref(false)
    const message = ref('')
    const messageType = ref('success')
    const produits = ref([])
    const seances = ref([])
    const ventesDuJour = ref([])
    const totalDuJour = ref(0)
    
    const vente = reactive({
      idSeance: null,
      idProduit: null,
      quantite: null,
      prixUnitaireVente: null
    })
    
    const total = computed(() => {
      return (vente.quantite || 0) * (vente.prixUnitaireVente || 0)
    })
    
    const canSubmit = computed(() => {
      return vente.idSeance && vente.idProduit && vente.quantite && vente.prixUnitaireVente
    })
    
    // Grouper les séances par période (passées, aujourd'hui, à venir)
    const groupedSeances = computed(() => {
      if (!seances.value || seances.value.length === 0) return []
      
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      
      const groups = {
        past: { label: 'Séances passées', seances: [], isPast: true, isToday: false },
        today: { label: "Séances d'aujourd'hui", seances: [], isPast: false, isToday: true },
        future: { label: 'Séances à venir', seances: [], isPast: false, isToday: false }
      }
      
      seances.value.forEach(seance => {
        const seanceDate = new Date(seance.dateHeure)
        const seanceDay = new Date(seanceDate.getFullYear(), seanceDate.getMonth(), seanceDate.getDate())
        
        if (seanceDay < today) {
          groups.past.seances.push(seance)
        } else if (seanceDay.getTime() === today.getTime()) {
          groups.today.seances.push(seance)
        } else {
          groups.future.seances.push(seance)
        }
      })
      
      // Retourner seulement les groupes qui ont des séances
      return Object.values(groups).filter(group => group.seances.length > 0)
    })
    
    const loadProduits = async () => {
      try {
        produits.value = await produitExtraService.getAllProduits()
      } catch (error) {
        console.error('Erreur lors du chargement des produits:', error)
      }
    }
    
    const loadSeances = async () => {
      try {
        // Charger toutes les séances (passées, présentes et futures)
        const response = await fetch('/api/seances/toutes')
        
        if (response.ok) {
          let seancesData = await response.json()
          
          seances.value = seancesData
          // Trier par date décroissante (plus récent en premier)
          seances.value.sort((a, b) => new Date(b.dateHeure) - new Date(a.dateHeure))
        } else {
          throw new Error('Erreur lors du chargement des séances')
        }
      } catch (error) {
        console.error('Erreur lors du chargement des séances:', error)
        // En cas d'erreur, essayer de charger les séances existantes
        try {
          const response = await fetch('/api/seances')
          if (response.ok) {
            let seancesData = await response.json()
            seancesData = await enrichSeancesWithDetails(seancesData)
            seances.value = seancesData
            seances.value.sort((a, b) => new Date(b.dateHeure) - new Date(a.dateHeure))
          }
        } catch (fallbackError) {
          console.error('Erreur lors du chargement des séances (fallback):', fallbackError)
        }
      }
    }
    
    const enrichSeancesWithDetails = async (seances) => {
      // Enrichir les séances avec les détails du film et de la salle
      return Promise.all(seances.map(async (seance) => {
        try {
          // Récupérer les détails de la séance avec film et salle
          const detailResponse = await fetch(`/api/seances/${seance.id}`)
          if (detailResponse.ok) {
            const detailSeance = await detailResponse.json()
            return {
              ...seance,
              filmTitre: detailSeance.film?.titre || 'Film inconnu',
              salleNom: detailSeance.salle?.nom || 'Salle inconnue'
            }
          }
        } catch (error) {
          console.warn('Impossible de charger les détails pour la séance', seance.id)
        }
        
        return {
          ...seance,
          filmTitre: 'Film inconnu',
          salleNom: 'Salle inconnue'
        }
      }))
    }
    
    const loadVentesDuJour = async () => {
      try {
        ventesDuJour.value = await venteProduitService.getVentesDuJour()
        
        // Calculer le total du jour
        totalDuJour.value = ventesDuJour.value.reduce((total, vente) => {
          return total + (vente.quantite * vente.prixUnitaireVente)
        }, 0)
      } catch (error) {
        console.error('Erreur lors du chargement des ventes du jour:', error)
      }
    }
    
    const onSeanceChange = () => {
      // Réinitialiser le produit quand la séance change
      vente.idProduit = null
    }
    
    const onProduitChange = () => {
      // Pré-remplir le prix unitaire avec le prix du produit
      const produit = produits.value.find(p => p.id === vente.idProduit)
      if (produit) {
        vente.prixUnitaireVente = produit.prixUnitaire
      }
    }
    
    const calculerTotal = () => {
      // Le total se calcule automatiquement via le computed
    }
    
    const submitForm = async () => {
      loading.value = true
      message.value = ''
      
      try {
        const venteData = {
          seance: { id: vente.idSeance },
          produit: { id: vente.idProduit },
          quantite: vente.quantite,
          prixUnitaireVente: vente.prixUnitaireVente
        }
        
        const newVente = await venteProduitService.createVente(venteData)
        emit('vente-added', newVente)
        message.value = 'Vente enregistrée avec succès!'
        messageType.value = 'success'
        resetForm()
        loadVentesDuJour() // Recharger les ventes du jour
      } catch (error) {
        message.value = error.message || 'Une erreur est survenue'
        messageType.value = 'error'
      } finally {
        loading.value = false
      }
    }
    
    const resetForm = () => {
      vente.idSeance = null
      vente.idProduit = null
      vente.quantite = null
      vente.prixUnitaireVente = null
      message.value = ''
    }
    
    const formatMontant = (montant) => {
      return new Intl.NumberFormat('fr-FR').format(montant)
    }
    
    const formatDateTime = (dateString) => {
      const date = new Date(dateString)
      return date.toLocaleString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    const getSeanceInfo = (seance) => {
      if (!seance) return 'Séance inconnue'
      return `${seance.film?.titre || 'Film inconnu'} - ${formatDateTime(seance.dateHeure)}`
    }
    
    onMounted(() => {
      loadProduits()
      loadSeances()
      loadVentesDuJour()
    })
    
    return {
      vente,
      produits,
      seances,
      ventesDuJour,
      totalDuJour,
      loading,
      message,
      messageType,
      total,
      canSubmit,
      groupedSeances,
      onSeanceChange,
      onProduitChange,
      calculerTotal,
      submitForm,
      resetForm,
      formatMontant,
      formatDateTime,
      getSeanceInfo
    }
  }
}
</script>

<style scoped>
.vente-produit-form {
  max-width: 1000px;
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
  border-color: #198754;
  box-shadow: 0 0 0 0.2rem rgba(25, 135, 84, 0.25);
}

.btn {
  border-radius: 6px;
  font-weight: 500;
}

.alert {
  border-radius: 8px;
}

.table th {
  background-color: #f8f9fa;
  font-weight: 600;
}

.bg-light {
  background-color: #f8f9fa !important;
}
</style>
