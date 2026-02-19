package com.cinema.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reservations")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seance_id", nullable = false)
    private Seance seance;

    @Column(name = "nom_client", nullable = false)
    private String nomClient;

    @Column(name = "email_client")
    private String emailClient;

    @Column(name = "telephone_client")
    private String telephoneClient;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("reservation")
    private List<ReservationPlace> reservationPlaces;

    @Column(name = "montant_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(name = "reference_reservation", nullable = false, unique = true)
    private String referenceReservation;

    @Column(nullable = false)
    private String statut = "CONFIRMEE";

    @Column(name = "date_reservation")
    private LocalDateTime dateReservation;

    @Column(name = "modification_count", nullable = false)
    private Integer modificationCount = 0;

    @Column(name = "can_modify", nullable = false)
    private Boolean canModify = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Méthode utilitaire pour calculer le montant total
    public void calculerMontantTotal() {
        if (this.reservationPlaces != null && !this.reservationPlaces.isEmpty()) {
            this.montantTotal = this.reservationPlaces.stream()
                .map(ReservationPlace::getPrixUnitaire)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            this.montantTotal = BigDecimal.ZERO;
        }
    }

    // Vérifier si la réservation peut être modifiée
    public boolean peutEtreModifiee() {
        return "CONFIRMEE".equals(this.statut) &&
               (this.canModify == null || Boolean.TRUE.equals(this.canModify)) &&
               (this.modificationCount == null || this.modificationCount < 1);
    }

    // Marquer comme modifié (une seule fois)
    public void marquerCommeModifie() {
        this.modificationCount = this.modificationCount + 1;
        if (this.modificationCount >= 1) {
            this.canModify = false;
        }
    }

    // Méthode utilitaire pour obtenir le nombre de places
    public int getNombrePlaces() {
        return this.reservationPlaces != null ? this.reservationPlaces.size() : 0;
    }
}
