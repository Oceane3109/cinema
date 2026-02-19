package com.cinema.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "diffusion_pub")
public class Diffusionpub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int id_societe;
    private int id_sceance;
    private double prix_unitaire;
    private String statut;

    @Column(name = "film_id", nullable = false)
    private Long filmId;

    @Column(name = "date_heure_diffusion", nullable = false)
    private LocalDateTime dateHeureDiff;

    public Diffusionpub(int id, int id_societe, int id_sceance, LocalDateTime date_heure_diff, double prix_unitaire,
            String statut) {
        this.id = id;
        this.id_societe = id_societe;
        this.id_sceance = id_sceance;
        this.dateHeureDiff = date_heure_diff;
        this.prix_unitaire = prix_unitaire;
        this.statut = statut;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdSociete() {
        return id_societe;
    }

    public void setIdSociete(int id_societe) {
        this.id_societe = id_societe;
    }

    public int getIdSceance() {
        return id_sceance;
    }

    public void setIdSceance(int id_sceance) {
        this.id_sceance = id_sceance;
    }

    public LocalDateTime getDateHeureDiff() {
        return dateHeureDiff;
    }

    public void setDateHeureDiff(LocalDateTime date_heure_diff) {
        this.dateHeureDiff = date_heure_diff;
    }

    public double getPrixUnitaire() {
        return prix_unitaire;
    }

    public void setPrixUnitaire(double prix_unitaire) {
        this.prix_unitaire = prix_unitaire;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Long getFilmId() {
        return filmId;
    }

    public void setFilmId(Long filmId) {
        this.filmId = filmId;
    }

    public Film getFilm() {
        // Placeholder implementation; replace with actual logic to retrieve the film
        return new Film();
    }
}
