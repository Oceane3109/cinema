package com.cinema.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "places")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @Column(name = "numero_rangee", nullable = false)
    private Integer numeroRangee;

    @Column(name = "numero_place", nullable = false)
    private Integer numeroPlace;

    @Column(nullable = false)
    private String categorie = "STANDARD"; // STANDARD, PREMIUM, VIP

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Méthode utilitaire pour obtenir l'identifiant unique de la place
    public String getIdentifiant() {
        return "R" + numeroRangee + "P" + numeroPlace;
    }

    // Méthode utilitaire pour vérifier si cette place est disponible pour une séance
    public boolean estDisponiblePourSeance(Long seanceId) {
        // Cette logique sera implémentée dans le service
        // Pour l'instant, on retourne true
        return true;
    }

    public void setType(String categorie) {
        this.categorie = categorie;
    }

    public void setRangee(int numeroRangee) {
        this.numeroRangee = numeroRangee;
    }

    public void setNumero(int numeroPlace) {
        this.numeroPlace = numeroPlace;
    }
}