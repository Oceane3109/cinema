<template>
  <div id="app">
    <!-- Navbar élégante et professionnelle pour clients -->
    <nav v-if="!$route.path.startsWith('/admin')" class="client-navbar navbar navbar-expand-lg navbar-dark shadow-lg fixed-top">
      <div class="container-fluid px-4">
        <!-- Logo et nom -->
        <router-link to="/" class="navbar-brand d-flex align-items-center text-decoration-none">
          <div class="brand-container">
            <div class="brand-icon-wrapper">
              <i class="bi bi-film brand-icon"></i>
              <div class="brand-glow"></div>
            </div>
            <div class="brand-text">
              <span class="brand-title">Cinéma</span>
              <span class="brand-subtitle">Management</span>
            </div>
          </div>
        </router-link>

        <!-- Bouton mobile -->
        <button class="navbar-toggler border-0 shadow-none" type="button" data-bs-toggle="collapse" data-bs-target="#clientNavbar">
          <span class="navbar-toggler-icon"></span>
        </button>

        <!-- Menu de navigation -->
        <div class="collapse navbar-collapse" id="clientNavbar">
          <ul class="navbar-nav me-auto">
            <li class="nav-item mx-1">
              <router-link to="/" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/' }">
                <i class="bi bi-house-door me-2"></i>
                <span class="nav-text">Accueil</span>
                <div class="nav-indicator" v-if="$route.path === '/'"></div>
              </router-link>
            </li>
            <li class="nav-item mx-1">
              <router-link to="/seances" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/seances' }">
                <i class="bi bi-calendar-event me-2"></i>
                <span class="nav-text">Séances</span>
                <div class="nav-indicator" v-if="$route.path === '/seances'"></div>
              </router-link>
            </li>
            <li class="nav-item mx-1">
              <router-link to="/mes-reservations" class="nav-link px-4 py-3 rounded-pill position-relative" :class="{ active: $route.path === '/mes-reservations' }">
                <i class="bi bi-ticket-perforated me-2"></i>
                <span class="nav-text">Mes Réservations</span>
                <div class="nav-indicator" v-if="$route.path === '/mes-reservations'"></div>
              </router-link>
            </li>
          </ul>

          <!-- Section droite avec actions utilisateur -->
          <ul class="navbar-nav">
            <li class="nav-item">
              <div class="user-actions">
                <button class="btn btn-outline-light btn-sm rounded-pill me-2 px-3" title="Notifications">
                  <i class="bi bi-bell"></i>
                  <span class="d-lg-none ms-1">Notifications</span>
                </button>
                <button class="btn btn-outline-light btn-sm rounded-pill px-3" title="Mon compte">
                  <i class="bi bi-person-circle me-1"></i>
                  <span class="d-lg-none ms-1">Mon compte</span>
                </button>
              </div>
            </li>
          </ul>
        </div>
      </div>

      <!-- Barre de progression de scroll -->
      <div class="scroll-progress">
        <div class="scroll-progress-bar"></div>
      </div>
    </nav>

    <!-- Contenu principal -->
    <main class="main-content">
      <div class="container-fluid px-4 py-5">
        <router-view />
      </div>
    </main>

    <!-- Footer discret -->
    <footer v-if="!$route.path.startsWith('/admin')" class="footer mt-auto py-4 bg-light border-top">
      <div class="container-fluid px-4">
        <div class="row align-items-center">
          <div class="col-md-6">
            <small class="text-muted d-flex align-items-center">
              <i class="bi bi-c-circle me-2 fs-5 text-primary"></i>
              <div>
                <div class="fw-semibold">2024 Cinéma Management</div>
                <div class="small opacity-75">Système de gestion de réservations</div>
              </div>
            </small>
          </div>
          <div class="col-md-6 text-end">
            <small class="text-muted d-flex align-items-center justify-content-end">
              <div class="text-end me-2">
                <div class="fw-semibold">Sécurisé et fiable</div>
                <div class="small opacity-75">SSL & Chiffrement 256-bit</div>
              </div>
              <i class="bi bi-shield-check fs-5 text-success"></i>
            </small>
          </div>
        </div>
      </div>
    </footer>

    <!-- Toast container pour les notifications -->
    <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 1060;">
      <div v-for="(toast, index) in toasts" :key="toast.id"
           class="toast show fade"
           :class="`bg-${toast.type} text-white border-0 shadow-lg`"
           role="alert">
        <div class="toast-header bg-transparent border-0 text-white">
          <i :class="getToastIcon(toast.type)" class="me-2"></i>
          <strong class="me-auto">{{ toast.title }}</strong>
          <button type="button" class="btn-close btn-close-white" @click="removeToast(toast.id)"></button>
        </div>
        <div class="toast-body">
          {{ toast.message }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      toasts: [],
      toastId: 0
    }
  },
  mounted() {
    // Animation de la barre de progression de scroll
    this.updateScrollProgress()

    window.addEventListener('scroll', this.updateScrollProgress)

    // Exposer la méthode showToast globalement
    this.$root.showToast = this.showToast
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
    },

    showToast(message, type = 'info', title = '', duration = 5000) {
      const id = ++this.toastId
      const toast = {
        id,
        message,
        type,
        title: title || this.getDefaultTitle(type),
        duration
      }

      this.toasts.push(toast)

      // Auto-remove après la durée
      setTimeout(() => {
        this.removeToast(id)
      }, duration)
    },

    removeToast(id) {
      const index = this.toasts.findIndex(t => t.id === id)
      if (index > -1) {
        this.toasts.splice(index, 1)
      }
    },

    getToastIcon(type) {
      const icons = {
        success: 'bi-check-circle-fill',
        error: 'bi-x-circle-fill',
        warning: 'bi-exclamation-triangle-fill',
        info: 'bi-info-circle-fill'
      }
      return icons[type] || icons.info
    },

    getDefaultTitle(type) {
      const titles = {
        success: 'Succès',
        error: 'Erreur',
        warning: 'Attention',
        info: 'Information'
      }
      return titles[type] || titles.info
    }
  }
}
</script>

<style>
@import './assets/app.css';

/* Navbar client professionnelle */
.client-navbar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.client-navbar:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%, rgba(0,0,0,0.1)) !important;
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

/* User actions */
.user-actions .btn {
  transition: all 0.3s ease;
}

.user-actions .btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
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

/* Mobile responsive */
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

  .user-actions {
    margin-top: 1rem;
    padding-top: 1rem;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
  }

  .user-actions .btn {
    width: 100%;
    margin-bottom: 0.5rem;
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

/* Espace pour le header fixe */
body:not(.admin-page) .main-content {
  padding-top: 80px;
}

/* Styles pour les toasts */
.toast-container {
  z-index: 1060;
}

.toast {
  margin-bottom: 0.5rem;
  border-radius: 12px !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1) !important;
  animation: slideInRight 0.3s ease-out;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.toast-header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.toast-body {
  font-weight: 500;
}

/* Footer amélioré */
.footer {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%) !important;
  border-top: 2px solid #e2e8f0 !important;
  margin-top: auto;
}

.footer .text-primary {
  color: #667eea !important;
}

.footer .text-success {
  color: #10b981 !important;
}
</style>
