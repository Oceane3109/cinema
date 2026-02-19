package com.cinema.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parametre_reservation")
public class ParametreReservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nom_parametre", nullable = false, unique = true)
    private String nomParametre;
    
    @Column(name = "valeur", nullable = false)
    private String valeur;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "type_valeur", nullable = false)
    private String typeValeur; // STRING, INTEGER, DECIMAL, BOOLEAN
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructeurs
    public ParametreReservation() {}
    
    public ParametreReservation(String nomParametre, String valeur, String description, String typeValeur) {
        this.nomParametre = nomParametre;
        this.valeur = valeur;
        this.description = description;
        this.typeValeur = typeValeur;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNomParametre() { return nomParametre; }
    public void setNomParametre(String nomParametre) { this.nomParametre = nomParametre; }
    
    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getTypeValeur() { return typeValeur; }
    public void setTypeValeur(String typeValeur) { this.typeValeur = typeValeur; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Méthodes utilitaires
    public Integer getValeurAsInteger() {
        try {
            return Integer.parseInt(valeur);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public BigDecimal getValeurAsBigDecimal() {
        try {
            return new BigDecimal(valeur);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
    
    public Boolean getValeurAsBoolean() {
        return "true".equalsIgnoreCase(valeur);
    }
}
