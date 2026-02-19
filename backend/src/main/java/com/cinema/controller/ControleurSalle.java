package com.cinema.controller;

import com.cinema.model.Salle;
import com.cinema.model.Place;
import com.cinema.repository.DepotSalle;
import com.cinema.repository.DepotPlace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/salles")
@CrossOrigin(origins = "*")
public class ControleurSalle {

    @Autowired
    private DepotSalle depotSalle;

    @Autowired
    private DepotPlace depotPlace;

    @GetMapping
    public List<Salle> getToutesLesSalles() {
        return depotSalle.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Salle> getSalleParId(@PathVariable Long id) {
        Optional<Salle> salle = depotSalle.findById(id);
        if (salle.isPresent()) {
            return ResponseEntity.ok(salle.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/nom/{nom}")
    public ResponseEntity<Salle> getSalleParNom(@PathVariable String nom) {
        Optional<Salle> salle = depotSalle.findByNom(nom);
        if (salle.isPresent()) {
            return ResponseEntity.ok(salle.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/type/{type}")
    public List<Salle> getSallesParType(@PathVariable String type) {
        return depotSalle.findByType(type);
    }

    @GetMapping("/capacite/{capacite}")
    public List<Salle> getSallesAvecCapaciteMinimale(@PathVariable Integer capacite) {
        return depotSalle.findSallesAvecCapaciteMinimale(capacite);
    }

    @PostMapping
    public ResponseEntity<?> creerSalle(@RequestBody Salle salle) {
        try {
            // Sauvegarder la salle d'abord
            salle.setCreatedAt(LocalDateTime.now());
            salle.setUpdatedAt(LocalDateTime.now());
            Salle salleSauvegardee = depotSalle.save(salle);
            
            // Générer automatiquement les places si la répartition est configurée
            if (salleSauvegardee.getRepartitionPlaces() != null && !salleSauvegardee.getRepartitionPlaces().isEmpty()) {
                genererPlacesPourSalle(salleSauvegardee);
            }
            
            return ResponseEntity.ok(salleSauvegardee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la création de la salle: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifierSalle(@PathVariable Long id, @RequestBody Salle detailsSalle) {
        try {
            Optional<Salle> salleOptional = depotSalle.findById(id);
            if (salleOptional.isPresent()) {
                Salle salle = salleOptional.get();
                salle.setNom(detailsSalle.getNom());
                salle.setCapacite(detailsSalle.getCapacite());
                salle.setType(detailsSalle.getType());
                salle.setDescription(detailsSalle.getDescription());
                salle.setRepartitionPlaces(detailsSalle.getRepartitionPlaces());
                salle.setUpdatedAt(LocalDateTime.now());
                
                Salle salleSauvegardee = depotSalle.save(salle);
                
                // Ne pas regénérer automatiquement les places pour éviter les erreurs
                // L'utilisateur peut utiliser le bouton "Configurer les places" manuellement
                // if (salleSauvegardee.getRepartitionPlaces() != null && !salleSauvegardee.getRepartitionPlaces().isEmpty()) {
                //     genererPlacesPourSalle(salleSauvegardee);
                // }
                
                return ResponseEntity.ok(salleSauvegardee);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la modification de la salle: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerSalle(@PathVariable Long id) {
        if (depotSalle.existsById(id)) {
            depotSalle.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private void genererPlacesPourSalle(Salle salle) {
        try {
            // Supprimer les places existantes pour cette salle
            depotPlace.deleteBySalleId(salle.getId());
            
            // Récupérer la configuration de répartition
            Map<String, Integer> repartition = salle.getRepartitionPlacesMap();
            
            int totalPlaces = 0;
            int placeCounter = 1;
            int rangeeCounter = 1;
            int placesParRangee = 10;
            
            // Créer les places VIP
            Integer vipCount = repartition.getOrDefault("VIP", 0);
            for (int i = 0; i < vipCount; i++) {
                Place place = new Place();
                place.setSalle(salle);
                place.setNumeroRangee(rangeeCounter);
                place.setNumeroPlace(placeCounter);
                place.setCategorie("VIP");
                place.setCreatedAt(LocalDateTime.now());
                place.setUpdatedAt(LocalDateTime.now());
                depotPlace.save(place);
                
                if (placeCounter % placesParRangee == 0) {
                    rangeeCounter++;
                }
                placeCounter++;
                totalPlaces++;
            }
            
            // Créer les places PREMIUM
            Integer premiumCount = repartition.getOrDefault("PREMIUM", 0);
            for (int i = 0; i < premiumCount; i++) {
                Place place = new Place();
                place.setSalle(salle);
                place.setNumeroRangee(rangeeCounter);
                place.setNumeroPlace(placeCounter - vipCount);
                place.setCategorie("PREMIUM");
                place.setCreatedAt(LocalDateTime.now());
                place.setUpdatedAt(LocalDateTime.now());
                depotPlace.save(place);
                
                if ((placeCounter - vipCount) % placesParRangee == 0) {
                    rangeeCounter++;
                }
                placeCounter++;
                totalPlaces++;
            }
            
            // Créer les places STANDARD
            Integer standardCount = repartition.getOrDefault("STANDARD", 0);
            for (int i = 0; i < standardCount; i++) {
                Place place = new Place();
                place.setSalle(salle);
                place.setNumeroRangee(rangeeCounter);
                place.setNumeroPlace(placeCounter - vipCount - premiumCount);
                place.setCategorie("STANDARD");
                place.setCreatedAt(LocalDateTime.now());
                place.setUpdatedAt(LocalDateTime.now());
                depotPlace.save(place);
                
                if ((placeCounter - vipCount - premiumCount) % placesParRangee == 0) {
                    rangeeCounter++;
                }
                placeCounter++;
                totalPlaces++;
            }
            
            System.out.println("Généré " + totalPlaces + " places pour la salle " + salle.getNom() + 
                " (VIP: " + vipCount + ", PREMIUM: " + premiumCount + ", STANDARD: " + standardCount + ")");
                
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération des places pour la salle " + salle.getNom() + ": " + e.getMessage());
            throw e;
        }
    }
}




