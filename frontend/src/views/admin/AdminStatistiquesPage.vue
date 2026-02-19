<template>
  <AdminLayout>
    <main class="container-fluid py-4">
    <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="h3 mb-0">
          <i class="bi bi-bar-chart-line me-3"></i>
          Statistiques Administrateur
        </h1>
        <p class="text-muted mb-0">Tableau de bord et analyses de performance</p>
      </div>
      <div class="d-flex gap-2">
        <select v-model="periode" class="form-select form-select-sm" style="width: 150px;">
                  <option value="JOURNALIER">Journalier</option>
                  <option value="HEBDOMADAIRE">Hebdomadaire</option>
                  <option value="MENSUEL">Mensuel</option>
                  <option value="ANNUEL">Annuel</option>
                </select>
                <button @click="refreshStats" class="btn btn-primary btn-sm">
                  <i class="bi bi-arrow-clockwise me-1"></i>
                  Actualiser
                </button>
              </div>
            </div>

        <!-- Cartes de statistiques principales -->
        <div class="row mb-4">
          <div class="col-md-3 mb-3">
            <div class="card stat-card-primary">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <h6 class="stat-title">Total Entrées</h6>
                    <h3 class="stat-number">{{ formatNombre(stats.totalEntrees || 0) }}</h3>
                    <div class="stat-trend positive">
                      <i class="bi bi-arrow-up"></i> +{{ stats.croissanceReservations || 0 }}% ce mois
                    </div>
                  </div>
                  <div class="stat-icon">
                    <i class="bi bi-ticket-perforated"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-md-3 mb-3">
            <div class="card stat-card-success">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <h6 class="stat-title">Chiffre d'Affaires</h6>
                    <h3 class="stat-number">{{ formatPrix(stats.totalCA) }}</h3>
                    <div class="stat-trend positive">
                      <i class="bi bi-arrow-up"></i> +{{ stats.croissanceCA }}% ce mois
                    </div>
                  </div>
                  <div class="stat-icon">
                    <i class="bi bi-currency-euro"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-md-3 mb-3">
            <div class="card stat-card-info">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <h6 class="stat-title">Films Actifs</h6>
                    <h3 class="stat-number">{{ formatNombre(stats.filmsActifs) }}</h3>
                    <div class="stat-trend positive">
                      <i class="bi bi-arrow-up"></i> +{{ stats.nouveauxFilms }} ce mois
                    </div>
                  </div>
                  <div class="stat-icon">
                    <i class="bi bi-film"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-md-3 mb-3">
            <div class="card stat-card-warning">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <h6 class="stat-title">Taux Occupation</h6>
                    <h3 class="stat-number">{{ stats.tauxOccupation }}%</h3>
                    <div class="stat-trend positive">
                      <i class="bi bi-arrow-up"></i> +{{ stats.croissanceOccupation }}% ce mois
                    </div>
                  </div>
                  <div class="stat-icon">
                    <i class="bi bi-door-open"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Graphiques -->
        <div class="row mb-4">
          <!-- Graphique d'évolution du CA -->
          <div class="col-md-8 mb-3">
            <div class="card">
              <div class="card-header d-flex justify-content-between align-items-center">
                <h6 class="mb-0">
                  <i class="bi bi-graph-up me-2"></i>
                  Évolution du Chiffre d'Affaires
                </h6>
                <div class="btn-group btn-group-sm" role="group">
                  <button 
                    v-for="p in ['JOURNALIER', 'HEBDOMADAIRE', 'MENSUEL']" 
                    :key="p"
                    @click="periodeGraph = p"
                    :class="['btn', periodeGraph === p ? 'btn-primary' : 'btn-outline-secondary']"
                  >
                    {{ p.charAt(0) + p.slice(1).toLowerCase() }}
                  </button>
                </div>
              </div>
              <div class="card-body">
                <canvas ref="caChart"></canvas>
              </div>
            </div>
          </div>

          <!-- Répartition par genre -->
          <div class="col-md-4 mb-3">
            <div class="card">
              <div class="card-header">
                <h6 class="mb-0">
                  <i class="bi bi-pie-chart me-2"></i>
                  Répartition par Genre
                </h6>
              </div>
              <div class="card-body">
                <canvas ref="genreChart"></canvas>
              </div>
            </div>
          </div>
        </div>

        <!-- Films les plus populaires -->
        <div class="row mb-4">
          <div class="col-md-6 mb-3">
            <div class="card">
              <div class="card-header">
                <h6 class="mb-0">
                  <i class="bi bi-trophy me-2"></i>
                  Films les Plus Populaires
                </h6>
              </div>
              <div class="card-body">
                <div class="table-responsive">
                  <table class="table table-hover">
                    <thead class="table-light">
                      <tr>
                        <th>Film</th>
                        <th>Réservations</th>
                        <th>CA</th>
                        <th>Note</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="film in topFilms" :key="film.id">
                        <td>
                          <div class="d-flex align-items-center">
                            <div class="film-thumbnail me-2">
                              <img :src="film.image && film.image.startsWith('/') ? film.image : '/placeholder-film.jpg'" 
                                   class="img-fluid rounded" style="width: 40px; height: 60px; object-fit: cover;"
                                   @error="handleImageError">
                            </div>
                            <div>
                              <div class="fw-semibold">{{ film.titre }}</div>
                              <small class="text-muted">{{ film.genre || 'Non spécifié' }}</small>
                            </div>
                          </div>
                        </td>
                        <td>
                          <span class="badge bg-primary">{{ formatNombre(film.reservations) }}</span>
                        </td>
                        <td class="fw-semibold">{{ formatPrix(film.ca) }}</td>
                        <td>
                          <div class="d-flex align-items-center">
                            <span class="badge bg-warning text-dark me-1">⭐</span>
                            <span>{{ film.note }}/5</span>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>

          <!-- Salles les plus rentables -->
          <div class="col-md-6 mb-3">
            <div class="card">
              <div class="card-header">
                <h6 class="mb-0">
                  <i class="bi bi-building me-2"></i>
                  Salles les Plus Rentables
                </h6>
              </div>
              <div class="card-body">
                <div class="table-responsive">
                  <table class="table table-hover">
                    <thead class="table-light">
                      <tr>
                        <th>Salle</th>
                        <th>Capacité</th>
                        <th>Taux Occupation</th>
                        <th>CA Mensuel</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="salle in topSalles" :key="salle.id">
                        <td>
                          <div class="d-flex align-items-center">
                            <div class="badge bg-primary me-2">{{ salle.type }}</div>
                            <div>
                              <div class="fw-semibold">{{ salle.nom }}</div>
                              <small class="text-muted">{{ salle.capacite }} places</small>
                            </div>
                          </div>
                        </td>
                        <td>{{ formatNombre(salle.capacite) }}</td>
                        <td>
                          <div class="progress progress-sm">
                            <div class="progress-bar" 
                                 :class="getProgressBarClass(salle.tauxOccupation)"
                                 :style="`width: ${salle.tauxOccupation}%`">
                              {{ salle.tauxOccupation }}%
                            </div>
                          </div>
                        </td>
                        <td class="fw-semibold">{{ formatPrix(salle.caMensuel) }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Statistiques horaires -->
        <div class="row mb-4">
          <div class="col-12">
            <div class="card">
              <div class="card-header">
                <h6 class="mb-0">
                  <i class="bi bi-clock-history me-2"></i>
                  Répartition des Réservations par Heure
                </h6>
              </div>
              <div class="card-body">
                <canvas ref="horaireChart"></canvas>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>2

    <!-- Footer discret -->
    <footer class="footer mt-auto py-3 bg-light border-top">
      <div class="container-fluid px-4">
        <div class="row align-items-center">
          <div class="col-md-6">
            <small class="text-muted">
              <i class="bi bi-c-circle me-1"></i>
              2024 Cinéma Management - Tableau de bord administrateur
            </small>
          </div>
          <div class="col-md-6 text-end">
            <small class="text-muted">
              <i class="bi bi-shield-check me-1"></i>
              Version sécurisée
            </small>
          </div>
        </div>
        </div>
      </footer>
    </AdminLayout>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { Chart, registerables } from 'chart.js'

// Enregistrer tous les composants Chart.js
Chart.register(...registerables)

export default {
  name: 'AdminStatistiquesPage',
  components: {
    AdminLayout
  },
  setup() {
    const periode = ref('MENSUEL')
    const periodeGraph = ref('MENSUEL')
    const loading = ref(false)
    
    const stats = ref({
      totalReservations: 0,
      totalCA: 0,
      filmsActifs: 0,
      tauxOccupation: 0,
      croissanceReservations: 0,
      croissanceCA: 0,
      nouveauxFilms: 0,
      croissanceOccupation: 0
    })

    const topFilms = ref([])
    const topSalles = ref([])

    const caChart = ref(null)
    const genreChart = ref(null)
    const horaireChart = ref(null)

    const formatNombre = (nombre) => {
      return new Intl.NumberFormat('fr-FR').format(nombre)
    }

    const formatPrix = (prix) => {
      return new Intl.NumberFormat('fr-FR', { 
        style: 'currency', 
        currency: 'EUR' 
      }).format(prix)
    }

    const evolutionData = ref([])
    const genreData = ref([])
    const horaireData = ref([])

    const refreshStats = async () => {
      loading.value = true
      try {
        // Récupérer les dates selon la période sélectionnée
        let dateDebut, dateFin
        
        switch (periode.value) {
          case 'JOURNALIER':
            dateDebut = new Date().toISOString().split('T')[0]
            dateFin = new Date().toISOString().split('T')[0]
            break
          case 'HEBDOMADAIRE':
            const debutSemaine = new Date()
            debutSemaine.setDate(debutSemaine.getDate() - debutSemaine.getDay())
            dateDebut = debutSemaine.toISOString().split('T')[0]
            dateFin = new Date().toISOString().split('T')[0]
            break
          case 'MENSUEL':
            dateDebut = new Date(new Date().getFullYear(), new Date().getMonth(), 1)
              .toISOString().split('T')[0]
            dateFin = new Date().toISOString().split('T')[0]
            break
          case 'ANNUEL':
            dateDebut = new Date(new Date().getFullYear(), 0, 1)
              .toISOString().split('T')[0]
            dateFin = new Date().toISOString().split('T')[0]
            break
          default:
            dateDebut = new Date(new Date().getFullYear(), new Date().getMonth(), 1)
              .toISOString().split('T')[0]
            dateFin = new Date().toISOString().split('T')[0]
        }
        
        console.log('Période:', periode.value, 'Date début:', dateDebut, 'Date fin:', dateFin)
        
        // Charger les statistiques principales
        const statsResponse = await fetch(`/api/statistiques/dashboard?periode=${periode.value}&dateDebut=${dateDebut}&dateFin=${dateFin}`)
        if (statsResponse.ok) {
          const statsData = await statsResponse.json()
          stats.value = statsData
          console.log('Statistiques chargées:', statsData)
        }

        // Charger les top films
        const filmsResponse = await fetch(`/api/statistiques/top-films?dateDebut=${dateDebut}&dateFin=${dateFin}`)
        if (filmsResponse.ok) {
          const filmsData = await filmsResponse.json()
          topFilms.value = filmsData
          console.log('Top films chargés:', filmsData)
        }

        // Charger les top salles
        const sallesResponse = await fetch(`/api/statistiques/top-salles?periode=${periode.value}&dateDebut=${dateDebut}&dateFin=${dateFin}`)
        if (sallesResponse.ok) {
          const sallesData = await sallesResponse.json()
          topSalles.value = sallesData
          console.log('Top salles chargées:', sallesData)
        }

        // Charger la répartition par genre
        const genreResponse = await fetch(`/api/statistiques/repartition-genre?dateDebut=${dateDebut}&dateFin=${dateFin}`)
        if (genreResponse.ok) {
          const genreDataResponse = await genreResponse.json()
          genreData.value = genreDataResponse
          console.log('Répartition genre chargée:', genreDataResponse)
          updateGenreChart()
        }

        // Charger l'évolution du CA
        const evolutionResponse = await fetch(`/api/statistiques/evolution-ca?periode=${periode.value}&dateDebut=${dateDebut}&dateFin=${dateFin}`)
        if (evolutionResponse.ok) {
          const evolutionDataResponse = await evolutionResponse.json()
          evolutionData.value = evolutionDataResponse
          console.log('Évolution CA chargée:', evolutionDataResponse)
          updateCAChart()
        }
        
        // Mettre à jour le graphique horaire
        updateHoraireChart()
        
        console.log('Statistiques rafraîchies avec succès')
      } catch (error) {
        console.error('Erreur lors du rafraîchissement des statistiques:', error)
      } finally {
        loading.value = false
      }
    }

    const initCharts = () => {
      // Graphique d'évolution du CA
      initCAChart()
      
      // Graphique par genre
      initGenreChart()
      
      // Graphique horaire
      initHoraireChart()
    }

    const initCAChart = () => {
      if (caChart.value) {
        // Détruire le graphique existant s'il y en a un
        if (caChart.value.chart) {
          caChart.value.chart.destroy()
        }
        
        const ctx = caChart.value.getContext('2d')
        caChart.value.chart = new Chart(ctx, {
          type: 'line',
          data: {
            labels: [],
            datasets: [{
              label: 'Chiffre d\'Affaires (€)',
              data: [],
              borderColor: 'rgb(13, 110, 253)',
              backgroundColor: 'rgba(13, 110, 253, 0.1)',
              tension: 0.4,
              fill: true
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: 'top',
              },
              title: {
                display: true,
                text: 'Évolution du Chiffre d\'Affaires'
              }
            },
            scales: {
              y: {
                beginAtZero: true,
                ticks: {
                  callback: function(value) {
                    return value + ' €'
                  }
                }
              }
            }
          }
        })
      }
    }

    const updateCAChart = () => {
      if (caChart.value && caChart.value.chart && evolutionData.value.length > 0) {
        const chart = caChart.value.chart
        chart.data.labels = evolutionData.value.map(item => {
          const date = new Date(item.date)
          return date.toLocaleDateString('fr-FR', { month: 'short', day: 'numeric' })
        })
        chart.data.datasets[0].data = evolutionData.value.map(item => Number(item.ca))
        chart.update()
      }
    }

    const initGenreChart = () => {
      if (genreChart.value) {
        if (genreChart.value.chart) {
          genreChart.value.chart.destroy()
        }
        
        const ctx = genreChart.value.getContext('2d')
        genreChart.value.chart = new Chart(ctx, {
          type: 'doughnut',
          data: {
            labels: [],
            datasets: [{
              data: [],
              backgroundColor: [
                'rgba(13, 110, 253, 0.8)',
                'rgba(25, 135, 84, 0.8)',
                'rgba(255, 193, 7, 0.8)',
                'rgba(220, 53, 69, 0.8)',
                'rgba(108, 117, 125, 0.8)',
                'rgba(253, 126, 20, 0.8)'
              ],
              borderWidth: 2,
              borderColor: '#fff'
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: 'bottom',
              },
              title: {
                display: true,
                text: 'Répartition par Genre'
              }
            }
          }
        })
      }
    }

    const updateGenreChart = () => {
      if (genreChart.value && genreChart.value.chart && genreData.value.length > 0) {
        const chart = genreChart.value.chart
        chart.data.labels = genreData.value.map(item => item.genre)
        chart.data.datasets[0].data = genreData.value.map(item => item.pourcentage)
        chart.update()
      }
    }

    const initHoraireChart = () => {
      if (horaireChart.value) {
        if (horaireChart.value.chart) {
          horaireChart.value.chart.destroy()
        }
        
        const ctx = horaireChart.value.getContext('2d')
        horaireChart.value.chart = new Chart(ctx, {
          type: 'bar',
          data: {
            labels: [],
            datasets: [{
              label: 'Réservations',
              data: [],
              backgroundColor: 'rgba(13, 110, 253, 0.8)',
              borderColor: 'rgb(13, 110, 253)',
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                display: false
              },
              title: {
                display: true,
                text: 'Répartition des Réservations par Heure'
              }
            },
            scales: {
              y: {
                beginAtZero: true,
                ticks: {
                  stepSize: 1
                }
              }
            }
          }
        })
      }
    }

    const updateHoraireChart = () => {
      // Pour l'instant, utiliser des données simulées basées sur les réservations réelles
      if (horaireChart.value && horaireChart.value.chart) {
        const chart = horaireChart.value.chart
        
        // Simuler la répartition horaire basée sur le nombre total de réservations
        const totalReservations = stats.value.totalReservations || 1
        const heures = ['10h', '12h', '14h', '16h', '18h', '20h', '22h']
        const distribution = [
          Math.floor(totalReservations * 0.05), // 10h
          Math.floor(totalReservations * 0.08), // 12h
          Math.floor(totalReservations * 0.15), // 14h
          Math.floor(totalReservations * 0.12), // 16h
          Math.floor(totalReservations * 0.18), // 18h
          Math.floor(totalReservations * 0.25), // 20h
          Math.floor(totalReservations * 0.17)  // 22h
        ]
        
        chart.data.labels = heures
        chart.data.datasets[0].data = distribution
        chart.update()
      }
    }

    const getProgressBarClass = (taux) => {
      if (taux >= 80) return 'bg-success'
      if (taux >= 60) return 'bg-warning'
      return 'bg-danger'
    }

    const handleImageError = (event) => {
      event.target.src = '/placeholder-film.jpg'
    }

    onMounted(() => {
      refreshStats()
      initCharts()
    })

    watch(periodeGraph, () => {
      refreshStats()
    })

    watch(periode, () => {
      refreshStats()
    })

    watch(() => stats.value.totalReservations, () => {
      updateHoraireChart()
    })

    return {
      periode,
      periodeGraph,
      loading,
      stats,
      topFilms,
      topSalles,
      evolutionData,
      genreData,
      horaireData,
      caChart,
      genreChart,
      horaireChart,
      formatNombre,
      formatPrix,
      refreshStats,
      getProgressBarClass,
      handleImageError,
      updateCAChart,
      updateGenreChart,
      updateHoraireChart
    }
  }
}
</script>

<style scoped>
/* Styles pour les pages admin */
.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.page-subtitle {
  color: #6c757d;
  margin-bottom: 0;
  font-size: 1.1rem;
}

/* Cartes de statistiques */
.stat-card-primary {
  border-left: 4px solid #0d6efd;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
}

.stat-card-success {
  border-left: 4px solid #198754;
  background: linear-gradient(135deg, #ffffff 0%, #f8fff9 100%);
}

.stat-card-info {
  border-left: 4px solid #0dcaf0;
  background: linear-gradient(135deg, #ffffff 0%, #f8ffff 100%);
}

.stat-card-warning {
  border-left: 4px solid #ffc107;
  background: linear-gradient(135deg, #ffffff 0%, #fffef8 100%);
}

.stat-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #6c757d;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.5rem;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.stat-trend {
  font-size: 0.875rem;
  font-weight: 500;
}

.stat-trend.positive {
  color: #198754;
}

.stat-trend.negative {
  color: #dc3545;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

.stat-card-primary .stat-icon {
  background: linear-gradient(135deg, #0d6efd, #0056b3);
}

.stat-card-success .stat-icon {
  background: linear-gradient(135deg, #198754, #145732);
}

.stat-card-info .stat-icon {
  background: linear-gradient(135deg, #0dcaf0, #0aa2c0);
}

.stat-card-warning .stat-icon {
  background: linear-gradient(135deg, #ffc107, #e0a800);
}

/* Cartes */
.card {
  border: none;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
  border-radius: 0.75rem;
  transition: all 0.3s ease;
}

.card:hover {
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
  border-radius: 0.75rem 0.75rem 0 0;
  font-weight: 600;
  color: #495057;
}

/* Tableaux */
.table th {
  border-top: none;
  font-weight: 600;
  color: #495057;
  background-color: #f8f9fa;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 0, 0, 0.025);
}

.film-thumbnail img {
  border: 2px solid #dee2e6;
  transition: border-color 0.3s ease;
}

.film-thumbnail img:hover {
  border-color: #0d6efd;
}

/* Progress bars */
.progress {
  background-color: #e9ecef;
  border-radius: 0.5rem;
}

.progress-bar {
  background-color: #0d6efd;
  border-radius: 0.5rem;
  transition: width 0.6s ease;
}

.progress-sm {
  height: 0.5rem;
}

/* Badges */
.badge {
  font-size: 0.75em;
  font-weight: 500;
}

/* Graphiques placeholders */
.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 0.5rem;
  border: 2px dashed #dee2e6;
}

.chart-placeholder-content {
  text-align: center;
  color: #6c757d;
}

.chart-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.6;
}

.chart-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.chart-subtitle {
  font-size: 0.9rem;
  margin-bottom: 0;
  opacity: 0.8;
}

/* Responsive */
@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }
  
  .stat-number {
    font-size: 1.5rem;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
    font-size: 1.25rem;
  }
}

@media (max-width: 576px) {
  .page-title {
    font-size: 1.75rem;
  }
  
  .stat-number {
    font-size: 1.25rem;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }
  
  .chart-placeholder {
    height: 200px;
  }
  
  .chart-icon {
    font-size: 2rem;
  }
}

/* Animations */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card {
  animation: fadeIn 0.6s ease-out;
}

.card:nth-child(1) { animation-delay: 0.1s; }
.card:nth-child(2) { animation-delay: 0.2s; }
.card:nth-child(3) { animation-delay: 0.3s; }
.card:nth-child(4) { animation-delay: 0.4s; }
</style>
