<template>
  <div class="toast-demo container mt-4">
    <div class="card shadow-sm">
      <div class="card-header bg-primary text-white">
        <h5 class="mb-0">
          <i class="bi bi-bell me-2"></i>
          Démonstration des Notifications Toast
        </h5>
      </div>
      <div class="card-body">
        <p class="text-muted">
          Testez les différents types de notifications toast avec des exemples réalistes.
        </p>

        <div class="row g-3">
          <div class="col-md-6">
            <h6>Notifications de succès</h6>
            <div class="d-grid gap-2">
              <button @click="showSuccessToast('Réservation confirmée avec succès !')" class="btn btn-success">
                <i class="bi bi-check-circle me-2"></i>
                Réservation réussie
              </button>
              <button @click="showSuccessToast('Paiement traité avec succès.')" class="btn btn-success">
                <i class="bi bi-credit-card me-2"></i>
                Paiement réussi
              </button>
            </div>
          </div>

          <div class="col-md-6">
            <h6>Notifications d'erreur</h6>
            <div class="d-grid gap-2">
              <button @click="showErrorToast('Erreur lors de la réservation. Veuillez réessayer.')" class="btn btn-danger">
                <i class="bi bi-x-circle me-2"></i>
                Erreur de réservation
              </button>
              <button @click="showErrorToast('Paiement refusé. Vérifiez vos informations.')" class="btn btn-danger">
                <i class="bi bi-exclamation-triangle me-2"></i>
                Paiement refusé
              </button>
            </div>
          </div>

          <div class="col-md-6">
            <h6>Notifications d'avertissement</h6>
            <div class="d-grid gap-2">
              <button @click="showWarningToast('Plus que 5 places disponibles pour ce film.')" class="btn btn-warning">
                <i class="bi bi-exclamation-triangle me-2"></i>
                Places limitées
              </button>
              <button @click="showWarningToast('Votre session expire dans 5 minutes.')" class="btn btn-warning">
                <i class="bi bi-clock me-2"></i>
                Session expirante
              </button>
            </div>
          </div>

          <div class="col-md-6">
            <h6>Notifications d'information</h6>
            <div class="d-grid gap-2">
              <button @click="showInfoToast('Nouveau film ajouté au catalogue cette semaine.')" class="btn btn-info">
                <i class="bi bi-info-circle me-2"></i>
                Nouveau film
              </button>
              <button @click="showInfoToast('Maintenance programmée ce soir de 22h à 23h.')" class="btn btn-info">
                <i class="bi bi-gear me-2"></i>
                Maintenance
              </button>
            </div>
          </div>
        </div>

        <hr class="my-4">

        <div class="alert alert-info">
          <i class="bi bi-lightbulb me-2"></i>
          <strong>Utilisation dans le code :</strong><br>
          <code>toast.success('Message', 'Titre')</code> •
          <code>toast.error('Message', 'Titre')</code> •
          <code>toast.warning('Message', 'Titre')</code> •
          <code>toast.info('Message', 'Titre')</code>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { toast } from '../services/api'

export default {
  name: 'ToastDemo',
  methods: {
    showSuccessToast(message) {
      toast.success(message, 'Succès')
    },

    showErrorToast(message) {
      toast.error(message, 'Erreur')
    },

    showWarningToast(message) {
      toast.warning(message, 'Attention')
    },

    showInfoToast(message) {
      toast.info(message, 'Information')
    }
  }
}
</script>

<style scoped>
.toast-demo {
  max-width: 800px;
  margin: 0 auto;
}

.card {
  border: none;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}

.card-header {
  border-radius: 15px 15px 0 0 !important;
  border: none;
}

.btn {
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.2);
}

.alert {
  border-radius: 10px;
  border: none;
}

code {
  background: rgba(0,0,0,0.05);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}
</style>