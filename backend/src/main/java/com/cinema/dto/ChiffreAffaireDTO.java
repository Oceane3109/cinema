package com.cinema.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ChiffreAffaireDTO {
    private Long id;
    private Long filmId;
    private String filmTitre;
    private LocalDate dateCalcul;
    private String typePeriode;
    private Integer nombreEntrees;
    private BigDecimal chiffreAffaire;
    private BigDecimal tarifMoyen;
    private BigDecimal pourcentageVariation;
    private BigDecimal dejaPaye;
    private BigDecimal resteAPayer;

    public ChiffreAffaireDTO() {}

    public ChiffreAffaireDTO(Long id, Long filmId, String filmTitre, LocalDate dateCalcul, 
                           String typePeriode, Integer nombreEntrees, BigDecimal chiffreAffaire, 
                           BigDecimal tarifMoyen, BigDecimal pourcentageVariation, BigDecimal dejaPaye, 
                           BigDecimal resteAPayer) {
        this.id = id;
        this.filmId = filmId;
        this.filmTitre = filmTitre;
        this.dateCalcul = dateCalcul;
        this.typePeriode = typePeriode;
        this.nombreEntrees = nombreEntrees;
        this.chiffreAffaire = chiffreAffaire;
        this.tarifMoyen = tarifMoyen;
        this.pourcentageVariation = pourcentageVariation;
        this.dejaPaye = dejaPaye;
        this.resteAPayer = resteAPayer;
    }

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

    public String getFilmTitre() {
        return filmTitre;
    }

    public void setFilmTitre(String filmTitre) {
        this.filmTitre = filmTitre;
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

    public BigDecimal getTarifMoyen() {
        return tarifMoyen;
    }

    public void setTarifMoyen(BigDecimal tarifMoyen) {
        this.tarifMoyen = tarifMoyen;
    }

    public BigDecimal getPourcentageVariation() {
        return pourcentageVariation;
    }

    public void setPourcentageVariation(BigDecimal pourcentageVariation) {
        this.pourcentageVariation = pourcentageVariation;
    }

    public BigDecimal getDejaPaye() {
        return dejaPaye;
    }

    public void setDejaPaye(BigDecimal dejaPaye) {
        this.dejaPaye = dejaPaye;
    }

    public BigDecimal getResteAPayer() {
        return resteAPayer;
    }

    public void setResteAPayer(BigDecimal resteAPayer) {
        this.resteAPayer = resteAPayer;
    }

    @Override
    public String toString() {
        return "ChiffreAffaireDTO{" +
                "filmId=" + filmId +
                ", filmTitre='" + filmTitre + '\'' +
                ", dateCalcul=" + dateCalcul +
                ", typePeriode='" + typePeriode + '\'' +
                ", nombreEntrees=" + nombreEntrees +
                ", chiffreAffaire=" + chiffreAffaire +
                ", tarifMoyen=" + tarifMoyen +
                '}';
    }
}
