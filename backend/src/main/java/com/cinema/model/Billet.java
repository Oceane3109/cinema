package com.cinema.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "billets")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Billet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_billet", unique = true, nullable = false)
    private String referenceBillet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "code_qr", unique = true, nullable = false)
    private String codeQr;

    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal prixUnitaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutBillet statut = StatutBillet.ACTIF;

    @Column(name = "date_emission", nullable = false)
    private LocalDateTime dateEmission;

    @Column(name = "date_utilisation")
    private LocalDateTime dateUtilisation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (referenceBillet == null) {
            referenceBillet = "BILLET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (codeQr == null) {
            codeQr = UUID.randomUUID().toString();
        }
        if (dateEmission == null) {
            dateEmission = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum StatutBillet {
        ACTIF, UTILISE, ANNULE, EXPIRE
    }
}