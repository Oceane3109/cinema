<template>
  <div class="admin-chiffre-affaires-page">
    <!-- Header Admin -->
    <AdminHeader />
    
    <!-- Contenu principal -->
    <div class="container-fluid">
      <div class="row">
        <!-- Contenu principal sans sidebar -->
        <main class="col-12 px-md-4">
          <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h1 class="h2 fw-bold">
              <i class="bi bi-graph-up me-2 text-primary"></i>
              Chiffre d'Affaires
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

          <!-- Composant AdminChiffreAffaires -->
          <AdminChiffreAffaires ref="adminChiffreAffaires" :stats-generales="statsGenerales" />
        </main>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import AdminHeader from '../../components/AdminHeader.vue'
import AdminChiffreAffaires from '../../components/admin/AdminChiffreAffaires.vue'

export default {
  name: 'AdminChiffreAffairesPage',
  components: {
    AdminHeader,
    AdminChiffreAffaires
  },
  setup() {
    const chiffreAffaires = ref([])
    const loading = ref(true)

    const statsGenerales = computed(() => {
      console.log('Calcul des statsGenerales avec chiffreAffaires:', chiffreAffaires.value)
      
      if (!chiffreAffaires.value || !Array.isArray(chiffreAffaires.value) || chiffreAffaires.value.length === 0) {
        console.log('Pas de données, retour des stats par défaut')
        return {
          totalChiffreAffaire: 0,
          totalEntrees: 0,
          tarifMoyen: 0,
          nombreFilms: 0
        }
      }
      
      // Utiliser les champs retournés par l'API /statistiques-generales
      const data = chiffreAffaires.value[0] // Prendre le premier (et seul) élément
      const stats = {
        totalChiffreAffaire: data.totalChiffreAffaire || 0,
        totalEntrees: data.totalEntrees || 0,
        tarifMoyen: data.tarifMoyen || 0,
        nombreFilms: data.nombreFilms || 0
      }
      
      console.log('Stats calculées:', stats)
      return stats
    })

    const loadChiffreAffaires = async () => {
      loading.value = true
      try {
        // Utiliser l'endpoint des statistiques générales qui calcule le CA réel depuis les réservations
        // Période large pour inclure toutes les réservations existantes
        const response = await fetch('/api/chiffre-affaires/statistiques-generales?periode=ANNEEL&dateDebut=2020-01-01&dateFin=2030-12-31')
        if (!response.ok) {
          console.error('Erreur HTTP:', response.status, response.statusText)
          chiffreAffaires.value = []
          return
        }
        
        const data = await response.json()
        console.log('Données des statistiques générales reçues dans AdminChiffreAffairesPage:', data)
        console.log('Type de données:', typeof data)
        
        // Les statistiques générales retournent un objet direct, pas un tableau
        // On le met dans un tableau pour compatibilité avec le composant
        chiffreAffaires.value = Array.isArray(data) ? data : [data]
        console.log('chiffreAffaires.value après assignation:', chiffreAffaires.value)
      } catch (error) {
        console.error('Erreur lors du chargement du chiffre d\'affaires:', error)
        chiffreAffaires.value = []
      } finally {
        loading.value = false
      }
    }

    const formatPrix = (prix) => {
      return new Intl.NumberFormat('fr-FR', { minimumFractionDigits: 2 }).format(prix)
    }

    onMounted(() => {
      loadChiffreAffaires()
    })

    return {
      chiffreAffaires,
      loading,
      statsGenerales,
      loadChiffreAffaires,
      formatPrix
    }
  }
}
</script>

<style scoped>
.admin-chiffre-affaires-page {
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
  color: gray;
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
