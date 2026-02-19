package com.cinema.controller;

import com.cinema.model.*;
import com.cinema.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ControleurReservation {

    @Autowired
    private DepotReservation depotReservation;

    @Autowired
    private DepotSeance depotSeance;

    @Autowired
    private DepotPlace depotPlace;

    @Autowired
    private DepotReservationPlace depotReservationPlace;

    @Autowired
    private DepotBillet depotBillet;

    @Autowired
    private DepotFacture depotFacture;

    @Autowired
    private DepotTarif depotTarif;

    @GetMapping
    public List<Reservation> getToutesLesReservations() {
        return depotReservation.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationParId(@PathVariable Long id) {
        Optional<Reservation> reservation = depotReservation.findById(id);
        if (reservation.isPresent()) {
            return ResponseEntity.ok(reservation.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/client/{email}")
    public ResponseEntity<List<Reservation>> getReservationsParClient(@PathVariable String email) {
        List<Reservation> reservations = depotReservation.findByEmailClient(email);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<Reservation> getReservationParReference(@PathVariable String reference) {
        Reservation reservation = depotReservation.findByReferenceReservation(reference);
        if (reservation != null) {
            return ResponseEntity.ok(reservation);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/seance/{seanceId}")
    public List<Reservation> getReservationsParSeance(@PathVariable Long seanceId) {
        Optional<Seance> seance = depotSeance.findById(seanceId);
        return seance.map(depotReservation::findBySeance).orElse(List.of());
    }

    @GetMapping("/recente")
    public List<Reservation> getReservationsRecentes() {
        return depotReservation.findReservationsRecentes();
    }

    /**
     * Créer une nouvelle réservation
     * Endpoint principal pour la fonctionnalité de réservation de places
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> creerReservation(@RequestBody DemandeReservation demande) {
        try {
            // Vérifier que la séance existe
            if (demande.getSeanceId() == null) {
                return ResponseEntity.badRequest().body("Séance requise");
            }

            Optional<Seance> seanceOptional = depotSeance.findById(demande.getSeanceId());
            if (seanceOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Séance non trouvée");
            }

            Seance seance = seanceOptional.get();

            // Vérifier que la séance n'est pas passée
            if (seance.getDateHeure().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Impossible de réserver pour une séance passée");
            }

            // Vérifier que la séance est active
            if (!"ACTIVE".equals(seance.getStatut())) {
                return ResponseEntity.badRequest().body("Cette séance n'est pas disponible");
            }

            // Vérifier que des places ont été sélectionnées
            if (demande.getPlaceIds() == null || demande.getPlaceIds().isEmpty()) {
                return ResponseEntity.badRequest().body("Au moins une place doit être sélectionnée");
            }

            // Récupérer les places sélectionnées
            List<Place> placesSelectionnees = depotPlace.findByIds(demande.getPlaceIds());
            if (placesSelectionnees.size() != demande.getPlaceIds().size()) {
                return ResponseEntity.badRequest().body("Certaines places sélectionnées n'existent pas");
            }

            // Vérifier que toutes les places appartiennent à la salle de la séance
            for (Place place : placesSelectionnees) {
                if (!place.getSalle().getId().equals(seance.getSalle().getId())) {
                    return ResponseEntity.badRequest().body("Toutes les places doivent appartenir à la salle de la séance");
                }
            }

            // Vérifier que les places ne sont pas déjà réservées pour cette séance
            List<Long> placesReserveesIds = depotReservationPlace.findPlaceIdsBySeanceId(seance.getId());
            for (Place place : placesSelectionnees) {
                if (placesReserveesIds.contains(place.getId())) {
                    return ResponseEntity.badRequest().body("La place " + place.getIdentifiant() + " est déjà réservée");
                }
            }

            // Créer la réservation
            Reservation reservation = new Reservation();
            reservation.setSeance(seance);
            reservation.setNomClient(demande.getNomClient());
            reservation.setEmailClient(demande.getEmailClient());
            reservation.setTelephoneClient(demande.getTelephoneClient());
            reservation.setStatut("CONFIRMEE");
            reservation.setDateReservation(LocalDateTime.now());
            reservation.setCreatedAt(LocalDateTime.now());
            reservation.setUpdatedAt(LocalDateTime.now());

            // Créer les ReservationPlace
            List<ReservationPlace> reservationPlaces = new ArrayList<>();
            BigDecimal montantTotal = BigDecimal.ZERO;

            for (Place place : placesSelectionnees) {
                // Récupérer le tarif selon le type de place ET le type de client
                List<Tarif> tarifs;
                BigDecimal prixUnitaire = BigDecimal.ZERO;
                
                // Chercher les tarifs spécifiques au type de client
                if ("enfant".equals(demande.getTypeClient())) {
                    tarifs = depotTarif.findByTypeAndActifTrue("ENFANT");
                } else if ("ado".equals(demande.getTypeClient())) {
                    tarifs = depotTarif.findByTypeAndActifTrue("Ado");
                } else if ("senior".equals(demande.getTypeClient())) {
                    tarifs = depotTarif.findByTypeAndActifTrue("SENIOR");
                } else {
                    // Adulte : utiliser les tarifs par catégorie de place (STANDARD, VIP, PREMIUM)
                    tarifs = depotTarif.findByTypeAndActifTrue(place.getCategorie());
                }
                
                if (!tarifs.isEmpty()) {
                    // Utiliser le tarif le plus élevé pour cette catégorie
                    prixUnitaire = tarifs.stream()
                            .map(Tarif::getPrix)
                            .max(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);
                } else {
                    // Tarif par défaut selon le type de client
                    if ("enfant".equals(demande.getTypeClient())) {
                        prixUnitaire = new BigDecimal("10.00"); // Enfant par défaut
                    } else if ("ado".equals(demande.getTypeClient())) {
                        prixUnitaire = new BigDecimal("15.00"); // Ado par défaut
                    } else if ("senior".equals(demande.getTypeClient())) {
                        prixUnitaire = new BigDecimal("16.00"); // Senior par défaut
                    } else {
                        prixUnitaire = new BigDecimal("20.00"); // Adulte par défaut
                    }
                }
                
                ReservationPlace rp = new ReservationPlace();
                rp.setReservation(reservation);
                rp.setPlace(place);
                rp.setPrixUnitaire(prixUnitaire);
                rp.setTypeClient(demande.getTypeClient()); // Enregistrer le type de client
                rp.setCreatedAt(LocalDateTime.now());
                rp.setUpdatedAt(LocalDateTime.now());
                reservationPlaces.add(rp);
                montantTotal = montantTotal.add(prixUnitaire);
            }

            reservation.setReservationPlaces(reservationPlaces);
            reservation.setMontantTotal(montantTotal);

            // Générer la référence de réservation
            String reference = genererReferenceReservation();
            reservation.setReferenceReservation(reference);

            // Sauvegarder la réservation et les places
            Reservation reservationCreee = depotReservation.save(reservation);
            depotReservationPlace.saveAll(reservationPlaces);

            // Mettre à jour les places disponibles dans la séance
            seance.setPlacesDisponibles(seance.getPlacesDisponibles() - placesSelectionnees.size());
            depotSeance.save(seance);

            // Générer les billets pour chaque place réservée
            List<Billet> billets = new ArrayList<>();
            for (ReservationPlace rp : reservationPlaces) {
                Billet billet = new Billet();
                billet.setReservation(reservationCreee);
                billet.setPlace(rp.getPlace());
                billet.setPrixUnitaire(rp.getPrixUnitaire());
                billet.setStatut(Billet.StatutBillet.ACTIF);
                billet.setDateEmission(LocalDateTime.now());
                billets.add(billet);
            }
            depotBillet.saveAll(billets);

            // Générer la facture
            Facture facture = new Facture();
            facture.setReservation(reservationCreee);
            facture.setNomClient(demande.getNomClient());
            facture.setEmailClient(demande.getEmailClient());
            facture.setTelephoneClient(demande.getTelephoneClient());
            facture.setMontantHt(montantTotal);
            facture.setStatut(Facture.StatutFacture.PAYE);
            depotFacture.save(facture);

            return ResponseEntity.ok(reservationCreee);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la réservation: " + e.getMessage());
        }
    }

    /**
     * Modifier une réservation (changement de places)
     */
    @PutMapping("/{id}/modifier")
    @Transactional
    public ResponseEntity<?> modifierReservation(@PathVariable Long id, @RequestBody DemandeReservation nouvelleDemande) {
        try {
            System.out.println("=== MODIFICATION DE RÉSERVATION ===");
            System.out.println("ID réservation: " + id);
            System.out.println("Données reçues: seanceId=" + nouvelleDemande.getSeanceId() +
                              ", placeIds=" + nouvelleDemande.getPlaceIds() +
                              ", nom=" + nouvelleDemande.getNomClient());

            Optional<Reservation> reservationOptional = depotReservation.findById(id);
            if (reservationOptional.isEmpty()) {
                System.out.println("Réservation non trouvée: " + id);
                return ResponseEntity.notFound().build();
            }

            Reservation reservation = reservationOptional.get();
            System.out.println("Réservation trouvée: statut=" + reservation.getStatut() +
                              ", canModify=" + reservation.getCanModify() +
                              ", modificationCount=" + reservation.getModificationCount());

            // Vérifier que la réservation peut être modifiée
            if (!reservation.peutEtreModifiee()) {
                System.out.println("Réservation ne peut pas être modifiée selon peutEtreModifiee()");
                return ResponseEntity.badRequest().body("Cette réservation ne peut plus être modifiée");
            }

            // Vérifier que la séance n'est pas passée
            LocalDateTime dateSeance = reservation.getSeance().getDateHeure();
            LocalDateTime maintenant = LocalDateTime.now();
            System.out.println("Date séance: " + dateSeance + ", maintenant: " + maintenant);
            if (dateSeance.isBefore(maintenant)) {
                System.out.println("Séance passée - modification impossible");
                return ResponseEntity.badRequest().body("Impossible de modifier une réservation pour une séance passée");
            }

            // Vérifier que la nouvelle demande concerne la même séance
            System.out.println("ID séance actuelle: " + reservation.getSeance().getId() +
                              ", ID séance demandée: " + nouvelleDemande.getSeanceId());
            if (!nouvelleDemande.getSeanceId().equals(reservation.getSeance().getId())) {
                System.out.println("Séance différente - modification impossible");
                return ResponseEntity.badRequest().body("La modification ne peut concerner que la même séance");
            }

            // Vérifier que des places ont été sélectionnées
            System.out.println("Places sélectionnées: " + nouvelleDemande.getPlaceIds());
            if (nouvelleDemande.getPlaceIds() == null || nouvelleDemande.getPlaceIds().isEmpty()) {
                System.out.println("Aucune place sélectionnée");
                return ResponseEntity.badRequest().body("Au moins une place doit être sélectionnée");
            }

            // Récupérer les nouvelles places sélectionnées
            List<Place> nouvellesPlaces = depotPlace.findByIds(nouvelleDemande.getPlaceIds());
            if (nouvellesPlaces.size() != nouvelleDemande.getPlaceIds().size()) {
                return ResponseEntity.badRequest().body("Certaines places sélectionnées n'existent pas");
            }

            // Vérifier que toutes les nouvelles places appartiennent à la salle de la séance
            System.out.println("Vérification appartenance salle - Salle séance: " + reservation.getSeance().getSalle().getId());
            for (Place place : nouvellesPlaces) {
                System.out.println("Place ID " + place.getId() + " (rangée " + place.getNumeroRangee() + ", place " + place.getNumeroPlace() + ") appartient à salle: " + place.getSalle().getId());
                if (!place.getSalle().getId().equals(reservation.getSeance().getSalle().getId())) {
                    System.out.println("Place " + place.getId() + " n'appartient pas à la bonne salle");
                    return ResponseEntity.badRequest().body("Toutes les places doivent appartenir à la salle de la séance");
                }
            }

            // Vérifier que les nouvelles places ne sont pas déjà réservées (sauf par cette réservation)
            System.out.println("Vérification disponibilité places");
            List<Long> placesReserveesIds = depotReservationPlace.findPlaceIdsBySeanceId(reservation.getSeance().getId());
            System.out.println("Places réservées pour cette séance: " + placesReserveesIds);
            System.out.println("Places actuelles de cette réservation: " + reservation.getReservationPlaces().stream().map(rp -> rp.getPlace().getId()).collect(java.util.stream.Collectors.toList()));

            for (Place place : nouvellesPlaces) {
                System.out.println("Vérification place ID " + place.getId());
                if (placesReserveesIds.contains(place.getId())) {
                    // Vérifier si c'est une place déjà réservée par cette réservation
                    boolean placeDejaReserveeParCetteReservation = reservation.getReservationPlaces().stream()
                        .anyMatch(rp -> rp.getPlace().getId().equals(place.getId()));
                    System.out.println("Place ID " + place.getId() + " déjà réservée - appartient à cette réservation: " + placeDejaReserveeParCetteReservation);
                    if (!placeDejaReserveeParCetteReservation) {
                        System.out.println("Place " + place.getNumeroRangee() + "-" + place.getNumeroPlace() + " déjà réservée par quelqu'un d'autre");
                        return ResponseEntity.badRequest().body("La place " + place.getNumeroRangee() + "-" + place.getNumeroPlace() + " est déjà réservée");
                    }
                } else {
                    System.out.println("Place ID " + place.getId() + " est disponible");
                }
            }

            // Identifier les changements de places
            List<Long> anciennesPlaceIds = reservation.getReservationPlaces().stream()
                .map(rp -> rp.getPlace().getId())
                .collect(java.util.stream.Collectors.toList());

            List<Long> nouvellesPlaceIds = nouvellesPlaces.stream()
                .map(Place::getId)
                .collect(java.util.stream.Collectors.toList());

            // Places à supprimer (dans anciennes mais pas dans nouvelles)
            List<ReservationPlace> placesASupprimer = reservation.getReservationPlaces().stream()
                .filter(rp -> !nouvellesPlaceIds.contains(rp.getPlace().getId()))
                .collect(java.util.stream.Collectors.toList());

            // Places à ajouter (dans nouvelles mais pas dans anciennes)
            List<Place> placesAAjouter = nouvellesPlaces.stream()
                .filter(place -> !anciennesPlaceIds.contains(place.getId()))
                .collect(java.util.stream.Collectors.toList());

            System.out.println("Anciennes places: " + anciennesPlaceIds);
            System.out.println("Nouvelles places: " + nouvellesPlaceIds);
            System.out.println("Places à supprimer: " + placesASupprimer.stream().map(rp -> rp.getPlace().getId()).collect(java.util.stream.Collectors.toList()));
            System.out.println("Places à ajouter: " + placesAAjouter.stream().map(Place::getId).collect(java.util.stream.Collectors.toList()));

            // Calculer le nouveau montant total
            BigDecimal nouveauMontantTotal = BigDecimal.ZERO;
            // Garder les places existantes
            List<ReservationPlace> placesGardees = reservation.getReservationPlaces().stream()
                .filter(rp -> nouvellesPlaceIds.contains(rp.getPlace().getId()))
                .collect(java.util.stream.Collectors.toList());
            nouveauMontantTotal = nouveauMontantTotal.add(
                placesGardees.stream()
                    .map(ReservationPlace::getPrixUnitaire)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            );
            // Ajouter le prix des nouvelles places
            nouveauMontantTotal = nouveauMontantTotal.add(
                placesAAjouter.stream()
                    .map(p -> reservation.getSeance().getPrix())
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            );

            // Supprimer les anciens billets des places supprimées
            List<Billet> billetsASupprimer = depotBillet.findByReservationId(reservation.getId()).stream()
                .filter(b -> placesASupprimer.stream().anyMatch(rp -> rp.getPlace().getId().equals(b.getPlace().getId())))
                .collect(java.util.stream.Collectors.toList());

            for (Billet billet : billetsASupprimer) {
                billet.setStatut(Billet.StatutBillet.ANNULE);
                billet.setUpdatedAt(LocalDateTime.now());
            }
            depotBillet.saveAll(billetsASupprimer);

            // Supprimer les ReservationPlace des places supprimées
            depotReservationPlace.deleteAll(placesASupprimer);

            // Créer les nouvelles ReservationPlace pour les places ajoutées
            List<ReservationPlace> nouvellesReservationPlaces = new ArrayList<>();
            for (Place place : placesAAjouter) {
                ReservationPlace rp = new ReservationPlace();
                rp.setReservation(reservation);
                rp.setPlace(place);
                rp.setPrixUnitaire(reservation.getSeance().getPrix());
                rp.setCreatedAt(LocalDateTime.now());
                rp.setUpdatedAt(LocalDateTime.now());
                nouvellesReservationPlaces.add(rp);
            }

            // Mettre à jour la réservation avec toutes les places finales
            List<ReservationPlace> toutesLesPlacesFinales = new ArrayList<>();
            toutesLesPlacesFinales.addAll(placesGardees);
            toutesLesPlacesFinales.addAll(nouvellesReservationPlaces);

            reservation.setReservationPlaces(toutesLesPlacesFinales);
            reservation.setMontantTotal(nouveauMontantTotal);
            reservation.setUpdatedAt(LocalDateTime.now());
            reservation.marquerCommeModifie(); // Incrémenter le compteur et désactiver modifications futures

            // Sauvegarder
            Reservation reservationModifiee = depotReservation.save(reservation);
            if (!nouvellesReservationPlaces.isEmpty()) {
                depotReservationPlace.saveAll(nouvellesReservationPlaces);
            }

            // Créer les nouveaux billets pour les places ajoutées
            List<Billet> nouveauxBillets = new ArrayList<>();
            for (ReservationPlace rp : nouvellesReservationPlaces) {
                Billet billet = new Billet();
                billet.setReservation(reservationModifiee);
                billet.setPlace(rp.getPlace());
                billet.setPrixUnitaire(rp.getPrixUnitaire());
                billet.setStatut(Billet.StatutBillet.ACTIF);
                billet.setDateEmission(LocalDateTime.now());
                nouveauxBillets.add(billet);
            }
            if (!nouveauxBillets.isEmpty()) {
                depotBillet.saveAll(nouveauxBillets);
            }

            // Mettre à jour la facture
            Optional<Facture> factureOpt = depotFacture.findByReservationId(reservation.getId());
            if (factureOpt.isPresent()) {
                Facture facture = factureOpt.get();
                facture.setMontantHt(nouveauMontantTotal);
                facture.recalculerMontants(); // Recalculer TVA et TTC
                facture.setUpdatedAt(LocalDateTime.now());
                depotFacture.save(facture);
            }

            // Mettre à jour les places disponibles dans la séance
            int differencePlaces = placesAAjouter.size() - placesASupprimer.size();
            Seance seance = reservation.getSeance();
            seance.setPlacesDisponibles(seance.getPlacesDisponibles() - differencePlaces);
            depotSeance.save(seance);

            System.out.println("Modification terminée avec succès pour réservation ID: " + reservation.getId());
            return ResponseEntity.ok(reservationModifiee);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la modification: " + e.getMessage());
        }
    }

    /**
     * Annuler une réservation
     */
    @PutMapping("/{id}/annuler")
    @Transactional
    public ResponseEntity<?> annulerReservation(@PathVariable Long id) {
        try {
            Optional<Reservation> reservationOptional = depotReservation.findById(id);
            if (reservationOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Reservation reservation = reservationOptional.get();

            // Vérifier que la réservation n'est pas déjà utilisée ou annulée
            if ("ANNULEE".equals(reservation.getStatut())) {
                return ResponseEntity.badRequest().body("La réservation est déjà annulée");
            }

            if ("UTILISEE".equals(reservation.getStatut())) {
                return ResponseEntity.badRequest().body("Impossible d'annuler une réservation utilisée");
            }

            // Vérifier que l'annulation se fait avant le jour de la séance (pas le jour même)
            LocalDateTime dateSeance = reservation.getSeance().getDateHeure();
            LocalDateTime maintenant = LocalDateTime.now();
            LocalDateTime debutJourSeance = dateSeance.toLocalDate().atStartOfDay();

            if (maintenant.isAfter(debutJourSeance) || maintenant.toLocalDate().equals(dateSeance.toLocalDate())) {
                return ResponseEntity.badRequest().body("L'annulation n'est possible que jusqu'à la veille de la séance");
            }

            // Annuler la réservation
            reservation.setStatut("ANNULEE");
            reservation.setUpdatedAt(LocalDateTime.now());

            // Annuler les billets associés
            List<Billet> billets = depotBillet.findByReservationId(reservation.getId());
            for (Billet billet : billets) {
                billet.setStatut(Billet.StatutBillet.ANNULE);
                billet.setUpdatedAt(LocalDateTime.now());
            }
            depotBillet.saveAll(billets);

            // Annuler la facture associée
            Optional<Facture> factureOpt = depotFacture.findByReservationId(reservation.getId());
            if (factureOpt.isPresent()) {
                Facture facture = factureOpt.get();
                facture.setStatut(Facture.StatutFacture.ANNULE);
                facture.setUpdatedAt(LocalDateTime.now());
                depotFacture.save(facture);
            }

            // Remettre les places disponibles en utilisant le nombre de places liées à la réservation
            Seance seance = reservation.getSeance();
            int nombrePlacesLiberees = reservation.getReservationPlaces().size();
            seance.setPlacesDisponibles(seance.getPlacesDisponibles() + nombrePlacesLiberees);
            depotSeance.save(seance);

            Reservation reservationAnnulee = depotReservation.save(reservation);

            return ResponseEntity.ok(reservationAnnulee);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'annulation: " + e.getMessage());
        }
    }

    /**
     * Marquer une réservation comme utilisée
     */
    @PutMapping("/{id}/utiliser")
    public ResponseEntity<?> utiliserReservation(@PathVariable Long id) {
        Optional<Reservation> reservationOptional = depotReservation.findById(id);
        if (reservationOptional.isPresent()) {
            Reservation reservation = reservationOptional.get();

            if (!"CONFIRMEE".equals(reservation.getStatut())) {
                return ResponseEntity.badRequest().body("Seule une réservation confirmée peut être marquée comme utilisée");
            }

            reservation.setStatut("UTILISEE");
            reservation.setUpdatedAt(LocalDateTime.now());

            return ResponseEntity.ok(depotReservation.save(reservation));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprimer une réservation (seulement si elle est annulée)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerReservation(@PathVariable Long id) {
        Optional<Reservation> reservationOptional = depotReservation.findById(id);
        if (reservationOptional.isPresent()) {
            Reservation reservation = reservationOptional.get();

            if (!"ANNULEE".equals(reservation.getStatut())) {
                return ResponseEntity.badRequest().body("Seules les réservations annulées peuvent être supprimées");
            }

            depotReservation.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Statistiques générales
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStatistiques() {
        try {
            Long totalReservations = depotReservation.count();
            Double chiffreAffaires = depotReservation.getChiffreAffairesTotal();

            return ResponseEntity.ok(new Object() {
                public final Long totalReservations = ControleurReservation.this.depotReservation.count();
                public final Double chiffreAffaires = ControleurReservation.this.depotReservation.getChiffreAffairesTotal();
            });
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des statistiques");
        }
    }

    /**
     * Génère une référence de réservation unique
     */
    private String genererReferenceReservation() {
        // Format: RES + YYMMDD + numéro séquentiel
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String datePart = LocalDate.now().format(formatter);

        // Générer un numéro unique (simple pour l'instant)
        long timestamp = System.currentTimeMillis();
        String sequence = String.format("%06d", (int)(timestamp % 1000000));

        return "RES" + datePart + sequence;
    }
}


