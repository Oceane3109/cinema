<template>
  <div class="admin-layout">
    <AdminHeader />
    
    <div class="admin-main">
      <div class="container-fluid">
        <div class="row">
          <div class="col-12">
            <div class="d-flex justify-content-between align-items-center mb-4">
              <h2 class="mb-0">
                <i class="bi bi-film text-primary me-2"></i>
                Gestion Publicitaire
              </h2>
              <div class="btn-group" role="group">
                <button 
                  type="button" 
                  class="btn" 
                  :class="activeTab === 'stats' ? 'btn-primary' : 'btn-outline-primary'"
                  @click="activeTab = 'stats'"
                >
                  <i class="bi bi-graph-up me-1"></i>
                  Stats
                </button>
                <button 
                  type="button" 
                  class="btn" 
                  :class="activeTab === 'paiements' ? 'btn-primary' : 'btn-outline-primary'"
                  @click="activeTab = 'paiements'"
                >
                  <i class="bi bi-cash-coin me-1"></i>
                  Paiements
                </button>
                <button 
                  type="button" 
                  class="btn" 
                  :class="activeTab === 'diffusions' ? 'btn-primary' : 'btn-outline-primary'"
                  @click="activeTab = 'diffusions'"
                >
                  <i class="bi bi-film me-1"></i>
                  Diffusions
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Onglet Stats -->
        <div v-if="activeTab === 'stats'" class="tab-content">
          <PubChiffreAffaire />
        </div>
        
        <!-- Onglet Paiements -->
        <div v-if="activeTab === 'paiements'" class="tab-content">
          <PubPaiementForm @paiement-ajoute="onPaiementAjoute" />
        </div>
        
        <!-- Onglet Diffusions -->
        <div v-if="activeTab === 'diffusions'" class="tab-content">
          <PubDiffusionForm @diffusion-ajoute="onDiffusionAjoute" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import AdminHeader from '../../components/AdminHeader.vue'
import PubChiffreAffaire from '../../components/admin/PubChiffreAffaire.vue'
import PubPaiementForm from '../../components/admin/PubPaiementForm.vue'
import PubDiffusionForm from '../../components/admin/PubDiffusionForm.vue'

export default {
  name: 'PubGestionComplete',
  components: {
    AdminHeader,
    PubChiffreAffaire,
    PubPaiementForm,
    PubDiffusionForm
  },
  setup() {
    const activeTab = ref('stats')
    
    const onPaiementAjoute = () => {
      // Revenir sur les stats après un paiement pour voir les mises à jour
      activeTab.value = 'stats'
    }
    
    const onDiffusionAjoute = () => {
      // Revenir sur les stats après une diffusion pour voir les mises à jour
      activeTab.value = 'stats'
    }
    
    return {
      activeTab,
      onPaiementAjoute,
      onDiffusionAjoute
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

.btn-group {
  border-radius: 10px;
  overflow: hidden;
}

.btn-group .btn {
  border-radius: 0;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-group .btn:first-child {
  border-top-left-radius: 10px;
  border-bottom-left-radius: 10px;
}

.btn-group .btn:last-child {
  border-top-right-radius: 10px;
  border-bottom-right-radius: 10px;
}

.btn-group .btn:hover {
  transform: translateY(-1px);
}

.tab-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .admin-main {
    padding: 1rem;
  }
  
  .btn-group {
    width: 100%;
  }
  
  .btn-group .btn {
    flex: 1;
  }
}
</style>
