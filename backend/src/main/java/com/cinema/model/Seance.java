package com.cinema.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "seances")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "film_id", nullable = false)
    private Film film;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(name = "places_disponibles", nullable = false)
    private Integer placesDisponibles;

    @Column(nullable = false)
    private String statut = "ACTIVE";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Méthode utilitaire pour vérifier si la séance a des places disponibles
    public boolean aPlacesDisponibles(int nombrePlaces) {
        return this.placesDisponibles >= nombrePlaces;
    }

    // Méthode utilitaire pour réduire le nombre de places disponibles
    public void reduirePlacesDisponibles(int nombrePlaces) {
        if (this.placesDisponibles >= nombrePlaces) {
            this.placesDisponibles -= nombrePlaces;
        } else {
            throw new IllegalStateException("Pas assez de places disponibles");
        }
    }
}
