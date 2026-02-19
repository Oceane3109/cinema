package com.cinema.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventes_produits_extra")
public class VenteProduit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "id_seance", nullable = false)
    private Seance seance;
    
    @ManyToOne
    @JoinColumn(name = "id_produit", nullable = false)
    private ProduitExtra produit;
    
    @Column(nullable = false)
    private Integer quantite;
    
    @Column(name = "prix_unitaire_vente", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaireVente;
    
    @Column(name = "date_vente")
    private LocalDateTime dateVente;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (dateVente == null) {
            dateVente = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Seance getSeance() {
        return seance;
    }
    
    public void setSeance(Seance seance) {
        this.seance = seance;
    }
    
    public ProduitExtra getProduit() {
        return produit;
    }
    
    public void setProduit(ProduitExtra produit) {
        this.produit = produit;
    }
    
    public Integer getQuantite() {
        return quantite;
    }
    
    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }
    
    public BigDecimal getPrixUnitaireVente() {
        return prixUnitaireVente;
    }
    
    public void setPrixUnitaireVente(BigDecimal prixUnitaireVente) {
        this.prixUnitaireVente = prixUnitaireVente;
    }
    
    public LocalDateTime getDateVente() {
        return dateVente;
    }
    
    public void setDateVente(LocalDateTime dateVente) {
        this.dateVente = dateVente;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Calcul du montant total
    public BigDecimal getMontantTotal() {
        return prixUnitaireVente.multiply(BigDecimal.valueOf(quantite));
    }
}
