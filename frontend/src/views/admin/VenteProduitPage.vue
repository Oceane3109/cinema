<template>
  <div class="admin-layout">
    <AdminHeader />
    
    <div class="admin-main">
      <div class="container-fluid">
        <div class="row">
          <div class="col-12">
            <div class="d-flex justify-content-between align-items-center mb-4">
              <h2 class="mb-0">
                <i class="bi bi-cash-coin text-success me-2"></i>
                Vente de Produits Extras
              </h2>
            </div>
          </div>
        </div>
        
        <!-- Formulaire de vente -->
        <VenteProduitForm @vente-added="onVenteAdded" />
      </div>
    </div>
  </div>
</template>

<script>
import AdminHeader from '../../components/AdminHeader.vue'
import VenteProduitForm from '../../components/admin/VenteProduitForm.vue'

export default {
  name: 'VenteProduitPage',
  components: {
    AdminHeader,
    VenteProduitForm
  },
  setup() {
    const onVenteAdded = (newVente) => {
      console.log('Nouvelle vente enregistrée:', newVente)
    }
    
    return {
      onVenteAdded
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
</style>
