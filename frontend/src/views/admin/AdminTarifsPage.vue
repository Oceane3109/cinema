<template>
  <div class="admin-tarifs-page">
    <!-- Header Admin -->
    <AdminHeader />
    
    <!-- Contenu principal -->
    <div class="container-fluid">
      <div class="row">
        <!-- Contenu principal sans sidebar -->
        <main class="col-12 px-md-4">
          <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h1 class="h2 fw-bold">
              <i class="bi bi-tag me-2 text-primary"></i>
              Gestion des Tarifs
            </h1>
            <div class="btn-toolbar mb-2 mb-md-0">
              <div class="btn-group me-2">
                <button type="button" class="btn btn-sm btn-outline-secondary">
                  <i class="bi bi-download me-1"></i>
                  Exporter
                </button>
              </div>
            </div>
          </div>

          <!-- Statistiques -->
          <div class="row mb-4">
            <div class="col-xl-3 col-md-6 mb-4">
              <div class="card border-0 shadow-sm h-100">
                <div class="card-body">
                  <div class="d-flex align-items-center">
                    <div class="flex-grow-1">
                      <div class="text-uppercase text-primary fw-bold small mb-1">
                        Total tarifs
                      </div>
                      <div class="h4 mb-0 fw-bold text-gray-800">
                        {{ totalTarifs }}
                      </div>
                    </div>
                    <div class="ms-3">
                      <div class="icon-box bg-primary bg-opacity-10 text-primary rounded-3 p-3">
                        <i class="bi bi-tag fs-4"></i>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="col-xl-3 col-md-6 mb-4">
              <div class="card border-0 shadow-sm h-100">
                <div class="card-body">
                  <div class="d-flex align-items-center">
                    <div class="flex-grow-1">
                      <div class="text-uppercase text-success fw-bold small mb-1">
                        Tarifs actifs
                      </div>
                      <div class="h4 mb-0 fw-bold text-gray-800">
                        {{ tarifsActifs }}
                      </div>
                    </div>
                    <div class="ms-3">
                      <div class="icon-box bg-success bg-opacity-10 text-success rounded-3 p-3">
                        <i class="bi bi-check-circle fs-4"></i>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="col-xl-3 col-md-6 mb-4">
              <div class="card border-0 shadow-sm h-100">
                <div class="card-body">
                  <div class="d-flex align-items-center">
                    <div class="flex-grow-1">
                      <div class="text-uppercase text-info fw-bold small mb-1">
                        Prix moyen
                      </div>
                      <div class="h4 mb-0 fw-bold text-gray-800">
                        {{ formatPrix(prixMoyen) }}€
                      </div>
                    </div>
                    <div class="ms-3">
                      <div class="icon-box bg-info bg-opacity-10 text-info rounded-3 p-3">
                        <i class="bi bi-currency-euro fs-4"></i>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="col-xl-3 col-md-6 mb-4">
              <div class="card border-0 shadow-sm h-100">
                <div class="card-body">
                  <div class="d-flex align-items-center">
                    <div class="flex-grow-1">
                      <div class="text-uppercase text-warning fw-bold small mb-1">
                        Types de tarifs
                      </div>
                      <div class="h4 mb-0 fw-bold text-gray-800">
                        {{ typesTarifs }}
                      </div>
                    </div>
                    <div class="ms-3">
                      <div class="icon-box bg-warning bg-opacity-10 text-warning rounded-3 p-3">
                        <i class="bi bi-list-ul fs-4"></i>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Composant AdminTarifs -->
          <AdminTarifs ref="adminTarifs" />
        </main>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import AdminHeader from '../../components/AdminHeader.vue'
import AdminTarifs from '../../components/admin/AdminTarifs.vue'

export default {
  name: 'AdminTarifsPage',
  components: {
    AdminHeader,
    AdminTarifs
  },
  setup() {
    const tarifs = ref([])
    const loading = ref(true)

    const totalTarifs = computed(() => tarifs.value.length)
    
    const tarifsActifs = computed(() => {
      return tarifs.value.filter(tarif => tarif.actif).length
    })
    
    const prixMoyen = computed(() => {
      if (tarifs.value.length === 0) return 0
      const total = tarifs.value.reduce((sum, tarif) => sum + tarif.prix, 0)
      return total / tarifs.value.length
    })
    
    const typesTarifs = computed(() => {
      const types = new Set(tarifs.value.map(tarif => tarif.type))
      return types.size
    })

    const loadTarifs = async () => {
      try {
        const response = await fetch('/api/tarifs')
        const data = await response.json()
        tarifs.value = data
      } catch (error) {
        console.error('Erreur lors du chargement des tarifs:', error)
      } finally {
        loading.value = false
      }
    }

    const formatPrix = (prix) => {
      return new Intl.NumberFormat('fr-FR', { minimumFractionDigits: 2 }).format(prix)
    }

    onMounted(() => {
      loadTarifs()
    })

    return {
      tarifs,
      loading,
      totalTarifs,
      tarifsActifs,
      prixMoyen,
      typesTarifs,
      formatPrix
    }
  }
}
</script>

<style scoped>
.admin-tarifs-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
}

/* Cartes de statistiques modernes */
.card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  background-color: var(--bg-primary);
  transition: all 0.15s ease-in-out;
}

.card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.card-body {
  padding: 1.5rem;
}

.icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: var(--radius-lg);
  font-size: 1.5rem;
  transition: all 0.15s ease-in-out;
}

.icon-box:hover {
  transform: scale(1.05);
}

.text-gray-800 {
  color: var(--text-primary) !important;
}

.text-primary {
  color: var(--primary-color) !important;
}

.text-success {
  color: var(--success-color) !important;
}

.text-info {
  color: var(--info-color) !important;
}

.text-warning {
  color: var(--warning-color) !important;
}

.bg-primary {
  background-color: var(--primary-color) !important;
}

.bg-success {
  background-color: var(--success-color) !important;
}

.bg-info {
  background-color: var(--info-color) !important;
}

.bg-warning {
  background-color: var(--warning-color) !important;
}

.bg-opacity-10 {
  opacity: 0.1 !important;
}

.rounded-3 {
  border-radius: var(--radius-xl) !important;
}

.small {
  font-size: 0.875rem;
}

.text-uppercase {
  text-transform: uppercase !important;
}

.fw-bold {
  font-weight: 700 !important;
}

.h4 {
  font-size: 1.5rem;
  font-weight: 700;
}

.fs-4 {
  font-size: 1.5rem;
}

.flex-grow-1 {
  flex-grow: 1 !important;
}

.ms-3 {
  margin-left: 1rem !important;
}

.d-flex {
  display: flex !important;
}

.align-items-center {
  align-items: center !important;
}

.border-bottom {
  border-bottom: 1px solid var(--border-color) !important;
}

.pt-3 {
  padding-top: 1rem !important;
}

.pb-2 {
  padding-bottom: 0.5rem !important;
}

.mb-3 {
  margin-bottom: 1rem !important;
}

.mb-4 {
  margin-bottom: 1.5rem !important;
}

.mb-2 {
  margin-bottom: 0.5rem !important;
}

.mb-md-0 {
  margin-bottom: 0 !important;
}

.justify-content-between {
  justify-content: space-between !important;
}

.flex-wrap {
  flex-wrap: wrap !important;
}

.flex-md-nowrap {
  flex-wrap: nowrap !important;
}

.align-items-center {
  align-items: center !important;
}

.h2 {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  font-weight: 700;
  line-height: 1.2;
}

.me-2 {
  margin-right: 0.5rem !important;
}

.me-1 {
  margin-right: 0.25rem !important;
}

.btn-toolbar {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.btn-group {
  position: relative;
  display: inline-flex;
  vertical-align: middle;
}

.btn-group > .btn:not(:last-child):not(.dropdown-toggle) {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.btn-group > .btn:nth-child(n + 3),
.btn-group > :not(.btn-check) + .btn {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
  border-radius: var(--radius-md);
}

.btn-outline-secondary {
  color: var(--text-secondary);
  border-color: var(--border-color);
  background-color: transparent;
}

.btn-outline-secondary:hover {
  background-color: var(--secondary-color);
  border-color: var(--secondary-color);
  color: white;
}

.btn-primary {
  color: white;
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

.btn-primary:hover {
  background-color: var(--primary-dark);
  border-color: var(--primary-dark);
  color: white;
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.container-fluid {
  width: 100%;
  padding-right: 15px;
  padding-left: 15px;
  margin-right: auto;
  margin-left: auto;
}

.px-md-4 {
  padding-right: 1.5rem !important;
  padding-left: 1.5rem !important;
}

@media (min-width: 768px) {
  .px-md-4 {
    padding-right: 1.5rem !important;
    padding-left: 1.5rem !important;
  }
}

.col,
.col-xl-3,
.col-md-6,
.col-auto,
.col-lg-10,
.col-md-9,
.col-sm-auto,
.col-12 {
  position: relative;
  width: 100%;
  padding-right: 15px;
  padding-left: 15px;
}

.col-xl-3 {
  flex: 0 0 25%;
  max-width: 25%;
}

.col-md-6 {
  flex: 0 0 50%;
  max-width: 50%;
}

.col-auto {
  flex: 0 0 auto;
  width: auto;
  max-width: 100%;
}

.col-lg-10 {
  flex: 0 0 83.333333%;
  max-width: 83.333333%;
}

.col-md-9 {
  flex: 0 0 75%;
  max-width: 75%;
}

.col-12 {
  flex: 0 0 100%;
  max-width: 100%;
}

@media (max-width: 767.98px) {
  .col-md-6 {
    flex: 0 0 50%;
    max-width: 50%;
  }
}

.row {
  display: flex;
  flex-wrap: wrap;
  margin-right: -15px;
  margin-left: -15px;
}

.h-100 {
  height: 100% !important;
}

.py-2 {
  padding-top: 0.5rem !important;
  padding-bottom: 0.5rem !important;
}

.mr-2 {
  margin-right: 0.5rem !important;
}

.no-gutters {
  margin-right: 0;
  margin-left: 0;
}

.align-items-center {
  align-items: center !important;
}
</style>
