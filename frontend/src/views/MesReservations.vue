<template>
  <div class="mes-reservations">
    <div class="container-fluid px-4 py-5">
      <div class="row">
        <div class="col-12">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h1 class="page-title">
                <i class="bi bi-ticket-perforated me-3"></i>
                Mes Réservations
              </h1>
              <p class="page-subtitle">Consultez et gérez vos réservations de cinéma</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Formulaire de recherche par email -->
      <div class="row mb-4">
        <div class="col-md-6">
          <div class="card">
            <div class="card-body">
              <h5 class="card-title">Rechercher mes réservations</h5>
              <div class="input-group">
                <input
                  v-model="searchEmail"
                  type="email"
                  class="form-control"
                  placeholder="Votre adresse email"
                  @keyup.enter="searchReservations"
                >
                <button
                  class="btn btn-primary"
                  @click="searchReservations"
                  :disabled="!searchEmail || loading"
                >
                  <i class="bi bi-search me-2"></i>
                  Rechercher
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Chargement...</span>
        </div>
        <p class="mt-2">Recherche de vos réservations...</p>
      </div>

      <!-- Erreur -->
      <div v-else-if="error" class="alert alert-danger">
        <i class="bi bi-exclamation-triangle me-2"></i>
        {{ error }}
      </div>

      <!-- Liste des réservations -->
      <div v-else-if="reservations.length > 0">
        <div class="row">
          <div class="col-12">
            <div class="card">
              <div class="card-header">
                <h5 class="mb-0">Vos réservations ({{ reservations.length }})</h5>
              </div>
              <div class="card-body p-0">
                <div class="list-group list-group-flush">
                  <div
                    v-for="reservation in reservations"
                    :key="reservation.id"
                    class="list-group-item"
                  >
                    <div class="d-flex justify-content-between align-items-start">
                      <div class="flex-grow-1">
                        <div class="d-flex align-items-center mb-2">
                          <h6 class="mb-0 me-3">
                            <i class="bi bi-film me-2"></i>
                            {{ reservation.seance.film.titre }}
                          </h6>
                          <span :class="getStatusBadgeClass(reservation.statut)">
                            {{ reservation.statut }}
                          </span>
                        </div>

                        <div class="row">
                          <div class="col-md-6">
                            <p class="mb-1">
                              <i class="bi bi-calendar-event me-2"></i>
                              {{ formatDateTime(reservation.seance.dateHeure) }}
                            </p>
                            <p class="mb-1">
                              <i class="bi bi-geo-alt me-2"></i>
                              {{ reservation.seance.salle.nom }}
                            </p>
                          </div>
                          <div class="col-md-6">
                            <p class="mb-1">
                              <i class="bi bi-ticket-perforated me-2"></i>
                              {{ reservation.reservationPlaces.length }} place(s)
                            </p>
                            <p class="mb-1">
                              <i class="bi bi-cash me-2"></i>
                              {{ reservation.montantTotal }}€
                            </p>
                            <p class="mb-1">
                              <i class="bi bi-hash me-2"></i>
                              Réf: {{ reservation.referenceReservation }}
                            </p>
                          </div>
                        </div>

                        <!-- Places détaillées -->
                        <div class="mt-2">
                          <small class="text-muted">
                            Places: {{ getPlacesDetails(reservation.reservationPlaces) }}
                          </small>
                        </div>
                      </div>

                      <div class="d-flex flex-column gap-2 ms-3">
                        <!-- Boutons d'action -->
                        <button
                          v-if="canModifyReservation(reservation)"
                          @click="modifyReservation(reservation)"
                          class="btn btn-outline-primary btn-sm"
                          :title="'Modifier la réservation ' + reservation.referenceReservation"
                        >
                          <i class="bi bi-pencil me-1"></i>
                          Modifier
                        </button>

                        <button
                          v-if="canCancelReservation(reservation)"
                          @click="setReservationToCancel(reservation)"
                          class="btn btn-outline-danger btn-sm"
                          :disabled="cancelling"
                          :title="'Annuler la réservation ' + reservation.referenceReservation"
                          data-bs-toggle="modal"
                          data-bs-target="#cancelModal"
                        >
                          <i class="bi bi-x-circle me-1"></i>
                          Annuler
                        </button>

                        <!-- Debug info -->
                        <small v-if="!canCancelReservation(reservation)" class="text-muted ms-2">
                          ({{ getCancelReason(reservation) }})
                        </small>

                        <button
                          @click="viewReservationDetails(reservation)"
                          class="btn btn-outline-info btn-sm"
                        >
                          <i class="bi bi-eye me-1"></i>
                          Détails
                        </button>

                        <!-- Bouton Facture -->
                        <div v-if="reservation.statut === 'CONFIRMEE' || reservation.statut === 'UTILISEE' || reservation.statut === 'ANNULEE'">
                          <button
                            @click="viewFacture(reservation)"
                            class="btn btn-outline-primary btn-sm"
                            title="Voir la facture"
                            data-bs-toggle="modal"
                            data-bs-target="#factureModal"
                          >
                            <i class="bi bi-receipt me-1"></i>
                            Facture
                          </button>
                        </div>

                        <!-- Bouton Billet -->
                        <div v-if="reservation.statut === 'CONFIRMEE' || reservation.statut === 'UTILISEE'">
                          <button
                            @click="viewBillet(reservation)"
                            class="btn btn-outline-success btn-sm"
                            title="Voir le billet"
                            data-bs-toggle="modal"
                            data-bs-target="#billetModal"
                          >
                            <i class="bi bi-file-earmark-pdf me-1"></i>
                            Billet
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Aucune réservation -->
      <div v-else-if="searched && !loading" class="text-center py-5">
        <div class="empty-state">
          <i class="bi bi-ticket-perforated display-1 text-muted mb-3"></i>
          <h4>Aucune réservation trouvée</h4>
          <p class="text-muted">Vous n'avez pas de réservation avec cette adresse email.</p>
          <router-link to="/" class="btn btn-primary">
            <i class="bi bi-house me-2"></i>
            Réserver des places
          </router-link>
        </div>
      </div>
    </div>

    <!-- Modal d'aperçu du billet -->
    <div class="modal fade" id="billetModal" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header bg-success text-white">
            <h5 class="modal-title">
              <i class="bi bi-ticket-perforated me-2"></i>
              Aperçu des Billets
              <small v-if="selectedBillets.length > 1" class="ms-2">
                ({{ selectedBilletIndex + 1 }} / {{ selectedBillets.length }})
              </small>
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body" v-if="selectedBillet && selectedReservationForBillet">
            <div class="billet-preview p-4 border rounded bg-light">
              <div class="text-center mb-4">
                <h3 class="text-success mb-4">
                  <i class="bi bi-film me-2"></i>
                  CINÉMA MANAGEMENT
                </h3>
                <div class="border-bottom pb-3 mb-3">
                  <h4>{{ selectedReservationForBillet.seance.film.titre }}</h4>
                  <p class="mb-1">{{ formatDateTime(selectedReservationForBillet.seance.dateHeure) }}</p>
                  <p class="mb-0">{{ selectedReservationForBillet.seance.salle.nom }}</p>
                </div>
              </div>

              <div class="row">
                <div class="col-md-6">
                  <h6><i class="bi bi-person me-2"></i>Client</h6>
                  <p class="mb-1">{{ selectedReservationForBillet.nomClient }}</p>
                  <p class="mb-3">{{ selectedReservationForBillet.emailClient }}</p>

                  <h6><i class="bi bi-tag me-2"></i>Référence</h6>
                  <p class="mb-1">{{ selectedBillet.referenceBillet }}</p>
                  <p class="mb-3">{{ selectedReservationForBillet.referenceReservation }}</p>
                </div>
                <div class="col-md-6">
                  <h6><i class="bi bi-geo-alt me-2"></i>Place</h6>
                  <p class="mb-3">Rangée {{ selectedBillet.place.numeroRangee }}, Place {{ selectedBillet.place.numeroPlace }}</p>

                  <h6><i class="bi bi-cash me-2"></i>Prix</h6>
                  <p class="mb-3">{{ selectedBillet.prixUnitaire }}€</p>

                  <h6><i class="bi bi-qr-code me-2"></i>Code QR</h6>
                  <p class="mb-0">{{ selectedBillet.codeQr }}</p>
                </div>
              </div>

              <div class="text-center mt-4">
                <div class="alert alert-info">
                  <i class="bi bi-info-circle me-2"></i>
                  Présentez ce billet à l'entrée de la salle
                </div>
              </div>

              <!-- Navigation entre les billets si plusieurs -->
              <div v-if="selectedBillets.length > 1" class="mt-3">
                <div class="d-flex justify-content-center align-items-center">
                  <button
                    @click="previousBillet"
                    :disabled="selectedBilletIndex === 0"
                    class="btn btn-outline-secondary btn-sm me-2"
                  >
                    <i class="bi bi-chevron-left"></i> Précédent
                  </button>

                  <div class="btn-group" role="group">
                    <button
                      v-for="(billet, index) in selectedBillets"
                      :key="billet.id"
                      @click="goToBillet(index)"
                      :class="['btn btn-sm', selectedBilletIndex === index ? 'btn-success' : 'btn-outline-success']"
                    >
                      {{ index + 1 }}
                    </button>
                  </div>

                  <button
                    @click="nextBillet"
                    :disabled="selectedBilletIndex === selectedBillets.length - 1"
                    class="btn btn-outline-secondary btn-sm ms-2"
                  >
                    Suivant <i class="bi bi-chevron-right"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
              Fermer
            </button>
            <button
              v-if="selectedBillets.length > 1"
              type="button"
              class="btn btn-outline-success me-2"
              @click="downloadAllBilletsPdf()"
            >
              <i class="bi bi-download me-2"></i>
              Télécharger tous les billets
            </button>
            <button
              type="button"
              class="btn btn-success"
              @click="downloadBilletPdf(selectedBillet)"
            >
              <i class="bi bi-download me-2"></i>
              Télécharger ce billet PDF
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal d'aperçu de la facture -->
    <div class="modal fade" id="factureModal" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title">
              <i class="bi bi-receipt me-2"></i>
              Aperçu de la Facture
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body" v-if="selectedFacture && selectedReservationForFacture">
            <div class="facture-preview p-4 border rounded bg-light">
              <div class="text-center mb-4">
                <h3 class="text-primary mb-4">
                  <i class="bi bi-building me-2"></i>
                  CINÉMA MANAGEMENT
                </h3>
                <div class="border-bottom pb-3 mb-3">
                  <h4>FACTURE</h4>
                  <p class="mb-1">{{ selectedFacture.numeroFacture }}</p>
                  <p class="mb-0">Émise le {{ formatDate(selectedFacture.dateEmission) }}</p>
                </div>
              </div>

              <div class="row">
                <div class="col-md-6">
                  <h6><i class="bi bi-person me-2"></i>Client</h6>
                  <p class="mb-1">{{ selectedFacture.nomClient }}</p>
                  <p class="mb-3">{{ selectedFacture.emailClient }}</p>

                  <h6><i class="bi bi-film me-2"></i>Film</h6>
                  <p class="mb-3">{{ selectedReservationForFacture.seance.film.titre }}</p>
                </div>
                <div class="col-md-6">
                  <h6><i class="bi bi-calendar-event me-2"></i>Séance</h6>
                  <p class="mb-1">{{ formatDateTime(selectedReservationForFacture.seance.dateHeure) }}</p>
                  <p class="mb-3">{{ selectedReservationForFacture.seance.salle.nom }}</p>

                  <h6><i class="bi bi-tag me-2"></i>Référence</h6>
                  <p class="mb-3">{{ selectedReservationForFacture.referenceReservation }}</p>
                </div>
              </div>

              <div class="mt-4">
                <h6><i class="bi bi-list me-2"></i>Détail des places</h6>
                <div class="table-responsive">
                  <table class="table table-sm">
                    <thead>
                      <tr>
                        <th>Description</th>
                        <th class="text-end">Quantité</th>
                        <th class="text-end">Prix unitaire</th>
                        <th class="text-end">Total</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="place in selectedReservationForFacture.reservationPlaces" :key="place.id">
                        <td>Place R{{ place.place.numeroRangee }}P{{ place.place.numeroPlace }}</td>
                        <td class="text-end">1</td>
                        <td class="text-end">{{ place.prixUnitaire }}€</td>
                        <td class="text-end">{{ place.prixUnitaire }}€</td>
                      </tr>
                    </tbody>
                    <tfoot>
                      <tr class="border-top">
                        <th colspan="3" class="text-end">Sous-total HT</th>
                        <th class="text-end">{{ selectedFacture.montantHt }}€</th>
                      </tr>
                      <tr>
                        <th colspan="3" class="text-end">TVA ({{ selectedFacture.tauxTva }}%)</th>
                        <th class="text-end">{{ selectedFacture.montantTva }}€</th>
                      </tr>
                      <tr class="border-top">
                        <th colspan="3" class="text-end">Total TTC</th>
                        <th class="text-end">{{ selectedFacture.montantTtc }}€</th>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>

              <div class="text-center mt-4">
                <div class="alert" :class="selectedFacture.statut === 'PAYE' ? 'alert-success' : 'alert-danger'">
                  <i class="bi bi-check-circle me-2"></i>
                  Statut: {{ selectedFacture.statut === 'PAYE' ? 'Payée' : 'Annulée' }}
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
              Fermer
            </button>
            <button
              type="button"
              class="btn btn-primary"
              @click="downloadFacturePdf(selectedFacture)"
            >
              <i class="bi bi-download me-2"></i>
              Télécharger PDF
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de confirmation d'annulation -->
    <div class="modal fade" id="cancelModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Confirmer l'annulation</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body" v-if="reservationToCancel">
            <p>Êtes-vous sûr de vouloir annuler cette réservation ?</p>
            <div class="alert alert-warning">
              <strong>{{ reservationToCancel.seance.film.titre }}</strong><br>
              {{ formatDateTime(reservationToCancel.seance.dateHeure) }}<br>
              {{ reservationToCancel.reservationPlaces.length }} place(s) - {{ reservationToCancel.montantTotal }}€
            </div>
            <p class="text-muted mb-0">
              <i class="bi bi-info-circle me-1"></i>
              L'annulation n'est possible que jusqu'à la veille de la séance.
            </p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>
            <button
              type="button"
              class="btn btn-danger"
              @click="cancelReservation"
              :disabled="cancelling"
            >
              <span v-if="cancelling" class="spinner-border spinner-border-sm me-2"></span>
              Confirmer l'annulation
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reservationService, billetService, factureService } from '../services/api'

export default {
  name: 'MesReservations',
  data() {
    return {
      searchEmail: '',
      reservations: [],
      loading: false,
      error: null,
      searched: false,
      cancelling: false,
      reservationToCancel: null,
      selectedBillet: null,
      selectedBillets: [],
      selectedBilletIndex: 0,
      selectedFacture: null,
      selectedReservationForBillet: null,
      selectedReservationForFacture: null
    }
  },
  methods: {
    async searchReservations() {
      if (!this.searchEmail) return

      this.loading = true
      this.error = null

      try {
        const response = await reservationService.getReservationsByClient(this.searchEmail)
        this.reservations = response.data || []
        this.searched = true

        // Trier par date décroissante
        this.reservations.sort((a, b) => new Date(b.dateReservation) - new Date(a.dateReservation))

      } catch (error) {
        console.error('Erreur lors de la recherche:', error)
        this.error = 'Erreur lors de la recherche des réservations'
        this.reservations = []
      } finally {
        this.loading = false
      }
    },

    canCancelReservation(reservation) {
      if (reservation.statut !== 'CONFIRMEE') return false

      const dateSeance = new Date(reservation.seance.dateHeure)
      const maintenant = new Date()
      const debutJourSeance = new Date(dateSeance)
      debutJourSeance.setHours(0, 0, 0, 0)

      // Annulation possible seulement avant le jour de la séance
      return maintenant < debutJourSeance
    },

    canModifyReservation(reservation) {
      // Pour les réservations existantes sans ces champs, considérer comme modifiables
      const canModify = reservation.canModify !== false // undefined/null = true
      const modificationCount = reservation.modificationCount || 0

      // Conditions de modification
      const isConfirmed = reservation.statut === 'CONFIRMEE'
      const canStillModify = canModify && modificationCount < 1

      const dateSeance = new Date(reservation.seance.dateHeure)
      const maintenant = new Date()
      const isBeforeSeance = dateSeance > maintenant

      return isConfirmed && canStillModify && isBeforeSeance
    },

    getCancelReason(reservation) {
      if (reservation.statut !== 'CONFIRMEE') {
        return `Statut: ${reservation.statut}`
      }

      const dateSeance = new Date(reservation.seance.dateHeure)
      const maintenant = new Date()
      const debutJourSeance = new Date(dateSeance)
      debutJourSeance.setHours(0, 0, 0, 0)

      if (maintenant >= debutJourSeance) {
        return 'Trop tard (jour de la séance)'
      }

      return 'Annulation possible'
    },

    setReservationToCancel(reservation) {
      this.reservationToCancel = reservation
    },

    async cancelReservation() {
      if (!this.reservationToCancel) {
        return
      }

      this.cancelling = true

      try {
        await reservationService.cancelReservation(this.reservationToCancel.id)

        // Rechercher à nouveau pour mettre à jour la liste
        await this.searchReservations()

        // Notification de succès
        this.showSuccessToast('Réservation annulée', 'Votre réservation a été annulée avec succès.')

      } catch (error) {
        console.error('Erreur lors de l\'annulation:', error)
        const message = error.response?.data?.message || error.response?.data || error.message || 'Erreur lors de l\'annulation'
        this.showErrorToast('Erreur d\'annulation', message)
      } finally {
        this.cancelling = false
        this.reservationToCancel = null
      }
    },

    modifyReservation(reservation) {
      // Rediriger vers la page de réservation avec les paramètres de modification
      const confirmModify = confirm(`Voulez-vous modifier la réservation ${reservation.referenceReservation} ?\n\nAttention : vous ne pourrez la modifier qu'une seule fois.`)
      if (confirmModify) {
        // Rediriger vers la page de réservation avec l'ID de la réservation à modifier
        this.$router.push({
          name: 'Reservation',
          params: { seanceId: reservation.seance.id },
          query: {
            modifyReservationId: reservation.id,
            email: reservation.emailClient
          }
        })
      }
    },

    viewReservationDetails(reservation) {
      // Pour l'instant, on affiche juste une alerte avec les détails
      const details = `
Film: ${reservation.seance.film.titre}
Date: ${this.formatDateTime(reservation.seance.dateHeure)}
Salle: ${reservation.seance.salle.nom}
Places: ${reservation.reservationPlaces.length}
Total: ${reservation.montantTotal}€
Référence: ${reservation.referenceReservation}
      `.trim()

      alert('Détails de la réservation:\n\n' + details)
    },

    async viewBillet(reservation) {
      try {
        this.loading = true
        // Récupérer tous les billets de cette réservation
        const response = await billetService.getBilletsByReservation(reservation.id)
        const billets = response.data

        if (!billets || billets.length === 0) {
          this.showErrorToast('Erreur', 'Aucun billet trouvé pour cette réservation. Elle a peut-être été créée avant l\'implémentation du système de billetterie.')
          return
        }

        // Stocker tous les billets et commencer par le premier
        this.selectedBillets = billets
        this.selectedBilletIndex = 0
        this.selectedBillet = billets[0]
        this.selectedReservationForBillet = reservation

        // Le modal s'ouvrira automatiquement grâce aux attributs data-bs dans le HTML
        // On utilise un petit délai pour s'assurer que les données sont bien chargées
        setTimeout(() => {
          const modal = document.getElementById('billetModal')
          if (modal) {
            modal.style.display = 'block'
            modal.classList.add('show')
            document.body.classList.add('modal-open')
          }
        }, 100)

      } catch (error) {
        console.error('Erreur lors de la récupération du billet:', error)
        if (error.response?.status === 404) {
          this.showErrorToast('Billet introuvable', 'Cette réservation n\'a pas de billet associé.')
        } else {
          this.showErrorToast('Erreur', 'Impossible de récupérer le billet')
        }
      } finally {
        this.loading = false
      }
    },

    async viewFacture(reservation) {
      try {
        this.loading = true
        // Récupérer la facture de cette réservation
        const response = await factureService.getFactureByReservation(reservation.id)
        const facture = response.data

        if (!facture) {
          this.showErrorToast('Erreur', 'Aucune facture trouvée pour cette réservation. Elle a peut-être été créée avant l\'implémentation du système de facturation.')
          return
        }

        this.selectedFacture = facture
        this.selectedReservationForFacture = reservation

        // Le modal s'ouvrira automatiquement grâce aux attributs data-bs dans le HTML
        // On utilise un petit délai pour s'assurer que les données sont bien chargées
        setTimeout(() => {
          const modal = document.getElementById('factureModal')
          if (modal) {
            modal.style.display = 'block'
            modal.classList.add('show')
            document.body.classList.add('modal-open')
          }
        }, 100)

      } catch (error) {
        console.error('Erreur lors de la récupération de la facture:', error)
        if (error.response?.status === 404) {
          this.showErrorToast('Facture introuvable', 'Cette réservation n\'a pas de facture associée.')
        } else {
          this.showErrorToast('Erreur', 'Impossible de récupérer la facture')
        }
      } finally {
        this.loading = false
      }
    },

    async downloadBilletPdf(billet) {
      try {
        // Télécharger le PDF
        const pdfResponse = await billetService.downloadBilletPdf(billet.id)

        // Créer un blob et déclencher le téléchargement
        const blob = new Blob([pdfResponse.data], { type: 'application/pdf' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `billet_${billet.referenceBillet}.pdf`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)

        this.showSuccessToast('Téléchargement réussi', 'Le billet a été téléchargé')

      } catch (error) {
        console.error('Erreur lors du téléchargement du billet:', error)
        this.showErrorToast('Erreur de téléchargement', 'Impossible de télécharger le billet')
      }
    },

    async downloadFacturePdf(facture) {
      try {
        // Télécharger le PDF
        const pdfResponse = await factureService.downloadFacturePdf(facture.id)

        // Créer un blob et déclencher le téléchargement
        const blob = new Blob([pdfResponse.data], { type: 'application/pdf' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `facture_${facture.numeroFacture}.pdf`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)

        this.showSuccessToast('Téléchargement réussi', 'La facture a été téléchargée')

      } catch (error) {
        console.error('Erreur lors du téléchargement de la facture:', error)
        this.showErrorToast('Erreur de téléchargement', 'Impossible de télécharger la facture')
      }
    },

    async downloadAllBilletsPdf() {
      try {
        // Télécharger chaque billet individuellement
        for (let i = 0; i < this.selectedBillets.length; i++) {
          const billet = this.selectedBillets[i]

          const pdfResponse = await billetService.downloadBilletPdf(billet.id)
          const blob = new Blob([pdfResponse.data], { type: 'application/pdf' })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = `billet_${billet.referenceBillet}.pdf`
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)

          // Petit délai entre chaque téléchargement pour éviter les conflits
          if (i < this.selectedBillets.length - 1) {
            await new Promise(resolve => setTimeout(resolve, 500))
          }
        }

        this.showSuccessToast('Téléchargement réussi', `Tous les ${this.selectedBillets.length} billets ont été téléchargés`)

      } catch (error) {
        console.error('Erreur lors du téléchargement des billets:', error)
        this.showErrorToast('Erreur de téléchargement', 'Impossible de télécharger tous les billets')
      }
    },

    // Navigation entre les billets
    previousBillet() {
      if (this.selectedBilletIndex > 0) {
        this.selectedBilletIndex--
        this.selectedBillet = this.selectedBillets[this.selectedBilletIndex]
      }
    },

    nextBillet() {
      if (this.selectedBilletIndex < this.selectedBillets.length - 1) {
        this.selectedBilletIndex++
        this.selectedBillet = this.selectedBillets[this.selectedBilletIndex]
      }
    },

    goToBillet(index) {
      if (index >= 0 && index < this.selectedBillets.length) {
        this.selectedBilletIndex = index
        this.selectedBillet = this.selectedBillets[index]
      }
    },

    getPlacesDetails(reservationPlaces) {
      return reservationPlaces.map(rp => `R${rp.place.numeroRangee}P${rp.place.numeroPlace}`).join(', ')
    },

    formatDateTime(dateTimeString) {
      const date = new Date(dateTimeString)
      return date.toLocaleDateString('fr-FR', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    },

    getStatusBadgeClass(statut) {
      const classes = {
        'CONFIRMEE': 'badge bg-success',
        'UTILISEE': 'badge bg-info',
        'ANNULEE': 'badge bg-danger'
      }
      return classes[statut] || 'badge bg-secondary'
    },

    showSuccessToast(title, message) {
      // Utiliser une notification simple pour l'instant
      alert(`${title}: ${message}`)
    },

    showErrorToast(title, message) {
      alert(`${title}: ${message}`)
    }
  },

  mounted() {
    // Écouter la fermeture des modals pour nettoyer les données
    const cancelModal = document.getElementById('cancelModal')
    if (cancelModal) {
      cancelModal.addEventListener('hidden.bs.modal', () => {
        this.reservationToCancel = null
      })
    }

    const billetModal = document.getElementById('billetModal')
    if (billetModal) {
      billetModal.addEventListener('hidden.bs.modal', () => {
        this.selectedBillet = null
        this.selectedBillets = []
        this.selectedBilletIndex = 0
        this.selectedReservationForBillet = null
      })
    }

    const factureModal = document.getElementById('factureModal')
    if (factureModal) {
      factureModal.addEventListener('hidden.bs.modal', () => {
        this.selectedFacture = null
        this.selectedReservationForFacture = null
      })
    }

    // Si on arrive avec un paramètre email dans l'URL, le pré-remplir
    if (this.$route.query.email) {
      this.searchEmail = this.$route.query.email
      this.searchReservations()
    }
  }
}
</script>

<style scoped>
.page-title {
  color: #2c3e50;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  color: #6c757d;
  font-size: 1.1rem;
  margin-bottom: 0;
}

.empty-state {
  max-width: 400px;
  margin: 0 auto;
}

.list-group-item {
  border-left: none;
  border-right: none;
}

.list-group-item:first-child {
  border-top: none;
}

.btn-sm {
  font-size: 0.8rem;
  padding: 0.25rem 0.5rem;
}

.badge {
  font-size: 0.75rem;
}

.billet-preview, .facture-preview {
  font-family: 'Courier New', monospace;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.billet-preview h3, .facture-preview h3 {
  font-family: 'Arial', sans-serif;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
}

@media (max-width: 768px) {
  .d-flex.justify-content-between {
    flex-direction: column;
    gap: 1rem;
  }

  .input-group {
    flex-direction: column;
  }

  .input-group .btn {
    border-top-left-radius: 0;
    border-top-right-radius: 0;
  }
}
</style>