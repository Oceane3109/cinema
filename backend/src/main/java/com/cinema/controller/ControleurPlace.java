package com.cinema.controller;

import com.cinema.model.Place;
import com.cinema.model.Salle;
import com.cinema.repository.DepotPlace;
import com.cinema.repository.DepotReservationPlace;
import com.cinema.repository.DepotSalle;
import com.cinema.service.ReinitialisationSallesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = "*")
public class ControleurPlace {

    @Autowired
    private DepotPlace depotPlace;

    @Autowired
    private DepotReservationPlace depotReservationPlace;

    @Autowired
    private DepotSalle depotSalle;

    @Autowired
    private ReinitialisationSallesService reinitialisationSallesService;

    @GetMapping("/salle/{salleId}")
    public List<Place> getPlacesParSalle(@PathVariable Long salleId) {
        return depotPlace.findBySalleIdOrderByRangeeAndPlace(salleId);
    }

    @GetMapping("/seance/{seanceId}/disponibles")
    public ResponseEntity<List<Map<String, Object>>> getPlacesDisponiblesPourSeance(@PathVariable Long seanceId) {
        try {
            // Récupérer les places déjà réservées pour cette séance
            List<Long> placesReserveesIds = depotReservationPlace.findPlaceIdsBySeanceId(seanceId);

            // Pour l'instant, on simule la récupération des places de la salle
            // Idéalement, il faudrait récupérer la salle depuis la séance
            // Ici on utilise une approche simplifiée

            return ResponseEntity.ok(List.of()); // Placeholder - sera implémenté avec la logique complète

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/salle/{salleId}/initialiser")
    public ResponseEntity<String> initialiserPlacesSalle(@PathVariable Long salleId) {
        try {
            Salle salle = depotSalle.findById(salleId).orElse(null);
            if (salle == null) {
                return ResponseEntity.badRequest().body("Salle non trouvée");
            }

            // Supprimer les places existantes pour cette salle
            depotPlace.deleteBySalleId(salleId);

            // Créer les nouvelles places selon la configuration
            int totalPlaces = 0;
            Map<String, Integer> repartition = salle.getRepartitionPlacesMap();
            
            // Créer les places VIP
            for (int i = 0; i < repartition.get("VIP"); i++) {
                Place place = new Place();
                place.setSalle(salle);
                place.setNumeroRangee(1); // Places VIP en première rangée
                place.setNumeroPlace(i + 1);
                place.setCategorie("VIP");
                depotPlace.save(place);
                totalPlaces++;
            }

            // Créer les places PREMIUM
            for (int i = 0; i < repartition.get("PREMIUM"); i++) {
                Place place = new Place();
                place.setSalle(salle);
                place.setNumeroRangee(2); // Places PREMIUM en deuxième rangée
                place.setNumeroPlace(i + 1);
                place.setCategorie("PREMIUM");
                depotPlace.save(place);
                totalPlaces++;
            }

            // Créer les places STANDARD
            int standardPlaces = repartition.get("STANDARD");
            int rangeeActuelle = 3;
            int placeDansRangee = 1;
            
            for (int i = 0; i < standardPlaces; i++) {
                if (placeDansRangee > salle.getPlacesParRangee()) {
                    rangeeActuelle++;
                    placeDansRangee = 1;
                }
                
                Place place = new Place();
                place.setSalle(salle);
                place.setNumeroRangee(rangeeActuelle);
                place.setNumeroPlace(placeDansRangee);
                place.setCategorie("STANDARD");
                depotPlace.save(place);
                totalPlaces++;
                placeDansRangee++;
            }

            return ResponseEntity.ok(totalPlaces + " places créées pour la salle " + salleId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'initialisation des places: " + e.getMessage());
        }
    }

    @PostMapping("/salle/{salleId}/initialiser-avec-config")
    public ResponseEntity<String> initialiserPlacesSalleAvecConfiguration(@PathVariable Long salleId, @RequestBody Map<String, Integer> repartition) {
        try {
            Salle salle = depotSalle.findById(salleId).orElse(null);
            if (salle == null) {
                return ResponseEntity.badRequest().body("Salle non trouvée");
            }

            // Supprimer les places existantes pour cette salle
            depotPlace.deleteBySalleId(salleId);

            // Créer les nouvelles places selon la configuration fournie
            int totalPlaces = 0;
            
            // Créer les places VIP
            for (int i = 0; i < repartition.getOrDefault("VIP", 0); i++) {
                Place place = new Place();
                place.setSalle(salle);
                place.setNumeroRangee(1); // Places VIP en première rangée
                place.setNumeroPlace(i + 1);
                place.setCategorie("VIP");
                depotPlace.save(place);
                totalPlaces++;
            }

            // Créer les places PREMIUM
            for (int i = 0; i < repartition.getOrDefault("PREMIUM", 0); i++) {
                Place place = new Place();
                place.setSalle(salle);
                place.setNumeroRangee(2); // Places PREMIUM en deuxième rangée
                place.setNumeroPlace(i + 1);
                place.setCategorie("PREMIUM");
                depotPlace.save(place);
                totalPlaces++;
            }

            // Créer les places STANDARD
            int standardPlaces = repartition.getOrDefault("STANDARD", 0);
            int rangeeActuelle = 3;
            int placeDansRangee = 1;
            
            for (int i = 0; i < standardPlaces; i++) {
                if (placeDansRangee > salle.getPlacesParRangee()) {
                    rangeeActuelle++;
                    placeDansRangee = 1;
                }
                
                Place place = new Place();
                place.setSalle(salle);
                place.setNumeroRangee(rangeeActuelle);
                place.setNumeroPlace(placeDansRangee);
                place.setCategorie("STANDARD");
                depotPlace.save(place);
                totalPlaces++;
                placeDansRangee++;
            }

            return ResponseEntity.ok(totalPlaces + " places créées pour la salle " + salleId + " avec configuration: " + repartition);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'initialisation des places: " + e.getMessage());
        }
    }

    @PostMapping("/salle/{salleId}/configurer")
    public ResponseEntity<String> configurerPlacesSalle(@PathVariable Long salleId, @RequestBody Map<String, Object> configuration) {
        try {
            Salle salle = depotSalle.findById(salleId).orElse(null);
            if (salle == null) {
                return ResponseEntity.badRequest().body("Salle non trouvée");
            }

            // Mettre à jour la capacité et la répartition
            Integer capacite = (Integer) configuration.get("capacite");
            if (capacite != null) {
                salle.setCapacite(capacite);
            }

            @SuppressWarnings("unchecked")
            Map<String, Integer> repartition = (Map<String, Integer>) configuration.get("repartition");
            if (repartition != null) {
                salle.setRepartitionPlacesMap(repartition);
            }

            depotSalle.save(salle);

            // Recréer les places avec la nouvelle configuration en passant directement la répartition
            return initialiserPlacesSalleAvecConfiguration(salleId, repartition);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la configuration des places: " + e.getMessage());
        }
    }

    @PostMapping("/salle/{salleId}/configurer-places")
    public ResponseEntity<String> configurerPlaces(@PathVariable Long salleId, @RequestBody Map<String, Integer> configuration) {
        try {
            reinitialisationSallesService.configurerPlaces(salleId, configuration);
            return ResponseEntity.ok("Configuration des places mise à jour avec succès.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la configuration des places : " + e.getMessage());
        }
    }
}