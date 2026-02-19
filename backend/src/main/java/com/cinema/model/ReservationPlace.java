package com.cinema.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_places")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ReservationPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal prixUnitaire;

    @Column(name = "type_client", nullable = false)
    private String typeClient = "adulte"; // "adulte", "enfant", "ado", "senior"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}