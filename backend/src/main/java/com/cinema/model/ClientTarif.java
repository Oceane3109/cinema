package com.cinema.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_tarifs")
public class ClientTarif {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;
    
    @Column(name = "tarif_id", nullable = false)
    private Long tarifId;
    
    @Column(name = "genre_applique", nullable = false, length = 20)
    private String genreApplique;
    
    @Column(name = "tarif_applique", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifApplique;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ClientTarif() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getTarifId() {
        return tarifId;
    }

    public void setTarifId(Long tarifId) {
        this.tarifId = tarifId;
    }

    public String getGenreApplique() {
        return genreApplique;
    }

    public void setGenreApplique(String genreApplique) {
        this.genreApplique = genreApplique;
    }

    public BigDecimal getTarifApplique() {
        return tarifApplique;
    }

    public void setTarifApplique(BigDecimal tarifApplique) {
        this.tarifApplique = tarifApplique;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
