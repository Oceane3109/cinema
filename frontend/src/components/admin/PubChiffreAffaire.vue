<template>
  <div class="admin-layout">
    <AdminHeader />
    <div class="admin-main">
      <div class="stats-container">
    <!-- En-tête avec résumé -->
    <div class="stats-header mb-4">
      <div class="row g-3">
        <div class="col">
          <div class="stat-card bg-primary text-white">
            <div class="stat-icon">
              <i class="bi bi-building"></i>
            </div>
            <div class="stat-content">
              <h6 class="stat-label">Total Sociétés</h6>
              <h3 class="stat-value">{{ statsParSociete.length }}</h3>
            </div>
          </div>
        </div>
        <div class="col">
          <div class="stat-card bg-success text-white">
            <div class="stat-icon">
              <i class="bi bi-film"></i>
            </div>
            <div class="stat-content">
              <h6 class="stat-label">Total Diffusions</h6>
              <h3 class="stat-value">{{ totalDiffusions }}</h3>
            </div>
          </div>
        </div>
        <div class="col">
          <div class="stat-card bg-warning text-white">
            <div class="stat-icon">
              <i class="bi bi-cash-coin"></i>
            </div>
            <div class="stat-content">
              <h6 class="stat-label">Total à Payer</h6>
              <h3 class="stat-value">{{ formatMontant(totalGeneral) }} Ar</h3>
            </div>
          </div>
        </div>
        <div class="col">
          <div class="stat-card bg-info text-white">
            <div class="stat-icon">
              <i class="bi bi-piggy-bank"></i>
            </div>
            <div class="stat-content">
              <h6 class="stat-label">Déjà Payé</h6>
              <h3 class="stat-value">{{ formatMontant(totalDejaPayer) }} Ar</h3>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tableau détaillé -->
    <div class="table-container">
      <div class="table-header d-flex justify-content-between align-items-center mb-3">
        <h5 class="mb-0">
          <i class="bi bi-table me-2"></i>
          Détail par Société
        </h5>
        <div class="table-actions">
          <button class="btn btn-sm btn-outline-primary me-2" @click="refreshData">
            <i class="bi bi-arrow-clockwise me-1"></i>
            Actualiser
          </button>
          <button class="btn btn-sm btn-success" @click="showPaiementForm = true">
            <i class="bi bi-plus-circle me-1"></i>
            Nouveau Paiement
          </button>
        </div>
      </div>
      
      <!-- Paiements d'aujourd'hui -->
      <div v-if="statsAujourdhui.length > 0" class="mb-4">
        <h6 class="text-success mb-3">
          <i class="bi bi-calendar-check me-2"></i>
          Paiements d'aujourd'hui ({{ new Date().toLocaleDateString('fr-FR') }})
        </h6>
        <div class="table-responsive">
          <table class="table table-hover table-bordered border-success">
            <thead class="table-light bg-success bg-opacity-10">
              <tr>
                <th><i class="bi bi-building me-1"></i> Société</th>
                <th><i class="bi bi-clock-history me-1"></i> Période</th>
                <th><i class="bi bi-film me-1"></i> Diffusions</th>
                <th><i class="bi bi-cash-stack me-1"></i> Total à payer</th>
                <th><i class="bi bi-check-circle me-1"></i> Déjà payé</th>
                <th><i class="bi bi-hourglass-split me-1"></i> Reste à payer</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="stat in statsAujourdhui" :key="`aujourdhui-${stat.societe}-${stat.periode}`" class="table-row-hover bg-success bg-opacity-5">
                <td><strong>{{ stat.societe }}</strong></td>
                <td><span class="badge bg-secondary">{{ stat.mois }}</span></td>
                <td><span class="badge bg-success">{{ stat.nombreDiffusions }}</span></td>
                <td class="text-primary fw-bold">{{ formatMontant(stat.montantTotal) }} Ar</td>
                <td class="text-info fw-bold">{{ formatMontant(stat.dejaPayer) }} Ar</td>
                <td class="text-warning fw-bold">{{ formatMontant(stat.resteAPayer) }} Ar</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <!-- Paiements passés -->
      <div>
        <h6 class="text-primary mb-3">
          <i class="bi bi-calendar-history me-2"></i>
          Tous les paiements
        </h6>
        <div class="table-responsive">
          <table class="table table-hover">
            <thead class="table-light">
              <tr>
                <th><i class="bi bi-building me-1"></i> Société</th>
                <th><i class="bi bi-clock-history me-1"></i> Période</th>
                <th><i class="bi bi-film me-1"></i> Diffusions</th>
                <th><i class="bi bi-cash-stack me-1"></i> Total à payer</th>
                <th><i class="bi bi-check-circle me-1"></i> Déjà payé</th>
                <th><i class="bi bi-hourglass-split me-1"></i> Reste à payer</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="stat in statsPasses" :key="`${stat.societe}-${stat.periode}`" class="table-row-hover">
                <td><strong>{{ stat.societe }}</strong></td>
                <td><span class="badge bg-secondary">{{ stat.mois }}</span></td>
                <td><span class="badge bg-success">{{ stat.nombreDiffusions }}</span></td>
                <td class="text-primary fw-bold">{{ formatMontant(stat.montantTotal) }} Ar</td>
                <td class="text-info fw-bold">{{ formatMontant(stat.dejaPayer) }} Ar</td>
                <td class="text-warning fw-bold">{{ formatMontant(stat.resteAPayer) }} Ar</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
  </div>
  </div>
  
  <!-- Formulaire de paiement -->
  <div v-if="showPaiementForm" class="payment-modal-overlay" @click.self="showPaiementForm = false">
    <div class="payment-modal" @click.stop>
      <div class="payment-modal-content">
        <div class="payment-modal-header">
          <div class="payment-modal-title">
            <h4 class="mb-0">
              <i class="bi bi-cash-coin text-primary me-2"></i>
              Nouveau Paiement
            </h4>
            <button type="button" class="btn-close" @click="showPaiementForm = false">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
        </div>
        
        <PubPaiementForm @paiement-ajoute="onPaiementAjoute" />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import AdminHeader from '../AdminHeader.vue'
import PubPaiementForm from '../admin/PubPaiementForm.vue'

export default {
  name: 'PubChiffreAffaire',
  components: {
    AdminHeader,
    PubPaiementForm
  },
  setup() {
    const statsParSociete = ref([])
    const statsParMoisEtSociete = ref([])
    const statsAujourdhui = ref([])
    const statsPasses = ref([])
    const paiements = ref([])
    const showPaiementForm = ref(false)
    
    const loadData = async () => {
      try {
        // Charger les stats par société (ancien endpoint qui fonctionne)
        const responseStats = await fetch('/api/pub/total-par-societe')
        const statsData = await responseStats.json()
        
        // Charger les paiements avec mois
        const responsePaiements = await fetch('/api/pub/paiements')
        paiements.value = await responsePaiements.json()
        
        // Charger les diffusions pour filtrer manuellement
        const responseDiffusions = await fetch('/api/pub/diffusions')
        const diffusionsData = await responseDiffusions.json()
        
        // Traiter les stats avec filtrage manuel
        traiterStatsAvecFiltrage(statsData, diffusionsData)
      } catch (error) {
        console.error('Erreur:', error)
      }
    }
    
    const traiterStatsAvecFiltrage = (statsData, diffusionsData) => {
      const statsMap = new Map()
      const aujourdhui = new Date().toLocaleDateString('fr-FR')
      
      // Récupérer TOUTES les sociétés uniques depuis les diffusions
      const toutesSocietes = [...new Set(diffusionsData.map(d => d.societe))]
      
      // Traiter chaque société
      toutesSocietes.forEach(societeNom => {
        // Filtrer les diffusions pour cette société
        const diffusionsSociete = diffusionsData.filter(d => d.societe === societeNom)
        
        // Grouper par mois et année
        const moisAnneesMap = new Map()
        
        diffusionsSociete.forEach(d => {
          const date = new Date(d.date_heure_diffusion)
          const moisAnneeKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
          
          if (!moisAnneesMap.has(moisAnneeKey)) {
            moisAnneesMap.set(moisAnneeKey, {
              annee: date.getFullYear(),
              mois: date.getMonth() + 1,
              diffusions: [],
              montantTotal: 0
            })
          }
          
          const moisAnneeData = moisAnneesMap.get(moisAnneeKey)
          moisAnneeData.diffusions.push(d)
          moisAnneeData.montantTotal += d.prix_unitaire
        })
        
        // Créer une entrée pour chaque mois/année
        moisAnneesMap.forEach((data, key) => {
          const moisAnneeLabel = `${getMoisLibelle(data.mois)} ${data.annee}`
          
          // Récupérer le deja_payer depuis les stats si disponible
          const statSociete = statsData.find(s => s.societe === societeNom)
          // Pour l'instant, on considère que tout est à payer pour chaque période
          const dejaPayer = 0
          
          const statKey = `${key}-${societeNom}`
          statsMap.set(statKey, {
            mois: moisAnneeLabel,
            societe: societeNom,
            periode: key, // Garder la clé pour le tri
            nombreDiffusions: data.diffusions.length,
            montantTotal: data.montantTotal,
            dejaPayer: dejaPayer,
            resteAPayer: data.montantTotal - dejaPayer,
            datePaiement: null
          })
        })
      })
      
      // Mettre à jour avec les paiements réels
      paiements.value.forEach(paiement => {
        // Trouver la période correspondante à ce paiement
        const paiementDate = new Date(paiement.date_paiement)
        const paiementKey = `${paiementDate.getFullYear()}-${String(paiementDate.getMonth() + 1).padStart(2, '0')}`
        
        const keys = Array.from(statsMap.keys()).filter(key => 
          key.startsWith(paiementKey) && key.endsWith(paiement.societe)
        )
        
        keys.forEach(key => {
          const existing = statsMap.get(key)
          if (existing) {
            // Mettre à jour le dejaPayer avec les paiements pour cette société et période
            const paiementsSocietePeriode = paiements.value.filter(p => {
              const pDate = new Date(p.date_paiement)
              const pKey = `${pDate.getFullYear()}-${String(pDate.getMonth() + 1).padStart(2, '0')}`
              return p.societe === paiement.societe && pKey === paiementKey
            })
            existing.dejaPayer = paiementsSocietePeriode.reduce((sum, p) => sum + p.montant, 0)
            existing.resteAPayer = existing.montantTotal - existing.dejaPayer
            existing.datePaiement = paiement.date_paiement
          }
        })
      })
      
      // Convertir en tableau et séparer
      const allStats = Array.from(statsMap.values())
      
      // Trier par période (du plus récent au plus ancien)
      allStats.sort((a, b) => b.periode.localeCompare(a.periode))
      
      // Séparer par date de paiement
      statsAujourdhui.value = allStats.filter(stat => {
        if (!stat.datePaiement) return false
        const datePaiement = new Date(stat.datePaiement).toLocaleDateString('fr-FR')
        return datePaiement === aujourdhui
      })
      
      statsPasses.value = allStats.filter(stat => {
        if (!stat.datePaiement) return true
        const datePaiement = new Date(stat.datePaiement).toLocaleDateString('fr-FR')
        return datePaiement !== aujourdhui
      })
    }
    
    const getMoisLibelle = (mois) => {
      const moisLabels = [
        'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
        'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'
      ]
      return moisLabels[mois - 1] || mois
    }
    
    const refreshData = () => {
      loadData()
    }
    
    const onPaiementAjoute = () => {
      console.log('🎯 Événement paiement-ajoute reçu dans le parent!')
      showPaiementForm.value = false
      loadData() // Recharger les stats après ajout
    }
    
    // Calculs des totaux
    const totalDiffusions = computed(() => {
      return statsParMoisEtSociete.value.reduce((sum, stat) => sum + stat.nombreDiffusions, 0)
    })
    
    const totalGeneral = computed(() => {
      return statsParMoisEtSociete.value.reduce((sum, stat) => sum + stat.montantTotal, 0)
    })
    
    const totalDejaPayer = computed(() => {
      return statsParMoisEtSociete.value.reduce((sum, stat) => sum + stat.dejaPayer, 0)
    })
    
    const formatMontant = (montant) => {
      return new Intl.NumberFormat('fr-FR').format(montant)
    }
    
    onMounted(() => {
      loadData()
    })
    
    return {
      statsParSociete,
      statsParMoisEtSociete,
      statsAujourdhui,
      statsPasses,
      paiements,
      showPaiementForm,
      totalDiffusions,
      totalGeneral,
      totalDejaPayer,
      formatMontant,
      refreshData,
      onPaiementAjoute
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
  padding-top: 70px;
}

.stats-container {
  padding: 2rem;
}

.stats-header {
  margin-bottom: 2rem;
}

.stat-card {
  border-radius: 15px;
  padding: 1.5rem;
  text-align: center;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  font-size: 2rem;
  margin-bottom: 1rem;
  opacity: 0.8;
}

.stat-label {
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  opacity: 0.9;
  margin-bottom: 0.5rem;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0;
}

.table-container {
  background: white;
  border-radius: 15px;
  padding: 1.5rem;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
}

.table-header {
  border-bottom: 2px solid #e9ecef;
  padding-bottom: 1rem;
  margin-bottom: 1.5rem;
}

.table {
  margin-bottom: 0;
  border-radius: 10px;
  overflow: hidden;
}

.table th {
  background-color: #f8f9fa;
  border-bottom: 2px solid #dee2e6;
  font-weight: 600;
  color: #495057;
  padding: 1rem;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.table td {
  padding: 1rem;
  vertical-align: middle;
  border-bottom: 1px solid #f1f3f5;
}

.table-row-hover:hover {
  background-color: #f8f9fa;
  transform: scale(1.01);
  transition: all 0.2s ease;
}

.badge {
  padding: 0.5rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
}

.btn-outline-primary {
  border-radius: 8px;
  border: 2px solid #0d6efd;
  color: #0d6efd;
  transition: all 0.3s ease;
}

.btn-outline-primary:hover {
  background-color: #0d6efd;
  color: white;
  transform: translateY(-1px);
}

.btn-success {
  border-radius: 8px;
  border: 2px solid #198754;
  color: white;
  transition: all 0.3s ease;
}

.btn-success:hover {
  background-color: #157347;
  color: white;
  transform: translateY(-1px);
}

.payment-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
  backdrop-filter: blur(5px);
  animation: fadeIn 0.3s ease;
}

.payment-modal {
  background: white;
  border-radius: 20px;
  padding: 0;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  animation: slideUp 0.3s ease;
}

.payment-modal-content {
  position: relative;
}

.payment-modal-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1.5rem;
  border-radius: 20px 20px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.payment-modal-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.btn-close {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.3s ease;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-close:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
  transform: scale(1.1);
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.payment-header {
  border-bottom: 2px solid #e9ecef;
  padding-bottom: 1rem;
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-close {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  color: #6c757d;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.btn-close:hover {
  background-color: #f8f9fa;
  color: #495057;
}

/* Responsive */
@media (max-width: 768px) {
  .payment-form {
    width: 95%;
    padding: 1.5rem;
  }
  
  .stats-header .row > .col {
    margin-bottom: 1rem;
  }
  
  .table-container {
    padding: 1rem;
  }
  
  .table th, .table td {
    padding: 0.75rem 0.5rem;
    font-size: 0.8rem;
  }
}
</style>