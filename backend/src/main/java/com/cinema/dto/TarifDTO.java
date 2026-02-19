package com.cinema.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TarifDTO {
    private Long id;
    private String nom;
    private BigDecimal prix;
    private String description;
    private String type;
    private String genre;
    private Integer ageMin;
    private Integer ageMax;
    private String descriptionComplementaire;
    private Boolean actif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TarifDTO() {}

    public TarifDTO(Long id, String nom, BigDecimal prix, String description, String type, 
                    String genre, Integer ageMin, Integer ageMax, String descriptionComplementaire, 
                    Boolean actif, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.description = description;
        this.type = type;
        this.genre = genre;
        this.ageMin = ageMin;
        this.ageMax = ageMax;
        this.descriptionComplementaire = descriptionComplementaire;
        this.actif = actif;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getAgeMin() {
        return ageMin;
    }

    public void setAgeMin(Integer ageMin) {
        this.ageMin = ageMin;
    }

    public Integer getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(Integer ageMax) {
        this.ageMax = ageMax;
    }

    public String getDescriptionComplementaire() {
        return descriptionComplementaire;
    }

    public void setDescriptionComplementaire(String descriptionComplementaire) {
        this.descriptionComplementaire = descriptionComplementaire;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
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

    @Override
    public String toString() {
        return "TarifDTO{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", type='" + type + '\'' +
                ", genre='" + genre + '\'' +
                ", actif=" + actif +
                '}';
    }
}
