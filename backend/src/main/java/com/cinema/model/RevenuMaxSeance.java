package com.cinema.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "revenu_max_seance")
public class RevenuMaxSeance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "salle_id", nullable = false)
    private Long salleId;
    
    @Column(name = "film_id", nullable = false)
    private Long filmId;
    
    @Column(name = "date_calcul", nullable = false)
    private LocalDate dateCalcul;
    
    @Column(name = "revenu_max_seance", nullable = false, precision = 12, scale = 2)
    private BigDecimal revenuMaxSeance;
    
    @Column(name = "nombre_places_total", nullable = false)
    private Integer nombrePlacesTotal;
    
    @Column(name = "nombre_places_vendues", nullable = false)
    private Integer nombrePlacesVendues;
    
    @Column(name = "taux_occupation", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxOccupation;
    
    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;
    
    // Constructeurs
    public RevenuMaxSeance() {}
    
    public RevenuMaxSeance(Long salleId, Long filmId, BigDecimal revenuMaxSeance, 
                              Integer nombrePlacesTotal, BigDecimal tauxOccupation) {
        this.salleId = salleId;
        this.filmId = filmId;
        this.dateCalcul = LocalDate.now();
        this.revenuMaxSeance = revenuMaxSeance;
        this.nombrePlacesTotal = nombrePlacesTotal;
        this.tauxOccupation = tauxOccupation;
        this.createdAt = LocalDate.now();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getSalleId() { return salleId; }
    public void setSalleId(Long salleId) { this.salleId = salleId; }
    
    public Long getFilmId() { return filmId; }
    public void setFilmId(Long filmId) { this.filmId = filmId; }
    
    public LocalDate getDateCalcul() { return dateCalcul; }
    public void setDateCalcul(LocalDate dateCalcul) { this.dateCalcul = dateCalcul; }
    
    public BigDecimal getRevenuMaxSeance() { return revenuMaxSeance; }
    public void setRevenuMaxSeance(BigDecimal revenuMaxSeance) { this.revenuMaxSeance = revenuMaxSeance; }
    
    public Integer getNombrePlacesTotal() { return nombrePlacesTotal; }
    public void setNombrePlacesTotal(Integer nombrePlacesTotal) { this.nombrePlacesTotal = nombrePlacesTotal; }
    
    public Integer getNombrePlacesVendues() { return nombrePlacesVendues; }
    public void setNombrePlacesVendues(Integer nombrePlacesVendues) { this.nombrePlacesVendues = nombrePlacesVendues; }
    
    public BigDecimal getTauxOccupation() { return tauxOccupation; }
    public void setTauxOccupation(BigDecimal tauxOccupation) { this.tauxOccupation = tauxOccupation; }
    
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}
