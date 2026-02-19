 <template>
  <div class="admin-page">
    <!-- Header admin réutilisable -->
    <AdminHeader />

    <!-- Contenu principal -->
    <main class="main-content">
      <div class="container-fluid px-4 py-5">
        <div class="row">
          <div class="col-12">
            <div class="d-flex justify-content-between align-items-center mb-4">
              <div>
                <h1 class="page-title">
                  <i class="bi bi-house-door me-3"></i>
                  Gestion des Salles
                </h1>
                <p class="page-subtitle">Administration des salles de cinéma</p>
              </div>
            </div>
          </div>
        </div>

        <AdminSalles />
      </div>
    </main>

    <!-- Footer discret -->
    <footer class="footer mt-auto py-3 bg-light border-top">
      <div class="container-fluid px-4">
        <div class="row align-items-center">
          <div class="col-md-6">
            <small class="text-muted">
              <i class="bi bi-c-circle me-1"></i>
              2024 Cinéma Management - Système de gestion de réservations
            </small>
          </div>
          <div class="col-md-6 text-end">
            <small class="text-muted">
              <i class="bi bi-shield-check me-1"></i>
              Sécurisé et fiable
            </small>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import AdminHeader from '../../components/AdminHeader.vue'
import AdminSalles from '../../components/admin/AdminSalles.vue'

export default {
  name: 'AdminSallesPage',
  components: {
    AdminHeader,
    AdminSalles
  }
}
</script>

<style scoped>
.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.page-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  margin: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }
}

@media (max-width: 576px) {
  .page-title {
    font-size: 1.75rem;
  }
}
</style>
