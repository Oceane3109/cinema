<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <h4 class="text-white mb-0">
        <i class="bi bi-film me-2"></i>
        Cinéma Admin
      </h4>
    </div>
    <nav class="sidebar-nav">
      <ul class="nav flex-column">
        <li class="nav-item">
          <router-link to="/admin/dashboard" class="nav-link">
            <i class="bi bi-speedometer2 me-2"></i>
            Tableau de bord
          </router-link>
        </li>
        <li class="nav-item">
          <router-link to="/admin/films" class="nav-link">
            <i class="bi bi-film me-2"></i>
            Films
          </router-link>
        </li>
        <li class="nav-item">
          <router-link to="/admin/seances" class="nav-link">
            <i class="bi bi-calendar-event me-2"></i>
            Séances
          </router-link>
        </li>
        <li class="nav-item">
          <router-link to="/admin/salles" class="nav-link">
            <i class="bi bi-building me-2"></i>
            Salles
          </router-link>
        </li>
        <li class="nav-item">
          <router-link to="/admin/reservations" class="nav-link">
            <i class="bi bi-ticket-perforated me-2"></i>
            Réservations
          </router-link>
        </li>
        <li class="nav-item">
          <router-link to="/admin/chiffre-affaires" class="nav-link">
            <i class="bi bi-currency-euro me-2"></i>
            Chiffre d'affaires
          </router-link>
        </li>
        <li class="nav-item">
          <router-link to="/admin/pub-gestion" class="nav-link">
            <i class="bi bi-film me-2"></i>
            Gestion Publicitaire
          </router-link>
        </li>
        <li class="nav-item">
          <router-link to="/admin/statistiques" class="nav-link">
            <i class="bi bi-graph-up me-2"></i>
            Statistiques
          </router-link>
        </li>
        <li class="nav-item">
          <router-link to="/admin/parametres" class="nav-link">
            <i class="bi bi-gear me-2"></i>
            Paramètres
          </router-link>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script>
export default {
  name: 'AdminSidebar'
}
</script>

<style scoped>
.sidebar {
  width: 250px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  position: fixed;
  height: 100vh;
  overflow-y: auto;
  z-index: 1000;
}

.sidebar-header {
  padding: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-nav {
  padding: 1rem 0;
}

.sidebar-nav .nav-link {
  color: rgba(255, 255, 255, 0.8);
  padding: 0.75rem 1.5rem;
  border-radius: 0;
  transition: all 0.3s ease;
  text-decoration: none;
  display: flex;
  align-items: center;
}

.sidebar-nav .nav-link:hover {
  color: white;
  background-color: rgba(255, 255, 255, 0.1);
  text-decoration: none;
}

.sidebar-nav .nav-link.active {
  color: white;
  background-color: rgba(255, 255, 255, 0.2);
  border-left: 4px solid white;
}

.sidebar-nav .nav-link.router-link-active {
  color: white;
  background-color: rgba(255, 255, 255, 0.2);
  border-left: 4px solid white;
}

.sidebar-nav .nav-link i {
  font-size: 1.1rem;
}
</style>
