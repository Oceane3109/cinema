package com.cinema.controller;

import com.cinema.model.ParametreReservation;
import com.cinema.service.ParametreReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/parametres-reservation")
@CrossOrigin(origins = "http://localhost:3000")
public class ControleurParametreReservation {
    
    @Autowired
    private ParametreReservationService parametreReservationService;
    
    @GetMapping
    public ResponseEntity<List<ParametreReservation>> getAllParametres() {
        List<ParametreReservation> parametres = parametreReservationService.getAllParametres();
        return ResponseEntity.ok(parametres);
    }
    
    @GetMapping("/{nom}")
    public ResponseEntity<ParametreReservation> getParametre(@PathVariable String nom) {
        Optional<ParametreReservation> parametre = parametreReservationService.getParametre(nom);
        return parametre.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<ParametreReservation> createParametre(@RequestBody ParametreReservation parametre) {
        ParametreReservation savedParametre = parametreReservationService.saveOrUpdateParametre(parametre);
        return ResponseEntity.ok(savedParametre);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ParametreReservation> updateParametre(@PathVariable Long id, @RequestBody ParametreReservation parametre) {
        parametre.setId(id);
        ParametreReservation updatedParametre = parametreReservationService.saveOrUpdateParametre(parametre);
        return ResponseEntity.ok(updatedParametre);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParametre(@PathVariable Long id) {
        parametreReservationService.deleteParametre(id);
        return ResponseEntity.ok().build();
    }
    
    // Endpoints spécifiques pour les paramètres de remise
    @GetMapping("/remise/info")
    public ResponseEntity<java.util.Map<String, Object>> getRemiseInfo() {
        java.util.Map<String, Object> remiseInfo = new java.util.HashMap<>();
        remiseInfo.put("placesMinimum", parametreReservationService.getNombrePlacesMinimumPourRemise());
        remiseInfo.put("pourcentageRemise", parametreReservationService.getPourcentageRemise().doubleValue());
        return ResponseEntity.ok(remiseInfo);
    }
    
    @PostMapping("/remise/calculer")
    public ResponseEntity<java.util.Map<String, Object>> calculerRemise(@RequestBody java.util.Map<String, Object> request) {
        try {
            // Extraire les données de la requête
            double totalHT = ((Number) request.get("totalHT")).doubleValue();
            int nombrePlaces = ((Number) request.get("nombrePlaces")).intValue();
            
            // Calculer la remise
            java.math.BigDecimal totalHTBig = java.math.BigDecimal.valueOf(totalHT);
            java.math.BigDecimal remise = parametreReservationService.calculerRemise(totalHTBig, nombrePlaces);
            java.math.BigDecimal totalAvecRemise = parametreReservationService.calculerTotalAvecRemise(totalHTBig, nombrePlaces);
            boolean appliquerRemise = parametreReservationService.appliquerRemise(nombrePlaces);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("appliquerRemise", appliquerRemise);
            result.put("montantRemise", remise.doubleValue());
            result.put("totalAvecRemise", totalAvecRemise.doubleValue());
            result.put("placesMinimum", parametreReservationService.getNombrePlacesMinimumPourRemise());
            result.put("pourcentageRemise", parametreReservationService.getPourcentageRemise().doubleValue());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("error", "Erreur lors du calcul de la remise: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
