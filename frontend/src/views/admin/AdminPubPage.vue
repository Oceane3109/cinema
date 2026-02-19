<template>
  <div class="admin-page">
    <AdminHeader />
    <div class="d-flex">
      <AdminSidebar />
      <main class="admin-main">
        <div class="container-fluid p-4">
          <h1 class="mb-4">💰 Gestion Publicité</h1>
          
          <div class="row">
            <div class="col-lg-8">
              <PubChiffreAffaire />
            </div>
            <div class="col-lg-4">
              <PubPaiementForm />
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script>
import AdminHeader from '@/components/AdminHeader.vue'
import AdminSidebar from '@/components/AdminSidebar.vue'
import PubChiffreAffaire from '../../components/admin/PubChiffreAffaire.vue'
import PubPaiementForm from '../../components/admin/PubPaiementForm.vue'

export default {
  name: 'AdminPubPage',
  components: {
    AdminHeader,
    AdminSidebar,
    PubChiffreAffaire,
    PubPaiementForm
  }
}
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.admin-main {
  margin-left: 280px;
  flex: 1;
  padding-top: 80px;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.container-fluid {
  max-width: 1400px;
}

h1 {
  color: #2c3e50;
  font-weight: 700;
  font-size: 2.5rem;
  margin-bottom: 2rem;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
  background: linear-gradient(45deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Responsive */
@media (max-width: 991px) {
  .admin-main {
    margin-left: 0;
    padding-top: 60px;
  }
  
  h1 {
    font-size: 2rem;
  }
}

@media (max-width: 768px) {
  h1 {
    font-size: 1.5rem;
  }
}
</style>
