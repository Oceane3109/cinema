package com.cinema.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "chiffre_affaire_film")
public class ChiffreAffaireFilm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "film_id", nullable = false)
    private Long filmId;
    
    @Column(name = "date_calcul", nullable = false)
    private LocalDate dateCalcul;
    
    @Column(name = "type_periode", nullable = false, length = 10)
    private String typePeriode;
    
    @Column(name = "nombre_entrées", nullable = false)
    private Integer nombreEntrees;
    
    @Column(name = "chiffre_affaire", nullable = false, precision = 12, scale = 2)
    private BigDecimal chiffreAffaire;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ChiffreAffaireFilm() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFilmId() {
        return filmId;
    }

    public void setFilmId(Long filmId) {
        this.filmId = filmId;
    }

    public LocalDate getDateCalcul() {
        return dateCalcul;
    }

    public void setDateCalcul(LocalDate dateCalcul) {
        this.dateCalcul = dateCalcul;
    }

    public String getTypePeriode() {
        return typePeriode;
    }

    public void setTypePeriode(String typePeriode) {
        this.typePeriode = typePeriode;
    }

    public Integer getNombreEntrees() {
        return nombreEntrees;
    }

    public void setNombreEntrees(Integer nombreEntrees) {
        this.nombreEntrees = nombreEntrees;
    }

    public BigDecimal getChiffreAffaire() {
        return chiffreAffaire;
    }

    public void setChiffreAffaire(BigDecimal chiffreAffaire) {
        this.chiffreAffaire = chiffreAffaire;
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
