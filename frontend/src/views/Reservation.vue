<template>
  <div>
    <div class="row justify-content-center">
      <div class="col-lg-10">
        <div class="card">
          <div class="card-header">
            <h4 class="mb-0">
              <i class="bi bi-ticket me-2"></i>
              {{ isModificationMode ? 'Modifier ma réservation' : 'Réserver des places' }}
            </h4>
          </div>
          <div class="card-body">
            <!-- Informations de la séance -->
            <div v-if="seance" class="mb-4">
              <div :class="isModificationMode ? 'alert alert-warning' : 'alert alert-info'">
                <h6>Résumé de la séance</h6>
                <div v-if="isModificationMode" class="mb-2">
                  <small class="text-muted">
                    <i class="bi bi-info-circle me-1"></i>
                    Modification possible une seule fois. Choisissez vos nouvelles places.
                  </small>
                </div>
                  <div class="row">
                  <div class="col-md-6">
                    <strong>Film :</strong> {{ seance.film?.titre }}<br>
                    <strong>Salle :</strong> {{ seance.salle?.nom }}<br>
                    <strong>Capacité :</strong> {{ seance.salle?.capacite }} places
                  </div>
                  <div class="col-md-6">
                    <strong>Date :</strong> {{ formatDate(seance.dateHeure) }}<br>
                    <strong>Heure :</strong> {{ formatTime(seance.dateHeure) }}<br>
                    <strong>Prix :</strong> {{ seance.prix }}€ par place
                  </div>
                </div>
              </div>
            </div>

            <!-- Chargement -->
            <div v-else-if="loading" class="text-center">
              <div class="spinner-border" role="status">
                <span class="visually-hidden">Chargement...</span>
              </div>
            </div>

            <!-- Erreur -->
            <div v-else-if="error" class="alert alert-danger">
              <i class="bi bi-exclamation-triangle me-2"></i>
              {{ error }}
            </div>

            <!-- Sélection des places -->
            <div v-if="seance && !reservationSuccess" class="mb-4">
              <h5 class="mb-3">Sélectionnez vos places</h5>
              <div class="seat-selection-container">
                <SeatSelector
                  :salle="seance.salle"
                  :occupied-seats="occupiedSeats"
                  :all-seats="allSeats"
                  :max-selection="10"
                  @seats-changed="onSeatsChanged"
                  @max-selection-reached="onMaxSelectionReached"
                />
              </div>
            </div>

            <!-- Formulaire de réservation -->
            <form v-if="seance && selectedPlaces.length > 0 && !reservationSuccess" @submit.prevent="submitReservation">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label">Nom *</label>
                  <input
                    type="text"
                    v-model="reservationData.nomClient"
                    class="form-control"
                    required
                  >
                </div>
                <div class="col-md-6">
                  <label class="form-label">Email *</label>
                  <input
                    type="email"
                    v-model="reservationData.emailClient"
                    class="form-control"
                    required
                  >
                </div>
                <div class="col-md-6">
                  <label class="form-label">Téléphone</label>
                  <input
                    type="tel"
                    v-model="reservationData.telephoneClient"
                    class="form-control"
                  >
                </div>
                <div class="col-md-6">
                  <label class="form-label">Type *</label>
                  <select 
                    v-model="reservationData.typeClient" 
                    class="form-control"
                    required
                  >
                    <option value="enfant">
                        Enfant
                    </option>
                    <option value="ado">
                        Ado
                    </option>
                    <option value="senior">
                        Senior
                    </option>
                    <option value="adulte">
                        Adulte
                    </option>
                  </select>
                </div>
              </div>

              <!-- Récapitulatif -->
              <div class="mt-4 p-3 bg-light rounded">
                <h6>Récapitulatif</h6>
                <div class="row mb-2" v-for="(count, categorie) in selectedPlacesByCategory" :key="categorie">
                  <div class="col-6">Places {{ categorie }} ({{ count }}) :</div>
                  <div class="col-6 text-end">{{ getPrixByCategorie(categorie) }}€ × {{ count }} = {{ (getPrixByCategorie(categorie) * count).toFixed(2) }}€</div>
                </div>
                <hr class="col-12">
                <div class="row">
                  <div class="col-6 fw-bold">Total :</div>
                  <div class="col-6 text-end fw-bold text-primary">{{ totalPrix }}€</div>
                </div>
              </div>

              <div class="d-grid mt-4">
                <button
                  type="submit"
                  class="btn btn-success btn-lg"
                  :disabled="submitting"
                >
                  <i class="bi bi-check-circle me-2"></i>
                  {{ isModificationMode ? 'Modifier la réservation' : 'Confirmer la réservation' }}
                </button>
              </div>
            </form>

            <!-- Succès de la réservation -->
            <div v-if="reservationSuccess" class="text-center">
              <div class="alert alert-success">
                <i class="bi bi-check-circle-fill fs-1 text-success mb-3"></i>
                <h5>Réservation confirmée !</h5>
                <p>Votre réservation a été enregistrée avec succès.</p>
                <div class="mt-3">
                  <strong>Référence :</strong> #{{ reservationSuccess.reservation.id }}<br>
                  <strong>Film :</strong> {{ reservationSuccess.seance.film?.titre }}<br>
                  <strong>Date :</strong> {{ formatDate(reservationSuccess.seance.dateHeure) }} à {{ formatTime(reservationSuccess.seance.dateHeure) }}<br>
                  <strong>Places :</strong> {{ reservationSuccess.reservation.nombrePlaces }}<br>
                  <strong>Total payé :</strong> {{ reservationSuccess.reservation.montantTotal }}€
                </div>
              </div>
              <router-link to="/" class="btn btn-primary">
                <i class="bi bi-house me-2"></i>
                Retour à l'accueil
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Affichage des détails de réservation après succès -->
    <div v-if="reservationSuccess" class="row justify-content-center mt-4">
      <div class="col-lg-10">
        <ReservationDetails
          :reservation="reservationSuccess"
          @error="handleReservationDetailsError"
        />
      </div>
    </div>

    <!-- Toast de notification -->
    <div v-if="toast.show" class="toast-container position-fixed top-0 end-0 p-3">
      <div class="toast show" role="alert">
        <div class="toast-header" :class="toast.type === 'success' ? 'bg-success text-white' : 'bg-danger text-white'">
          <i :class="toast.type === 'success' ? 'bi bi-check-circle-fill me-2' : 'bi bi-exclamation-triangle-fill me-2'"></i>
          <strong class="me-auto">{{ toast.title }}</strong>
          <button type="button" class="btn-close btn-close-white" @click="hideToast"></button>
        </div>
        <div class="toast-body">
          {{ toast.message }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { seanceService, reservationService, placeService, tarifService } from '../services/api'
import SeatSelector from '../components/SeatSelector.vue'
import ReservationDetails from '../components/ReservationDetails.vue'

export default {
  name: 'Reservation',
  components: {
    SeatSelector,
    ReservationDetails
  },
  props: ['seanceId'],
  data() {
    return {
      seance: null,
      loading: true,
      error: null,
      submitting: false,
      reservationSuccess: null,
      occupiedSeats: [],
      allSeats: [],
      selectedPlaces: [],
      tarifsByCategorie: {}, // Pour stocker les vrais tarifs par catégorie
      isModificationMode: false,
      reservationToModify: null,
      reservationData: {
        nomClient: '',
        emailClient: '',
        telephoneClient: '',
        typeClient: 'adulte' // Par défaut adulte
      },
      toast: {
        show: false,
        type: 'success',
        title: '',
        message: ''
      }
    }
  },
  computed: {
    selectedPlacesByCategory() {
      const counts = { VIP: 0, PREMIUM: 0, STANDARD: 0 };
      
      this.selectedPlaces.forEach(place => {
        if (place.categorie && counts[place.categorie] !== undefined) {
          counts[place.categorie]++;
        }
      });
      
      return counts;
    },

    totalPrix() {
      if (!this.seance || !this.selectedPlaces.length) return 0;
      
      let total = 0;
      Object.entries(this.selectedPlacesByCategory).forEach(([categorie, count]) => {
        const prix = this.getPrixByCategorie(categorie);
        total += prix * count;
      });
      
      return total.toFixed(2);
    }
  },
  async mounted() {
    // Charger les tarifs disponibles
    await this.loadTarifs();
    
    // Vérifier si on est en mode modification
    if (this.$route.query.modifyReservationId) {
      this.isModificationMode = true
      await this.loadReservationToModify(this.$route.query.modifyReservationId)
    } else if (this.seanceId) {
      await this.loadSeance(this.seanceId)
      await this.loadOccupiedSeats(this.seanceId)
    } else {
      this.error = 'Aucune séance sélectionnée'
      this.loading = false
    }

    // Pré-remplir l'email si fourni
    if (this.$route.query.email) {
      this.reservationData.emailClient = this.$route.query.email
    }
  },
  methods: {
    async loadTarifs() {
      try {
        console.log('Chargement des tarifs depuis la base de données...');
        const response = await tarifService.getAllTarifs();
        const tarifs = response.data;
        
        console.log('Tarifs reçus de la base:', tarifs);
        
        // Initialiser la structure des tarifs par type de client
        this.tarifsByCategorie = {
          adulte: {},
          enfant: {},
          ado: {},
          senior: {}
        };
        
        // Organiser les tarifs selon la structure réelle de la base
        tarifs.forEach(tarif => {
          console.log('Traitement du tarif:', tarif);
          
          // Pour les tarifs adultes (type = STANDARD, VIP, PREMIUM)
          if (['STANDARD', 'VIP', 'PREMIUM'].includes(tarif.type)) {
            this.tarifsByCategorie['adulte'][tarif.type] = tarif.prix; // Garder en centimes
            console.log(`Tarif adulte ${tarif.type}: ${tarif.prix}`);
          }
          // Pour les tarifs Ado (type = Ado)
          else if (tarif.type === 'Ado') {
            // Extraire la catégorie depuis le nom (ex: "VIP Ado" -> "VIP")
            const categorie = this.extractCategorieFromNom(tarif.nom);
            if (categorie) {
              this.tarifsByCategorie['ado'][categorie] = tarif.prix; // Garder en centimes
              console.log(`Tarif ado ${categorie}: ${tarif.prix}`);
            }
          }
          // Pour les tarifs ENFANT
          else if (tarif.type === 'ENFANT') {
            const categorie = this.extractCategorieFromNom(tarif.nom);
            if (categorie) {
              this.tarifsByCategorie['enfant'][categorie] = tarif.prix; // Garder en centimes
              console.log(`Tarif enfant ${categorie}: ${tarif.prix}`);
            }
          }
          // Pour les tarifs SENIOR
          else if (tarif.type === 'SENIOR') {
            const categorie = this.extractCategorieFromNom(tarif.nom);
            if (categorie) {
              this.tarifsByCategorie['senior'][categorie] = tarif.prix; // Garder en centimes
              console.log(`Tarif senior ${categorie}: ${tarif.prix}`);
            }
          }
        });
        
        // Valeurs par défaut si aucun tarif trouvé (basées sur vos données réelles en centimes)
        const defaultTarifs = {
          adulte: { 
            VIP: 50000.00,      // 50000.00 centimes
            PREMIUM: 40000.00,  // 40000.00 centimes
            STANDARD: 30000.00  // 30000.00 centimes
          },
          enfant: { 
            VIP: 25000.00,      // 50% de 50000.00
            PREMIUM: 20000.00,  // 50% de 40000.00
            STANDARD: 15000.00  // 50% de 30000.00
          },
          ado: { 
            VIP: 45000.00,      // 45000.00 (votre donnée)
            PREMIUM: 30000.00,  // 30000.00 (votre donnée)
            STANDARD: 20000.00  // 20000.00 (votre donnée)
          },
          senior: { 
            VIP: 50000.00,      // À créer
            PREMIUM: 40000.00,  // À créer
            STANDARD: 30000.00  // À créer
          }
        };
        
        // Compléter avec les valeurs par défaut
        Object.keys(defaultTarifs).forEach(typeClient => {
          Object.keys(defaultTarifs[typeClient]).forEach(categorie => {
            if (!this.tarifsByCategorie[typeClient][categorie]) {
              this.tarifsByCategorie[typeClient][categorie] = defaultTarifs[typeClient][categorie];
            }
          });
        });
        
        console.log('Tarifs finaux organisés:', this.tarifsByCategorie);
      } catch (error) {
        console.error('Erreur lors du chargement des tarifs:', error);
        // Valeurs par défaut en cas d'erreur (en centimes)
        this.tarifsByCategorie = {
          adulte: { VIP: 50000.00, PREMIUM: 40000.00, STANDARD: 30000.00 },
          enfant: { VIP: 25000.00, PREMIUM: 20000.00, STANDARD: 15000.00 },
          ado: { VIP: 45000.00, PREMIUM: 30000.00, STANDARD: 20000.00 },
          senior: { VIP: 50000.00, PREMIUM: 40000.00, STANDARD: 30000.00 }
        };
      }
    },

    // Extraire la catégorie depuis le nom du tarif (ex: "VIP Ado" -> "VIP")
    extractCategorieFromNom(nom) {
      const categories = ['VIP', 'PREMIUM', 'STANDARD'];
      for (const categorie of categories) {
        if (nom.includes(categorie)) {
          return categorie;
        }
      }
      return null;
    },

    // Mapper le type de tarif depuis la base vers le type de client
    mapTypeClient(tarifType) {
      const mapping = {
        'STANDARD': 'adulte',
        'ENFANT': 'enfant',
        'Ado': 'ado',
        'SENIOR': 'senior',
        'VIP': 'adulte',    // VIP utilise les tarifs adultes par défaut
        'PREMIUM': 'adulte', // Premium utilise les tarifs adultes par défaut
        'GROUPE': 'adulte'   // Groupe utilise les tarifs adultes
      };
      return mapping[tarifType] || 'adulte';
    },

    getPrixByCategorie(categorie) {
      const typeClient = this.reservationData?.typeClient || 'adulte';
      
      // Utiliser la nouvelle structure de tarifs par type de client
      const tarifsClient = this.tarifsByCategorie[typeClient];
      
      if (tarifsClient && tarifsClient[categorie]) {
        return tarifsClient[categorie];
      }
      
      // Valeur par défaut si non trouvé
      return 20.00;
    },

    async loadSeance(seanceId) {
      try {
        // Pour l'instant, on utilise la recherche pour obtenir les détails
        // Idéalement, il faudrait un endpoint pour récupérer une séance par ID
        const response = await seanceService.rechercherSeances()
        const seance = response.data.find(s => s.id == seanceId)

        if (seance) {
          this.seance = seance

          // Charger toutes les places de la salle
          await this.loadAllSeats(seance.salle.id)
        } else {
          this.error = 'Séance non trouvée'
        }
      } catch (error) {
        console.error('Erreur lors du chargement de la séance:', error)
        this.error = 'Erreur lors du chargement de la séance'
      } finally {
        this.loading = false
      }
    },

    async loadOccupiedSeats(seanceId) {
      try {
        // Charger les réservations pour cette séance pour connaître les places occupées
        const response = await reservationService.getReservationsParSeance(seanceId)
        const reservations = response.data

        // Extraire les places occupées depuis les réservations NON annulées
        this.occupiedSeats = []
        reservations.forEach(reservation => {
          // Ne compter que les réservations confirmées (pas annulées)
          if (reservation.statut === 'CONFIRMEE' && reservation.reservationPlaces) {
            reservation.reservationPlaces.forEach(rp => {
              this.occupiedSeats.push(rp.place)
            })
          }
        })
      } catch (error) {
        console.error('Erreur lors du chargement des places occupées:', error)
        // Ne pas afficher d'erreur pour ça, juste continuer
      }
    },

    async loadAllSeats(salleId) {
      try {
        const response = await placeService.getPlacesParSalle(salleId)
        this.allSeats = response.data || []
        console.log('Places chargées pour la salle', salleId, ':', this.allSeats)
        
        // Compter les catégories pour vérification
        const categoriesCount = { VIP: 0, PREMIUM: 0, STANDARD: 0 }
        this.allSeats.forEach(place => {
          if (place.categorie && categoriesCount[place.categorie] !== undefined) {
            categoriesCount[place.categorie]++
          }
        })
        console.log('Répartition des catégories dans les places:', categoriesCount)
      } catch (error) {
        console.error('Erreur lors du chargement de toutes les places:', error)
        this.allSeats = []
      }
    },

    async loadReservationToModify(reservationId) {
      try {
        console.log('Chargement de la réservation à modifier:', reservationId)
        const response = await reservationService.getReservation(reservationId)
        this.reservationToModify = response.data

        console.log('Réservation à modifier:', this.reservationToModify)

        // Charger la séance
        await this.loadSeance(this.reservationToModify.seance.id)

        // Charger TOUTES les places de la salle d'abord
        await this.loadAllSeats(this.reservationToModify.seance.salle.id)

        // Charger les places occupées (mais exclure celles de cette réservation)
        await this.loadOccupiedSeatsForModification(this.reservationToModify.seance.id, reservationId)

        // Pré-sélectionner les places actuelles de la réservation
        this.selectedPlaces = this.reservationToModify.reservationPlaces.map(rp => ({
          row: rp.place.numeroRangee,
          seat: rp.place.numeroPlace
        }))

        // Pré-remplir le formulaire
        this.reservationData = {
          nomClient: this.reservationToModify.nomClient,
          emailClient: this.reservationToModify.emailClient,
          telephoneClient: this.reservationToModify.telephoneClient
        }

        console.log('Places pré-sélectionnées:', this.selectedPlaces)

      } catch (error) {
        console.error('Erreur lors du chargement de la réservation à modifier:', error)
        this.error = 'Erreur lors du chargement de la réservation à modifier'
        this.loading = false
      }
    },

    async loadOccupiedSeatsForModification(seanceId, excludeReservationId) {
      try {
        const response = await reservationService.getReservationsParSeance(seanceId)
        const reservations = response.data

        // Exclure la réservation en cours de modification
        this.occupiedSeats = []
        reservations.forEach(reservation => {
          if (reservation.id != excludeReservationId && reservation.statut === 'CONFIRMEE') {
            reservation.reservationPlaces.forEach(rp => {
              this.occupiedSeats.push(rp.place)
            })
          }
        })
      } catch (error) {
        console.error('Erreur lors du chargement des places occupées pour modification:', error)
        this.occupiedSeats = []
      }
    },

    onSeatsChanged(selectedSeats) {
      // Ajouter les informations de catégorie aux places sélectionnées
      this.selectedPlaces = selectedSeats.map(seat => {
        // Trouver la place correspondante dans allSeats pour obtenir sa catégorie
        const place = this.allSeats.find(p => 
          p.numeroRangee === seat.row && p.numeroPlace === seat.seat
        );
        
        return {
          ...seat,
          categorie: place ? place.categorie : 'STANDARD' // Par défaut STANDARD
        };
      });
      
      console.log('Places sélectionnées avec catégories:', this.selectedPlaces);
    },

    onMaxSelectionReached() {
      this.error = 'Vous ne pouvez pas sélectionner plus de 10 places à la fois'
      setTimeout(() => {
        this.error = null
      }, 3000)
    },
    async submitReservation() {
      this.submitting = true
      this.error = null

      try {
        // Récupérer les vrais IDs des places depuis toutes les places de la salle
        const placeIds = []
        console.log('Recherche des IDs pour les places sélectionnées:', this.selectedPlaces)
        console.log('Places disponibles:', this.allSeats.slice(0, 5)) // Premieres 5 places pour debug

        for (const selectedSeat of this.selectedPlaces) {
          // Chercher la place correspondante dans toutes les places de la salle
          const place = this.allSeats.find(p => {
            const match = p.numeroRangee === selectedSeat.row && p.numeroPlace === selectedSeat.seat
            if (match) console.log('Place trouvée:', p)
            return match
          })

          if (place) {
            placeIds.push(place.id)
            console.log(`Place trouvée: rangée ${selectedSeat.row}, place ${selectedSeat.seat} -> ID ${place.id}`)
          } else {
            // Chercher avec des types différents (au cas où)
            const placeAlt = this.allSeats.find(p =>
              Number(p.numeroRangee) === Number(selectedSeat.row) &&
              Number(p.numeroPlace) === Number(selectedSeat.seat)
            )

            if (placeAlt) {
              placeIds.push(placeAlt.id)
              console.log(`Place trouvée (conversion): rangée ${selectedSeat.row}, place ${selectedSeat.seat} -> ID ${placeAlt.id}`)
            } else {
              // Méthode de secours avec calcul si la place n'est pas trouvée
              const calculatedId = (selectedSeat.row - 1) * this.seance.salle.placesParRangee + selectedSeat.seat
              placeIds.push(calculatedId)
              console.warn(`Place non trouvée: rangée ${selectedSeat.row}, place ${selectedSeat.seat}, utilisation ID calculé: ${calculatedId}`)
            }
          }
        }

        const data = {
          seanceId: this.seance.id,
          placeIds: placeIds,
          ...this.reservationData
        }

        let response
        if (this.isModificationMode && this.reservationToModify) {
          // Mode modification
          console.log('Mode modification - appel API modifier')
          console.log('Données envoyées:', data)
          console.log('ID réservation:', this.reservationToModify.id)
          response = await reservationService.modifyReservation(this.reservationToModify.id, data)

          // Afficher une notification de succès pour la modification
          this.showSuccessToast('Réservation modifiée avec succès !', 'Vos nouvelles places ont été réservées.')

          // Afficher aussi une alerte popup
          alert('✅ Réservation modifiée avec succès !\n\nVos nouvelles places ont été réservées et vos billets/facture mis à jour.')

          // Rediriger vers Mes Réservations après modification réussie
          setTimeout(() => {
            this.$router.push({
              name: 'MesReservations',
              query: { email: this.reservationData.emailClient || this.reservationToModify?.emailClient }
            })
          }, 1000) // Petit délai pour que l'utilisateur voie le message
        } else {
          // Mode création
          console.log('Mode création - appel API reserver')
          response = await reservationService.reserverPlaces(data)

          // Afficher une notification de succès
          this.showSuccessToast('Réservation confirmée avec succès !', 'Vos places ont été réservées.')

          // Afficher aussi une alerte popup pour être sûr que l'utilisateur voit le message
          alert('✅ Réservation confirmée avec succès !\n\nVos places ont été réservées et vos billets/facture sont prêts.')
        }

        this.reservationSuccess = response.data

      } catch (error) {
        console.error('Erreur lors de la réservation:', error)
        this.error = error.response?.data || 'Erreur lors de la réservation'

        // Afficher une notification d'erreur
        this.showErrorToast('Erreur de réservation', error.response?.data?.message || 'Une erreur est survenue lors de la réservation.')
      } finally {
        this.submitting = false
      }
    },
    formatDate(dateTimeString) {
      const date = new Date(dateTimeString)
      return date.toLocaleDateString('fr-FR', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    },
    formatTime(dateTimeString) {
      const date = new Date(dateTimeString)
      return date.toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    handleReservationDetailsError(errorMessage) {
      this.error = errorMessage
    },

    showSuccessToast(title, message) {
      this.toast = {
        show: true,
        type: 'success',
        title,
        message
      }

      // Masquer automatiquement après 10 secondes
      setTimeout(() => {
        this.toast.show = false
      }, 10000)
    },

    showErrorToast(title, message) {
      this.toast = {
        show: true,
        type: 'error',
        title,
        message
      }

      // Masquer automatiquement après 10 secondes
      setTimeout(() => {
        this.toast.show = false
      }, 10000)
    },

    hideToast() {
      this.toast.show = false
    }
  }
}
</script>

<style scoped>
/* Styles pour les toasts */
.toast-container {
  z-index: 1100;
}

.toast {
  min-width: 350px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.toast-header {
  font-weight: 600;
}

.toast .btn-close {
  opacity: 0.8;
}

.toast .btn-close:hover {
  opacity: 1;
}
</style>
