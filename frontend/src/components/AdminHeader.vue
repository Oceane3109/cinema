<template>
  <nav class="admin-navbar navbar navbar-expand-lg navbar-dark shadow-lg fixed-top">
    <div class="container-fluid px-4">
      <!-- Logo et nom -->
      <router-link to="/admin/reservations" class="navbar-brand d-flex align-items-center text-decoration-none">
        <div class="brand-container">
          <div class="brand-icon-wrapper">
            <i class="bi bi-shield-check brand-icon"></i>
            <div class="brand-glow"></div>
          </div>
          <div class="brand-text">
            <span class="brand-title">Admin</span>
            <span class="brand-subtitle">Cinéma</span>
          </div>
        </div>
      </router-link>

      <!-- Bouton mobile -->
      <button class="navbar-toggler border-0 shadow-none" type="button" data-bs-toggle="collapse" data-bs-target="#adminNavbar">
        <span class="navbar-toggler-icon"></span>
      </button>

      <!-- Menu de navigation -->
      <div class="collapse navbar-collapse" id="adminNavbar">
        <!-- Section gauche - Navigation principale -->
        <ul class="navbar-nav me-auto">
          <li class="nav-item mx-1">
            <router-link to="/" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/' }">
              <i class="bi bi-house-door me-2"></i>
              <span class="nav-text">Site Public</span>
              <div class="nav-indicator" v-if="$route.path === '/'"></div>
            </router-link>
          </li>
        </ul>

        <!-- Section droite - Administration -->
        <ul class="navbar-nav">
          <li class="nav-item">
            <div class="admin-divider me-3 d-none d-lg-block"></div>
          </li>

          <!-- Statistiques rapides -->
          <li class="nav-item dropdown mx-1">
            <ul class="dropdown-menu dropdown-menu-end shadow-lg border-0" aria-labelledby="statsDropdown">
              <li><h6 class="dropdown-header text-primary">📊 Aujourd'hui</h6></li>
              <li><a class="dropdown-item" href="#"><i class="bi bi-ticket-perforated me-2"></i>Réservations: <strong>24</strong></a></li>
              <li><a class="dropdown-item" href="#"><i class="bi bi-cash me-2"></i>Chiffre d'affaires: <strong>480€</strong></a></li>
              <li><hr class="dropdown-divider"></li>
              <li><a class="dropdown-item text-center text-muted small" href="#">Voir détails →</a></li>
            </ul>
          </li>

          <!-- Gestion des films -->
          <li class="nav-item mx-1">
            <router-link to="/admin/films" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/admin/films' }">
              <i class="bi bi-film me-2"></i>
              <span class="nav-text">Films</span>
              <div class="nav-indicator" v-if="$route.path === '/admin/films'"></div>
            </router-link>
          </li>

          <!-- Gestion des salles -->
          <li class="nav-item mx-1">
            <router-link to="/admin/salles" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/admin/salles' }">
              <i class="bi bi-building me-2"></i>
              <span class="nav-text">Salles</span>
              <div class="nav-indicator" v-if="$route.path === '/admin/salles'"></div>
            </router-link>
          </li>

          <!-- Gestion des séances -->
          <li class="nav-item mx-1">
            <router-link to="/admin/seances" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/admin/seances' }">
              <i class="bi bi-calendar-event me-2"></i>
              <span class="nav-text">Séances</span>
              <div class="nav-indicator" v-if="$route.path === '/admin/seances'"></div>
            </router-link>
          </li>

          <!-- Gestion des réservations -->
          <li class="nav-item mx-1">
            <router-link to="/admin/reservations" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/admin/reservations' }">
              <i class="bi bi-ticket-perforated me-2"></i>
              <span class="nav-text">Réservations</span>
              <div class="nav-indicator" v-if="$route.path === '/admin/reservations'"></div>
              <div class="notification-badge" v-if="pendingReservations > 0">{{ pendingReservations }}</div>
            </router-link>
          </li>

          <!-- Gestion des tarifs -->
          <li class="nav-item mx-1">
            <router-link to="/admin/tarifs" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/admin/tarifs' }">
              <i class="bi bi-tag me-2"></i>
              <span class="nav-text">Tarifs</span>
              <div class="nav-indicator" v-if="$route.path === '/admin/tarifs'"></div>
            </router-link>
          </li>

          <!-- Gestion du chiffre d'affaires -->
          <li class="nav-item mx-1">
            <router-link to="/admin/chiffre-affaires" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/admin/chiffre-affaires' }">
              <i class="bi bi-graph-up me-2"></i>
              <span class="nav-text">Chiffre d'affaires</span>
              <div class="nav-indicator" v-if="$route.path === '/admin/chiffre-affaires'"></div>
            </router-link>
          </li>

          <!-- Produits Extras -->
          <li class="nav-item mx-1">
            <router-link to="/admin/produits-extras" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/admin/produits-extras' }">
              <i class="bi bi-basket me-2"></i>
              <span class="nav-text">Produits</span>
              <div class="nav-indicator" v-if="$route.path === '/admin/produits-extras'"></div>
            </router-link>
          </li>

          <!-- Vente Produits -->
          <li class="nav-item mx-1">
            <router-link to="/admin/vente-produits-extras" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/admin/vente-produits-extras' }">
              <i class="bi bi-cash-coin me-2"></i>
              <span class="nav-text">Ventes</span>
              <div class="nav-indicator" v-if="$route.path === '/admin/vente-produits-extras'"></div>
            </router-link>
          </li>

          <!-- Statistiques détaillées -->
          <li class="nav-item mx-1">
            <router-link to="/admin/statistiques" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/admin/statistiques' }">
              <i class="bi bi-bar-chart-line me-2"></i>
              <span class="nav-text">Statistiques</span>
              <div class="nav-indicator" v-if="$route.path === '/admin/statistiques'"></div>
            </router-link>
          </li>
            <!-- Gestion publicitaire -->
          <li class="nav-item mx-1">
            <router-link to="/admin/pub-gestion" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/admin/pub-gestion' }">
              <i class="bi bi-film me-2"></i>
              <span class="nav-text">Publicité</span>
              <div class="nav-indicator" v-if="$route.path === '/admin/pub-gestion'"></div>
            </router-link>
          </li>


          <!-- Actions utilisateur -->
          <li class="nav-item dropdown mx-1">
            <button class="nav-link px-3 py-3 rounded-pill dropdown-toggle" id="userDropdown" data-bs-toggle="dropdown">
              <i class="bi bi-person-circle me-2"></i>
              <span class="nav-text">Admin</span>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow-lg border-0" aria-labelledby="userDropdown">
              <li><h6 class="dropdown-header">👤 Administrateur</h6></li>
              <li><a class="dropdown-item" href="#"><i class="bi bi-gear me-2"></i>Paramètres</a></li>
              <li><a class="dropdown-item" href="#"><i class="bi bi-file-earmark-text me-2"></i>Rapports</a></li>
              <li><hr class="dropdown-divider"></li>
              <li><a class="dropdown-item text-danger" href="#"><i class="bi bi-box-arrow-right me-2"></i>Déconnexion</a></li>
            </ul>
          </li>
        </ul>
      </div>
    </div>

    <!-- Barre de progression de scroll -->
    <div class="scroll-progress">
      <div class="scroll-progress-bar"></div>
    </div>
  </nav>
</template>

<script>
export default {
  name: 'AdminHeader',
  data() {
    return {
      pendingReservations: 3, // À remplacer par une vraie valeur depuis l'API
      todayStats: {
        reservations: 24,
        revenue: 480
      }
    }
  },
  mounted() {
    // Animation de la barre de progression de scroll
    this.updateScrollProgress()

    window.addEventListener('scroll', this.updateScrollProgress)
  },
  beforeUnmount() {
    window.removeEventListener('scroll', this.updateScrollProgress)
  },
  methods: {
    updateScrollProgress() {
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop
      const scrollHeight = document.documentElement.scrollHeight - window.innerHeight
      const scrollPercent = (scrollTop / scrollHeight) * 100

      const progressBar = document.querySelector('.scroll-progress-bar')
      if (progressBar) {
        progressBar.style.width = scrollPercent + '%'
      }
    }
  }
}
</script>

<style scoped>
@import '../assets/app.css';

/* Navbar admin professionnelle */
.admin-navbar {
  background: linear-gradient(135deg, #1e3a8a 0%, #3730a3 50%, #581c87 100%) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.admin-navbar:hover {
  background: linear-gradient(135deg, #1e3a8a 0%, #3730a3 50%, #581c87 100%, rgba(0,0,0,0.1)) !important;
}

/* Logo amélioré */
.brand-container {
  display: flex;
  align-items: center;
  position: relative;
}

.brand-icon-wrapper {
  position: relative;
  width: 45px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  margin-right: 1rem;
  transition: all 0.3s ease;
}

.brand-icon-wrapper:hover {
  transform: scale(1.05);
  background: rgba(255, 255, 255, 0.25);
}

.brand-icon {
  font-size: 1.5rem;
  color: #ffffff;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
}

.brand-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 14px;
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4, #45b7d1, #96ceb4);
  background-size: 400% 400%;
  animation: glow 3s ease-in-out infinite;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.brand-icon-wrapper:hover .brand-glow {
  opacity: 0.6;
}

@keyframes glow {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.brand-title {
  font-weight: 700;
  font-size: 1.5rem;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
  letter-spacing: -0.5px;
}

.brand-subtitle {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 400;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* Navigation links */
.nav-link {
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9) !important;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border: 2px solid transparent;
}

.nav-link:hover {
  color: #ffffff !important;
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.nav-link.active {
  background: rgba(255, 255, 255, 0.15);
  color: #ffffff !important;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

.nav-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 3px;
  background: linear-gradient(90deg, #ff6b6b, #4ecdc4);
  border-radius: 2px;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from { width: 0; opacity: 0; }
  to { width: 30px; opacity: 1; }
}

/* Dropdown menus améliorés */
.dropdown-menu {
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1) !important;
  margin-top: 0.5rem !important;
}

.dropdown-item {
  transition: all 0.2s ease;
}

.dropdown-item:hover {
  background: rgba(102, 126, 234, 0.1) !important;
  color: #667eea !important;
}

/* Badge de statistiques */
.stats-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
  color: white;
  font-size: 0.7rem;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 600;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

/* Badge de notifications */
.notification-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #dc3545;
  color: white;
  font-size: 0.7rem;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 600;
  min-width: 18px;
  text-align: center;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-4px); }
  60% { transform: translateY(-2px); }
}

/* Séparateur admin */
.admin-divider {
  width: 2px;
  height: 40px;
  background: linear-gradient(to bottom, transparent, rgba(255,255,255,0.3), transparent);
  border-radius: 1px;
}

/* Scroll progress bar */
.scroll-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: rgba(255, 255, 255, 0.1);
}

.scroll-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #ff6b6b, #4ecdc4);
  width: 0%;
  transition: width 0.3s ease;
}

/* Espace pour le header fixe */
body.admin-page .main-content {
  padding-top: 80px;
}

/* Responsive */
@media (max-width: 991.98px) {
  .navbar-collapse {
    background: rgba(0, 0, 0, 0.95);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border-radius: 15px;
    margin-top: 1rem;
    padding: 1rem;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 8px 32px rgba(0,0,0,0.3);
  }

  .nav-text {
    display: none;
  }

  .navbar-nav .nav-item {
    margin: 0.25rem 0;
  }

  .brand-subtitle {
    display: none;
  }

  .admin-divider {
    display: none;
  }

  .dropdown-menu {
    background: rgba(0, 0, 0, 0.9) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
  }

  .dropdown-item {
    color: rgba(255, 255, 255, 0.9) !important;
  }

  .dropdown-item:hover {
    background: rgba(255, 255, 255, 0.1) !important;
    color: #ffffff !important;
  }
}

@media (max-width: 575.98px) {
  .container-fluid {
    padding-left: 1rem !important;
    padding-right: 1rem !important;
  }

  .brand-title {
    font-size: 1.2rem;
  }

  .brand-icon-wrapper {
    width: 40px;
    height: 40px;
  }

  .brand-icon {
    font-size: 1.3rem;
  }
}
</style>

