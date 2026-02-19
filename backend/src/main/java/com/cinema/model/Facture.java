package com.cinema.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "factures")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_facture", unique = true, nullable = false)
    private String numeroFacture;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", unique = true, nullable = false)
    private Reservation reservation;

    @Column(name = "nom_client", nullable = false)
    private String nomClient;

    @Column(name = "email_client", nullable = false)
    private String emailClient;

    @Column(name = "telephone_client")
    private String telephoneClient;

    @Column(name = "montant_ht", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantHt;

    @Column(name = "tva_taux", nullable = false, precision = 5, scale = 2, columnDefinition = "DECIMAL(5,2) DEFAULT 20.00")
    private BigDecimal tvaTaux = new BigDecimal("20.00");

    @Column(name = "montant_tva", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTva;

    @Column(name = "montant_ttc", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTtc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutFacture statut = StatutFacture.PAYE;

    @Column(name = "date_emission", nullable = false)
    private LocalDateTime dateEmission;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (numeroFacture == null) {
            numeroFacture = "FAC-" + LocalDateTime.now().getYear() + "-" +
                          String.format("%06d", Math.abs(UUID.randomUUID().hashCode()) % 1000000);
        }
        if (dateEmission == null) {
            dateEmission = LocalDateTime.now();
        }
        if (datePaiement == null && statut == StatutFacture.PAYE) {
            datePaiement = LocalDateTime.now();
        }
        // Calculer automatiquement la TVA et TTC
        calculerTvaEtTtc();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculerTvaEtTtc();
    }

    private void calculerTvaEtTtc() {
        if (montantHt != null && tvaTaux != null) {
            montantTva = montantHt.multiply(tvaTaux).divide(new BigDecimal("100"));
            montantTtc = montantHt.add(montantTva);
        }
    }

    public void recalculerMontants() {
        calculerTvaEtTtc();
    }

    public enum StatutFacture {
        PAYE, ANNULE, REMBOURSE, EN_ATTENTE
    }
}