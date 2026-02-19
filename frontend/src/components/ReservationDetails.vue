<template>
  <div class="reservation-details">
    <div class="reservation-header">
      <h3>Réservation #{{ reservation.referenceReservation }}</h3>
      <span class="status-badge" :class="getStatusClass(reservation.statut)">
        {{ reservation.statut }}
      </span>
    </div>

    <div class="reservation-info">
      <div class="info-section">
        <h4>Informations de la séance</h4>
        <p><strong>Film:</strong> {{ reservation.seance.film.titre }}</p>
        <p><strong>Date:</strong> {{ formatDate(reservation.seance.dateHeure) }}</p>
        <p><strong>Salle:</strong> {{ reservation.seance.salle.nom }}</p>
        <p><strong>Prix par place:</strong> {{ reservation.seance.prix }} €</p>
      </div>

      <div class="info-section">
        <h4>Informations client</h4>
        <p><strong>Nom:</strong> {{ reservation.nomClient }}</p>
        <p><strong>Email:</strong> {{ reservation.emailClient }}</p>
        <p><strong>Téléphone:</strong> {{ reservation.telephoneClient }}</p>
      </div>

      <div class="info-section">
        <h4>Détails de la réservation</h4>
        <p><strong>Places réservées:</strong> {{ reservation.reservationPlaces.length }}</p>
        <p><strong>Montant total:</strong> {{ reservation.montantTotal }} €</p>
        <p><strong>Date de réservation:</strong> {{ formatDate(reservation.dateReservation) }}</p>
      </div>
    </div>

    <div class="places-section">
      <h4>Places réservées</h4>
      <div class="places-list">
        <div
          v-for="rp in reservation.reservationPlaces"
          :key="rp.id"
          class="place-item"
        >
          Rangée {{ rp.place.numeroRangee }}, Place {{ rp.place.numeroPlace }}
          ({{ rp.prixUnitaire }} €)
        </div>
      </div>
    </div>

    <div class="actions-section">
      <h4>Documents</h4>
      <div class="actions-buttons">
        <button
          v-for="billet in billets"
          :key="billet.id"
          @click="downloadBillet(billet.id)"
          class="btn btn-primary"
        >
          📄 Télécharger Billet (Rangée {{ billet.place.numeroRangee }}, Place {{ billet.place.numeroPlace }})
        </button>

        <button
          v-if="facture"
          @click="downloadFacture(facture.id)"
          class="btn btn-secondary"
        >
          🧾 Télécharger Facture
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { billetService, factureService } from '../services/api'

export default {
  name: 'ReservationDetails',
  props: {
    reservation: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      billets: [],
      facture: null,
      loading: false
    }
  },
  async mounted() {
    await this.loadBilletsAndFacture()
  },
  methods: {
    async loadBilletsAndFacture() {
      try {
        this.loading = true

        // Charger les billets
        const billetsResponse = await billetService.getBilletsByReservation(this.reservation.id)
        this.billets = billetsResponse.data

        // Charger la facture
        const factureResponse = await factureService.getFactureByReservation(this.reservation.id)
        this.facture = factureResponse.data

      } catch (error) {
        console.error('Erreur lors du chargement des documents:', error)
        this.$emit('error', 'Erreur lors du chargement des billets et facture')
      } finally {
        this.loading = false
      }
    },

    async downloadBillet(billetId) {
      try {
        const response = await billetService.downloadBilletPdf(billetId)
        const blob = new Blob([response.data], { type: 'application/pdf' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `billet-${billetId}.pdf`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      } catch (error) {
        console.error('Erreur lors du téléchargement du billet:', error)
        this.$emit('error', 'Erreur lors du téléchargement du billet')
      }
    },

    async downloadFacture(factureId) {
      try {
        const response = await factureService.downloadFacturePdf(factureId)
        const blob = new Blob([response.data], { type: 'application/pdf' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `facture-${factureId}.pdf`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      } catch (error) {
        console.error('Erreur lors du téléchargement de la facture:', error)
        this.$emit('error', 'Erreur lors du téléchargement de la facture')
      }
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleString('fr-FR')
    },

    getStatusClass(status) {
      switch (status) {
        case 'CONFIRMEE': return 'status-confirmed'
        case 'ANNULEE': return 'status-cancelled'
        case 'UTILISEE': return 'status-used'
        default: return 'status-default'
      }
    }
  }
}
</script>

<style scoped>
.reservation-details {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #eee;
}

.reservation-header h3 {
  margin: 0;
  color: #333;
}

.status-badge {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
}

.status-confirmed {
  background: #28a745;
  color: white;
}

.status-cancelled {
  background: #dc3545;
  color: white;
}

.status-used {
  background: #6c757d;
  color: white;
}

.status-default {
  background: #007bff;
  color: white;
}

.reservation-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.info-section {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
  border-left: 4px solid #007bff;
}

.info-section h4 {
  margin: 0 0 10px 0;
  color: #495057;
  font-size: 16px;
}

.info-section p {
  margin: 5px 0;
  color: #6c757d;
  font-size: 14px;
}

.places-section {
  margin-bottom: 30px;
}

.places-section h4 {
  margin-bottom: 15px;
  color: #495057;
}

.places-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
}

.place-item {
  background: #e9ecef;
  padding: 10px;
  border-radius: 4px;
  text-align: center;
  font-size: 14px;
  color: #495057;
}

.actions-section h4 {
  margin-bottom: 15px;
  color: #495057;
}

.actions-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.btn {
  padding: 10px 15px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  display: inline-block;
  text-align: center;
  transition: background-color 0.3s;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #545b62;
}

@media (max-width: 768px) {
  .reservation-details {
    padding: 15px;
  }

  .reservation-info {
    grid-template-columns: 1fr;
  }

  .places-list {
    grid-template-columns: 1fr;
  }
}
</style>