package com.cinema.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "salles")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(nullable = false)
    private Integer capacite;

    @Column(nullable = false)
    private String type = "STANDARD";

    @Column(length = 1000)
    private String description;

    @Column(name = "nombre_rangees", nullable = false)
    private Integer nombreRangees = 10;

    @Column(name = "places_par_rangee", nullable = false)
    private Integer placesParRangee = 15;

    @Column(name = "repartition_places", columnDefinition = "TEXT")
    private String repartitionPlaces = "{\"VIP\":0,\"PREMIUM\":0,\"STANDARD\":0}";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Méthode utilitaire pour obtenir la répartition des places
    public Map<String, Integer> getRepartitionPlacesMap() {
        try {
            if (repartitionPlaces == null || repartitionPlaces.isEmpty()) {
                Map<String, Integer> defaultRepartition = new HashMap<>();
                defaultRepartition.put("VIP", 0);
                defaultRepartition.put("PREMIUM", 0);
                defaultRepartition.put("STANDARD", capacite != null ? capacite : 0);
                return defaultRepartition;
            }
            
            // Parser le JSON repartitionPlaces
            Map<String, Integer> repartition = new HashMap<>();
            
            // Enlever les accolades et diviser par les virgules
            String cleanJson = repartitionPlaces.replaceAll("[{}]", "");
            String[] pairs = cleanJson.split(",");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].replaceAll("\"", "").trim();
                    try {
                        Integer value = Integer.parseInt(keyValue[1].trim());
                        repartition.put(key, value);
                    } catch (NumberFormatException e) {
                        repartition.put(key, 0);
                    }
                }
            }
            
            // S'assurer que toutes les catégories existent
            repartition.putIfAbsent("VIP", 0);
            repartition.putIfAbsent("PREMIUM", 0);
            repartition.putIfAbsent("STANDARD", capacite != null ? capacite : 0);
            
            return repartition;
        } catch (Exception e) {
            System.err.println("Erreur lors du parsing de repartitionPlaces: " + repartitionPlaces + " - " + e.getMessage());
            Map<String, Integer> defaultRepartition = new HashMap<>();
            defaultRepartition.put("VIP", 0);
            defaultRepartition.put("PREMIUM", 0);
            defaultRepartition.put("STANDARD", capacite != null ? capacite : 0);
            return defaultRepartition;
        }
    }

    public void setRepartitionPlacesMap(Map<String, Integer> repartition) {
        // Conversion simple de Map vers JSON
        this.repartitionPlaces = String.format("{\"VIP\":%d,\"PREMIUM\":%d,\"STANDARD\":%d}", 
            repartition.getOrDefault("VIP", 0),
            repartition.getOrDefault("PREMIUM", 0),
            repartition.getOrDefault("STANDARD", 0)
        );
    }
}
