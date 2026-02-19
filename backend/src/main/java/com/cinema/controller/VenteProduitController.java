package com.cinema.controller;

import com.cinema.model.VenteProduit;
import com.cinema.service.VenteProduitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ventes-produits")
@CrossOrigin(origins = "*")
public class VenteProduitController {
    
    @Autowired
    private VenteProduitService venteProduitService;
    
    @PostMapping
    public ResponseEntity<?> createVente(@RequestBody VenteProduit vente) {
        try {
            VenteProduit newVente = venteProduitService.createVente(vente);
            return ResponseEntity.ok(newVente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de l'enregistrement de la vente");
        }
    }
    
    @GetMapping("/seance/{idSeance}")
    public ResponseEntity<List<VenteProduit>> getVentesBySeance(@PathVariable Integer idSeance) {
        try {
            List<VenteProduit> ventes = venteProduitService.getVentesBySeance(idSeance);
            return ResponseEntity.ok(ventes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/jour/{date}")
    public ResponseEntity<List<VenteProduit>> getVentesByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            List<VenteProduit> ventes = venteProduitService.getVentesByDate(date);
            return ResponseEntity.ok(ventes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/total/seance/{idSeance}")
    public ResponseEntity<BigDecimal> getMontantTotalBySeance(@PathVariable Integer idSeance) {
        try {
            BigDecimal total = venteProduitService.getMontantTotalBySeance(idSeance);
            return ResponseEntity.ok(total);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/du-jour")
    public ResponseEntity<List<VenteProduit>> getVentesDuJour() {
        try {
            List<VenteProduit> ventes = venteProduitService.getVentesDuJour();
            return ResponseEntity.ok(ventes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/chiffre-affaires-du-jour")
    public ResponseEntity<BigDecimal> getChiffreAffairesDuJour() {
        try {
            BigDecimal total = venteProduitService.getChiffreAffairesDuJour();
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
