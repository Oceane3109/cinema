package com.cinema.service;

import com.cinema.model.Place;
import com.cinema.model.Salle;
import com.cinema.repository.DepotPlace;
import com.cinema.repository.DepotSalle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReinitialisationSallesService {

    @Autowired
    private DepotSalle depotSalle;

    @Autowired
    private DepotPlace depotPlace;

    @Transactional
    public Map<String, Object> reinitialiserToutesLesSalles() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. Supprimer toutes les places existantes
            long placesSupprimees = depotPlace.count();
            depotPlace.deleteAll();
            
            // 2. Récupérer toutes les salles
            var salles = depotSalle.findAll();
            int totalPlacesCrees = 0;
            
            for (Salle salle : salles) {
                // 3. Réinitialiser la configuration de la salle
                Map<String, Integer> repartitionParDefaut = new HashMap<>();
                repartitionParDefaut.put("VIP", 0);
                repartitionParDefaut.put("PREMIUM", 0);
                repartitionParDefaut.put("STANDARD", salle.getCapacite() != null ? salle.getCapacite() : 100);
                
                salle.setRepartitionPlacesMap(repartitionParDefaut);
                
                // S'assurer que la salle a des valeurs par défaut
                if (salle.getCapacite() == null || salle.getCapacite() == 0) {
                    salle.setCapacite(100);
                }
                if (salle.getNombreRangees() == null || salle.getNombreRangees() == 0) {
                    salle.setNombreRangees(10);
                }
                if (salle.getPlacesParRangee() == null || salle.getPlacesParRangee() == 0) {
                    salle.setPlacesParRangee(10);
                }
                
                depotSalle.save(salle);
                
                // 4. Créer les places pour cette salle
                int placesCreesPourSalle = creerPlacesPourSalle(salle, repartitionParDefaut);
                totalPlacesCrees += placesCreesPourSalle;
                
                System.out.println("Salle réinitialisée: " + salle.getNom() + 
                    " (ID: " + salle.getId() + ") - " + placesCreesPourSalle + " places créées");
            }
            
            result.put("statut", "SUCCÈS");
            result.put("placesSupprimees", placesSupprimees);
            result.put("sallesTraitees", salles.size());
            result.put("totalPlacesCrees", totalPlacesCrees);
            
            System.out.println("=== RÉINITIALISATION TERMINÉE ===");
            System.out.println("Places supprimées: " + placesSupprimees);
            System.out.println("Salles traitées: " + salles.size());
            System.out.println("Total places créées: " + totalPlacesCrees);
            
        } catch (Exception e) {
            result.put("statut", "ERREUR");
            result.put("message", e.getMessage());
            System.err.println("Erreur lors de la réinitialisation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    private int creerPlacesPourSalle(Salle salle, Map<String, Integer> repartition) {
        int totalPlaces = 0;
        
        // Créer les places VIP
        for (int i = 0; i < repartition.getOrDefault("VIP", 0); i++) {
            Place place = new Place();
            place.setSalle(salle);
            place.setNumeroRangee(1);
            place.setNumeroPlace(i + 1);
            place.setCategorie("VIP");
            depotPlace.save(place);
            totalPlaces++;
        }
        
        // Créer les places PREMIUM
        for (int i = 0; i < repartition.getOrDefault("PREMIUM", 0); i++) {
            Place place = new Place();
            place.setSalle(salle);
            place.setNumeroRangee(2);
            place.setNumeroPlace(i + 1);
            place.setCategorie("PREMIUM");
            depotPlace.save(place);
            totalPlaces++;
        }
        
        // Créer les places STANDARD
        int standardPlaces = repartition.getOrDefault("STANDARD", salle.getCapacite());
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
        
        return totalPlaces;
    }
    
    @Transactional
    public void configurerPlaces(Long salleId, Map<String, Integer> configuration) {
        Salle salle = depotSalle.findById(salleId).orElseThrow(() -> new IllegalArgumentException("Salle introuvable"));

        // Supprimer les anciennes places
        depotPlace.deleteBySalleId(salleId);

        // Générer les nouvelles places de manière aléatoire
        int totalPlaces = configuration.values().stream().mapToInt(Integer::intValue).sum();
        int rangees = salle.getNombreRangees();
        int placesParRangee = totalPlaces / rangees;

        int currentRow = 1;
        int currentSeat = 1;

        for (Map.Entry<String, Integer> entry : configuration.entrySet()) {
            String typePlace = entry.getKey();
            int count = entry.getValue();

            for (int i = 0; i < count; i++) {
                Place place = new Place();
                place.setSalle(salle);
                place.setType(typePlace);
                place.setRangee(currentRow);
                place.setNumero(currentSeat);

                depotPlace.save(place);

                currentSeat++;
                if (currentSeat > placesParRangee) {
                    currentSeat = 1;
                    currentRow++;
                }
            }
        }
    }
}
